package it.aulab.progetto_finale_docente.services;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface CrudService<ReadDto, Model, Key> {
    List<ReadDto> readAll();
    ReadDto read(Key key);
    ReadDto create(Model model, Principal principal, MultipartFile file);
    ReadDto update(Key key, Model model, MultipartFile file);
    void delete(Key key);
}