package it.aulab.progetto_finale_docente.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NotificationInterceptor implements HandlerInterceptor {

    @Autowired
    CareerRequestRepository careerRequestRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Override
    public void postHandle(@SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") Object handler, @SuppressWarnings("null") ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && request.isUserInRole("ROLE_ADMIN")) {
            int careerCount = careerRequestRepository.findByIsCheckedFalse().size();
            modelAndView.addObject("careerRequests", careerCount);
        }

        if (modelAndView != null && request.isUserInRole("ROLE_REVISOR")) {
            int revisedCount = articleRepository.findByIsAcceptedIsNull().size();
            modelAndView.addObject("articlesToBeRevised", revisedCount);
        }
    }
}
