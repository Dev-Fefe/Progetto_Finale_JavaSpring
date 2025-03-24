package it.aulab.progetto_finale_docente.services;

import it.aulab.progetto_finale_docente.models.CareerRequest;
import it.aulab.progetto_finale_docente.models.User;

public interface CareerRequestService {
    boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest);
    void save(CareerRequest careerRequest, User user);
    void careerAccept(Long requestId);
    CareerRequest find(Long id);
    void careerReject(Long requestId);

}
