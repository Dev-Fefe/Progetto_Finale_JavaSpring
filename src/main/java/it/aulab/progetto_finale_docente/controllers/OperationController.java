package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.models.CareerRequest;
import it.aulab.progetto_finale_docente.models.Role;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.RoleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;
import it.aulab.progetto_finale_docente.services.CareerRequestService;

@Controller
@RequestMapping("/operations")
public class OperationController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CareerRequestService careerRequestService;

    // Rotta per la creazione di una richiesta di collaborazione
    @GetMapping("/career/request")
    public String careerRequestCreate(Model viewModel) {
        viewModel.addAttribute("title", "Inserisci la tua richiesta");
        viewModel.addAttribute("careerRequest", new CareerRequest());

        List<Role> roles = roleRepository.findAll();
        roles.removeIf(e -> e.getName().equals("ROLE_USER"));
        viewModel.addAttribute("roles", roles);

        return "career/requestForm";
    }

    //Rotta per il salvataggio di una richiesta di ruolo
    @PostMapping("/career/request/save")
    public String careerRequestStore(@ModelAttribute("careerRequest") CareerRequest careerRequest, Principal principal, RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(principal.getName());

        if (careerRequestService.isRoleAlreadyAssigned(user, careerRequest)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sei già assegnato a questo ruolo!");
            return "redirect:/";
        }
        careerRequestService.save(careerRequest, user);

        redirectAttributes.addFlashAttribute("successMessage", "Richiesta inviata con successo!");
        return "redirect:/";

    }

    //Rotta per il dettaglio di una richiesta
    @GetMapping("/career/request/detail/{id}")
    public String careerRequestDetail(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Dettaglio richiesta");
        viewModel.addAttribute("request", careerRequestService.find(id));
        return "career/requestDetail";
    }

    //Rotta per l'approvazione di una richiesta
    @PostMapping("/career/request/accept/{requestId}")
    public String careerRequestAccept(@PathVariable Long requestId, RedirectAttributes redirectAttributes) {
        careerRequestService.careerAccept(requestId);
        redirectAttributes.addFlashAttribute("successMessage", "Richiesta approvata con successo!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/career/request/reject/{requestId}")
    public String careerRequestReject(@PathVariable Long requestId, RedirectAttributes redirectAttributes) {
        careerRequestService.careerReject(requestId);
        redirectAttributes.addFlashAttribute("successMessage", "Richiesta rifiutata con successo!");
        return "redirect:/admin/dashboard";
    }

}
