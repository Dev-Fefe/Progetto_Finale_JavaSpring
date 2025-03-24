package it.aulab.progetto_finale_docente.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Category;
import it.aulab.progetto_finale_docente.models.User;

public interface ArticleRepository extends ListCrudRepository<Article, Long> {
    List<Article> findByCategory(Category category);
    List<Article> findByUser(User user);
    List<Article> findByIsAcceptedTrue();
    List<Article> findByIsAcceptedFalse();
    List<Article> findByIsAcceptedIsNull();

    //Ricerca Base
    @Query("SELECT a FROM Article a WHERE " +
        "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.subtitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(a.body) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "CAST(a.publishDate AS string) LIKE CONCAT('%', :searchTerm, '%') OR " +
        "CAST(a.id AS string) LIKE CONCAT('%', :searchTerm, '%')")


    List<Article> search(@Param("searchTerm") String searchTerm);
}
