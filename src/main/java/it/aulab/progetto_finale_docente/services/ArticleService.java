package it.aulab.progetto_finale_docente.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Category;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long>{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ImageService imageService;

    @Override
    public List<ArticleDto> readAll() {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findAll()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @Override
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if (optArticle.isPresent()) {
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author id=" + key + " not found");
        }
    }

    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        String url = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId())).get();
            article.setUser(user);
        }

        if (!file.isEmpty()) {
            try {
                CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                url = futureUrl.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        article.setIsAccepted(null);

        ArticleDto dto = modelMapper.map(articleRepository.save(article), ArticleDto.class);

        if (!file.isEmpty()) {
            imageService.saveImageOnDB(url, article);
        }

        return dto;
    }

    @Override
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
        String url = "";
        // Controlla se l'articolo esiste
        if (articleRepository.existsById(key)) {
            // Imposta l'ID dell'articolo da aggiornare
            updatedArticle.setId(key);
            // Ottieni l'articolo originale
            Article article = articleRepository.findById(key).get();
            // Imposta l'utente dall'articolo originale
            updatedArticle.setUser(article.getUser());

            boolean hasChanges = false;

            // Gestisci le modifiche delle immagini
            if (!file.isEmpty()) {
                try {
                    // Elimina la vecchia immagine se esiste
                    if (article.getImage() != null) {
                        imageService.deleteImage(article.getImage().getPath());
                    }
                    // Salva nuova immagine
                    CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                    url = futureUrl.get();
                    imageService.saveImageOnDB(url, updatedArticle);
                    hasChanges = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Mantieni l'immagine originale se non viene fornita una nuova immagine
                updatedArticle.setImage(article.getImage());
            }

            // Controlla le modifiche del testo
            if (!updatedArticle.equals(article)) {
                hasChanges = true;
            }

            // Se vengono rilevate modifiche, inviarle per la revisione
            if (hasChanges) {
                updatedArticle.setIsAccepted(null);
            } else {
                updatedArticle.setIsAccepted(article.getIsAccepted());
            }

            return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }


    @Override
    public void delete(Long key) {
        if (articleRepository.existsById(key)) {
            Article article = articleRepository.findById(key).get();
            try {
                String path = article.getImage().getPath();
                article.getImage().setArticle(null);
                imageService.deleteImage(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            articleRepository.deleteById(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public List<ArticleDto> searchByCategory(Category category){
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findByCategory(category)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }    

    public List<ArticleDto> searchByAuthor(User user){
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.findByUser(user)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }    

    public void setIsAccepted(Boolean result, Long id) {
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    public List<ArticleDto> search(String keyword) {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article: articleRepository.search(keyword)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
}
