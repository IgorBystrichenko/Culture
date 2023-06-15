package ru.igorba.Culture.Controllers;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.igorba.Culture.Models.ApiAnswer;
import ru.igorba.Culture.Models.Event;
import ru.igorba.Culture.Repository.EventRepository;
import ru.igorba.Culture.Services.RequestService;
import ru.igorba.Culture.Services.RestService;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    EventRepository eventRepository;
    @Autowired
    RestService rest;

    @GetMapping("/adminList")
    @RolesAllowed("ROLE_ADMIN")
    public String adminList(@RequestParam(required = false, defaultValue = "1") Integer p,
                            @RequestParam(required = false, defaultValue = "false") boolean d, Model model) {
        int l = 9;
        Pageable page = PageRequest.of(p - 1, l);

        Page<Event> eventsPage = eventRepository.findAllByDeleted(d, page);
        List<Event> events = eventsPage.stream().toList();

        LinkedList<Integer> pages = RequestService.getPaginationList(p, eventsPage.getTotalPages());

        model.addAttribute("title", "Измененные");
        model.addAttribute("events", events);
        model.addAttribute("pages", pages);
        model.addAttribute("d", d);
        model.addAttribute("activePage", p);
        return "adminList";
    }

    @GetMapping("/events/{id}/edit")
    @RolesAllowed("ROLE_ADMIN")
    public String editEvent(@PathVariable Long id, Model model) {
        Event event = eventRepository.findEventById(id);
        if (event == null) {

            URI uri = RequestService.getEventRequest(id);

            ApiAnswer answer = rest.getEvents(uri);
            List<Event> events = answer.getData();
            if (!events.isEmpty()) {
                event = events.get(0);
            }
            else {
                event = new Event();
            }
        }

        String title = event.getName();

        model.addAttribute("title", title);
        model.addAttribute("event", event);

        return "editEvent";
    }

    @PostMapping("/events/{id}/edit")
    @RolesAllowed("ROLE_ADMIN")
    public String saveEvent(@PathVariable Long id, @ModelAttribute Event event) {

        Event eventFromLocalDB = eventRepository.findEventById(id);

        if (eventFromLocalDB == null) {
            String uri = "https://opendata.mkrf.ru/v2/events/$";
            String f = "{\"nativeId\":{\"$eq\":\"" + id + "\"}}";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
                    .queryParam("f", f);

            ApiAnswer answer = rest.getEvents(builder.build().toUri());
            List<Event> events = answer.getData();
            if (!events.isEmpty()) {
                eventFromLocalDB = events.get(0);
            }
            else {
                eventFromLocalDB = new Event();
            }
        }

        eventFromLocalDB.setName(event.getName());
        eventFromLocalDB.setDescription(event.getDescription());

        if (eventRepository.existsEventById(id)) {
            eventRepository.save(eventFromLocalDB);
        }
        else {
            eventRepository.insert(eventFromLocalDB);
        }
        return "redirect:/adminList";
    }

    @PostMapping ("/events/{id}/delete")
    @RolesAllowed("ROLE_ADMIN")
    public String deleteEvent(@PathVariable Long id) {
        Event event = new Event();
        if (eventRepository.existsEventById(id)) {
            event = eventRepository.findEventById(id);
        }
        else {
            List<Event> list = rest.getEvents(RequestService.getEventRequest(id)).getData();
            if (!list.isEmpty()) {
                event = list.get(0);
            }
        }
        event.setDeleted(true);
        eventRepository.save(event);
        return "redirect:/events/" + id;
    }

    @PostMapping ("/events/{id}/restore")
    @RolesAllowed("ROLE_ADMIN")
    public String restoreEvent(@PathVariable Long id) {
        Event event = new Event();
        if (eventRepository.existsEventById(id)) {
            event = eventRepository.findEventById(id);
        }
        else {
            List<Event> list = rest.getEvents(RequestService.getEventRequest(id)).getData();
            if (!list.isEmpty()) {
                event = list.get(0);
            }
        }
        event.setDeleted(false);
        eventRepository.save(event);
        return "redirect:/events/" + id;
    }

    @PostMapping ("/events/{id}/reset")
    @RolesAllowed("ROLE_ADMIN")
    public String resetEvent(@PathVariable Long id) {
        if (eventRepository.existsEventById(id)) {
            eventRepository.deleteEventById(id);
        }
        return "redirect:/events/" + id;
    }
}
