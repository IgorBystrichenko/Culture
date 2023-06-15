package ru.igorba.Culture.Services;

import org.springframework.web.util.UriComponentsBuilder;
import ru.igorba.Culture.Models.Event;
import ru.igorba.Culture.Models.Filter;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RequestService {
    public static URI getFilterRequest(final Filter filter, final List<Event> nin, final int pagination, final int l) {
        String uri = "https://opendata.mkrf.ru/v2/events/$";
        StringBuilder f = new StringBuilder();
        int s = (pagination - 1) * l;

        HashMap<String, String> params = new HashMap<>();
        if (filter.getRawTo() != null) {
            params.put("data.general.start", String.format("\"$lt\":\"%s\"", filter.getRawTo()));
        }
        if (filter.getRawFrom() != null) {
            params.put("data.general.end", String.format("\"$gt\":\"%s\"", filter.getRawFrom()));
        }
        if (filter.getCity() != null && !filter.getCity().equals("")) {
            params.put("data.general.organization.name", String.format("\"$search\":\"%s\"", filter.getCity()));
        }
        if (!nin.isEmpty()) {
            String n = String.join(",", nin.stream().map(event -> event.getId().toString()).toList());
            params.put("nativeId", String.format("\"$nin\":[%s]", n));
        }
        if (!params.isEmpty()) {
            String fs = String.join(",", params.entrySet().stream()
                    .map(param -> String.format("\"%s\":{%s}", param.getKey(), param.getValue()))
                    .toList());
            f.append('{');
            f.append(fs);
            f.append('}');
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("f", f)
                .queryParam("s", s)
                .queryParam("l", l);
        return builder.build().encode(StandardCharsets.UTF_8).toUri();
    }
    public static URI getFavoriteRequest(final Set<Long> in, final List<Event> nin, final int pagination, final int l) {
        String uri = "https://opendata.mkrf.ru/v2/events/$";
        StringBuilder f = new StringBuilder();
        int s = (pagination - 1) * l;

        HashMap<String, String> idparams = new HashMap<>();
        idparams.put("$in", String.join(",", in.stream().map(Object::toString).toList()));
        idparams.put("$nin", String.join(",", nin.stream().map(event -> event.getId().toString()).toList()));


        String ids = String.join(",", idparams.entrySet().stream()
                .map(param -> String.format("\"%s\":[%s]", param.getKey(), param.getValue()))
                .toList());
        f.append("{\"nativeId\":{");
        f.append(ids);
        f.append("}}");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("f", f)
                .queryParam("s", s)
                .queryParam("l", l);
        return builder.build().toUri();
    }
    public static URI getEventRequest(final Long in) {
        String uri = "https://opendata.mkrf.ru/v2/events/$";
        StringBuilder f = new StringBuilder();

        String ids = String.format("\"$in\":[%s]", in.toString());
        f.append("{\"nativeId\":{");
        f.append(ids);
        f.append("}}");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("f", f);
        return builder.build().toUri();
    }
    public static LinkedList<Integer> getPaginationList(final int p, int lastP) {
        if (lastP == 0) lastP = 1;
        Set<Integer> pagesSet = new HashSet<>();
        pagesSet.add(1);
        pagesSet.add(p);
        pagesSet.add(lastP);
        if (p != 1) pagesSet.add(p - 1);
        if (p != lastP) pagesSet.add(p + 1);
        LinkedList <Integer> pages = pagesSet.stream().sorted().collect(Collectors.toCollection(LinkedList::new));
        if (p > 3) pages.add(1, 0);
        if (p < lastP - 2) pages.add(pages.lastIndexOf(lastP), 0);
        return pages;
    }
}
