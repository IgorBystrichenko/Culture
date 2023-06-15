package ru.igorba.Culture.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.igorba.Culture.Models.ApiAnswer;
import ru.igorba.Culture.Models.Event;
import ru.igorba.Culture.Models.User;
import ru.igorba.Culture.Repository.EventRepository;
import ru.igorba.Culture.Repository.UserRepository;
import ru.igorba.Culture.Services.MongoAuthUserDetailService;
import ru.igorba.Culture.Services.RequestService;
import ru.igorba.Culture.Services.RestService;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Controller
public class EventController {
    @Autowired
    RestService rest;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    private MongoAuthUserDetailService userService;

    @GetMapping("/events/{id}")
    public String event(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findUserByUsername(auth.getName());
        String title = "Null";
        Event event = new Event();
        boolean isFavorite = false;
        boolean isEdited = false;
        boolean isDeleted = false;

        List<Event> nin = eventRepository.findAll();

        if (nin.stream().anyMatch(e -> Objects.equals(e.getId(), id))) {
            event = nin.stream().filter(e -> Objects.equals(e.getId(), id)).toList().get(0);
            title = event.getName();
            isDeleted = event.isDeleted();
            if (!isDeleted) {
                isEdited = true;
            }
        }
        else {
            URI uri = RequestService.getEventRequest(id);
            ApiAnswer answer = rest.getEvents(uri);
            List<Event> events = answer.getData();
            if (!events.isEmpty()) {
                title = events.get(0).getName();
                event = events.get(0);
                if (currentUser != null) {
                    isFavorite = currentUser.getFavorite().contains(event.getId());
                }
            }
        }

        model.addAttribute("title", title);
        model.addAttribute("event", event);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("isEdited", isEdited);
        model.addAttribute("isDeleted", isDeleted);

        return "event";
    }

    @PostMapping("/addFavorite")
    public String addFavorite(@ModelAttribute Event event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findUserByUsername(auth.getName());
        currentUser.addFavorite(event.getId());
        userRepository.save(currentUser);
        return "redirect:/events/" + event.getId();
    }

    @PostMapping("/deleteFavorite")
    public String deleteFavorite(@ModelAttribute Event event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findUserByUsername(auth.getName());
        currentUser.deleteFavorite(event.getId());
        userRepository.save(currentUser);
        return "redirect:/events/" + event.getId();
    }

    @GetMapping("/favorite")
    public String favoritePage (@RequestParam(required = false, defaultValue = "1") Integer p, Model model) {
        int l = 9;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());

        List<Event> nin = eventRepository.findAllByDeleted(true);

        URI uri = RequestService.getFavoriteRequest(currentUser.getFavorite(), nin, p, l);
        ApiAnswer answer = rest.getEvents(uri);

        int lastP = answer.getTotal() / l + 1;

        LinkedList<Integer> pages = RequestService.getPaginationList(p, lastP);

        model.addAttribute("title", "Избранные");
        model.addAttribute("events", answer.getData());
        model.addAttribute("pages", pages);
        model.addAttribute("activePage", p);
        return "favorite";
    }
}
