package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.dtos.CategoryDto;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Category;
import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.services.ArticleService;
import it.aulab.progetto_finale_docente.services.CrudService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    @Qualifier("categoryService")
    private CrudService<CategoryDto, Category, Long> categoryService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Rotta index degli articoli
    @GetMapping
    public String articleIndex(Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli");

        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }

        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        viewModel.addAttribute("articles", articles);

        return "article/articles";
    }

    // Rotta per la creazione di un articolo
    @GetMapping("/create")
    public String articleCreate(Model viewModel) {
        viewModel.addAttribute("title", "Crea un articolo");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/create";
    }

    // Rotta per lo store di un articolo
    @PostMapping
    public String articleStore(@Valid @ModelAttribute("article") Article article,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Principal principal,
                              MultipartFile file,
                              Model viewModel) {

        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Crea un articolo");
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/create";
        }

        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo aggiunto con successo!");

        return "redirect:/";
    }

    //Rotta di dettaglio di un articolo
    @GetMapping("detail/{id}")
    public String detailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "article/detail";
    }

    //Rotta di modifica di un articolo
    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article update");
        viewModel.addAttribute("article", articleService.read(id));
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    //Rotta di memorizzazione modifica di un articolo
    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable("id")Long id, @Valid @ModelAttribute("article") Article article, BindingResult result, RedirectAttributes redirectAttributes, Principal principal, MultipartFile file, Model viewModel) {

        //Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Article update");
            article.setImage(articleService.read(id).getImage());
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }

        articleService.update(id, article, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo modificato con successo!");

        return "redirect:/articles";
    }

    //Rotta per la cancellazione di un articolo
    
    @GetMapping("/delete/{id}")
    public String articleDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo cancellato con successo!");
        return "redirect:/writer/dashboard";
    }

    //Rotta dettaglio di un articolo per il revisore
    @GetMapping("revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "revisor/detail";
    }

    //Rotta dedicata all'azione del revisore
    @PostMapping("/accept")
    public String articleSetAccepted(@RequestParam("action") String action, @RequestParam("articleId") Long articleId, RedirectAttributes redirectAttributes) {
        if (action.equals("accept")) {
            articleService.setIsAccepted(true, articleId);
            redirectAttributes.addFlashAttribute("successMessage", "Articolo accettato con successo!");
        } else if (action.equals("reject")) {
            articleService.setIsAccepted(false, articleId);
            redirectAttributes.addFlashAttribute("successMessage", "Articolo rifiutato!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Operazione non valida!");
        }
        
        return "redirect:/revisor/dashboard";

    }

    //Rotta di ricerca di un articolo
    @GetMapping("/search")
    public String articleSearch(@Param("keyword") String keyword, Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli trovati");
        List<ArticleDto> articles = articleService.search(keyword);

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }
}
