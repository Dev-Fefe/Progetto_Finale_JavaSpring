package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;
import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.dtos.UserDto;
import it.aulab.progetto_finale_docente.services.ArticleService;
import it.aulab.progetto_finale_docente.services.UserService;
import it.aulab.progetto_finale_docente.services.CategoryService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private ModelMapper modelMapper;

    //Rotta di home
    @GetMapping("/")
    public String home(Model viewModel) {

        //Recupero tutti gli articoli accettati
        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for(Article article : articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }

        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());

        List<ArticleDto> lastThreeArticles = articles.stream().limit(3).collect(Collectors.toList());

        viewModel.addAttribute("articles", lastThreeArticles);

        return "home";
    }

    //Rotta registrazione
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }   
    
    //Rotta login
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    //Rotta per il salvataggio della registrazione
    @SuppressWarnings("null")
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        } 

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "auth/register";
        }

        userService.saveUser(userDto, redirectAttributes, request, response);

        redirectAttributes.addFlashAttribute("successMessage", "Registrazione avvenuta!");

        return "redirect:/";
    }

    //Rotta per la ricerca degli articoli in base all'utente
    @GetMapping("/search/{id}")
    public String userArticlesSearch(@PathVariable("id") Long id, Model viewModel) {
        User user = userService.find(id);
        viewModel.addAttribute("title", "Tutti gli articoli trovati per utente" + user.getUsername());

        List<ArticleDto> articles = articleService.searchByAuthor(user);

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);
        
        return "article/articles";
    }

    //Rotta per la dashboard dell'admin
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model viewModel) {
        viewModel.addAttribute("title", "Richieste ricevute");
        viewModel.addAttribute("requests", careerRequestRepository.findByIsCheckedFalse());

        viewModel.addAttribute("categories", categoryService.readAll());
        return "admin/dashboard";
    }

    //Rotta per la dashboard del revisor
    @GetMapping("/revisor/dashboard")
    public String revisorDashboard(Model viewModel) {
        viewModel.addAttribute("title", "Articoli da revisionare");
        viewModel.addAttribute("articles", articleRepository.findByIsAcceptedIsNull());

        return "revisor/dashboard";
    }

    //Rotta per la dashboard del writer
    @GetMapping("/writer/dashboard")
    public String writerDashboard(Model viewModel, Principal principal) {
        viewModel.addAttribute("title", "I tuoi articoli");
        List<ArticleDto> userArticles = articleService.readAll().stream().filter(article -> article.getUser().getEmail().equals(principal.getName())).toList();
        viewModel.addAttribute("articles", userArticles);

        return "writer/dashboard";
    }
}
