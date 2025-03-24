package it.aulab.progetto_finale_docente.repositories;

import org.springframework.data.repository.ListCrudRepository;

import it.aulab.progetto_finale_docente.models.Category;

public interface CategoryRepository extends ListCrudRepository<Category, Long>{
}