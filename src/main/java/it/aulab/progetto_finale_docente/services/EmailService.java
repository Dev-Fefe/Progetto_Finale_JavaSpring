package it.aulab.progetto_finale_docente.services;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
}
