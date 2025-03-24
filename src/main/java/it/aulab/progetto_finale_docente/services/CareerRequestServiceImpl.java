package it.aulab.progetto_finale_docente.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.progetto_finale_docente.models.CareerRequest;
import it.aulab.progetto_finale_docente.models.Role;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;
import it.aulab.progetto_finale_docente.repositories.RoleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service

public class CareerRequestServiceImpl implements CareerRequestService{

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest) {
        List<Long> allUserIds = careerRequestRepository.findAllUserIds();

        if (!allUserIds.contains(user.getId())) {
            return false;
        }

        List<Long> requests = careerRequestRepository.findByUserId(user.getId());

        return requests.stream().anyMatch(roleId -> roleId.equals(careerRequest.getRole().getId()));
    }

    public void save(CareerRequest careerRequest, User user) {
        careerRequest.setUser(user);
        careerRequest.setIsChecked(false);
        careerRequestRepository.save(careerRequest);

        //Invio email di richiesta del ruolo all'admin
        emailService.sendSimpleEmail("adminAulabpost@admin.com", "Richiesta di ruolo: " + careerRequest.getRole().getName(), "C'è una nuova richiesta di collaborazione da parte di " + user.getUsername());
    }

    @Override
    public void careerAccept(Long requestId) {
        //Recupero la richiesta
        CareerRequest request = careerRequestRepository.findById(requestId).get();

        //Recupero l'utente che ha richiesto il ruolo ed il ruolo stesso
        User user = request.getUser();
        Role role = request.getRole();

        //Recupero il nuovo ruolo e sostituisco i ruoli esistenti
        Role newRole = roleRepository.findByName(role.getName());
        
        //Creo una nuova lista con solo il nuovo ruolo
        List<Role> rolesUser = new ArrayList<>();
        rolesUser.add(newRole);

        //Salvo le modifiche
        user.setRoles(rolesUser);

        userRepository.save(user);
        request.setIsChecked(true);
        careerRequestRepository.save(request);

        emailService.sendSimpleEmail(user.getEmail(), "Ruolo abilitato", "Ciao, la tua richiesta di collaborazione è stata accettata");
    }



    @Override
    public CareerRequest find(Long id) {
        return careerRequestRepository.findById(id).get();
    }

    @Override
    public void careerReject(Long requestId) {
        // Recupero la richiesta
        CareerRequest request = careerRequestRepository.findById(requestId).get();
        
        // Recupero l'utente che ha richiesto il ruolo
        User user = request.getUser();
        
        // Segno la richiesta come gestita
        request.setIsChecked(true);
        careerRequestRepository.save(request);
        
        // Invio email di rifiuto all'utente
        emailService.sendSimpleEmail(user.getEmail(), "Richiesta rifiutata", 
            "Ciao, la tua richiesta per il ruolo di " + request.getRole().getName() + " è stata rifiutata.");
    }

}
