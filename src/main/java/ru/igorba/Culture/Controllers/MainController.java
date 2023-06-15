package ru.igorba.Culture.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.igorba.Culture.Models.ApiAnswer;
import ru.igorba.Culture.Models.Event;
import ru.igorba.Culture.Models.Filter;
import ru.igorba.Culture.Repository.EventRepository;
import ru.igorba.Culture.Services.RequestService;
import ru.igorba.Culture.Services.RestService;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Controller
public class MainController {
    @Autowired
    RestService rest;
    @Autowired
    EventRepository eventRepository;

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "1") Integer p,
                       @ModelAttribute Filter filter, Model model) {
        int l = 9;

        List<Event> nin = eventRepository.findAllByDeleted(true);

        URI uri = RequestService.getFilterRequest(filter, nin, p, l);
        ApiAnswer answer = rest.getEvents(uri);
        List<Event> events = new ArrayList<>(answer.getData());
        List<Event> edited = eventRepository.findAll();
        for (int i = 0; i < events.size(); ++i) {

            for (Event event : edited) {
                if (Objects.equals(events.get(i).getId(), event.getId())) {
                    events.set(i, event);
                }
            }
            try {
                events.get(i).setStart(events.get(i).getNextSeance(filter.getRawFrom()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        int lastP = answer.getTotal() / l + 1;
        LinkedList<Integer> pages = RequestService.getPaginationList(p, lastP);

        model.addAttribute("title", "Главная страница");
        model.addAttribute("events", events);
        model.addAttribute("pages", pages);
        model.addAttribute("activePage", p);
        model.addAttribute("filter", filter);
        return "home";
    }

    @PostMapping("/")
    public String filterSubmit(@RequestParam(required = false, defaultValue = "1") Integer p,
                               @ModelAttribute Filter filter, RedirectAttributes attributes) {
        attributes.addFlashAttribute("filter", filter);
        return "redirect:/?p=" + p;
    }

}
