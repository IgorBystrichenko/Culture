package ru.igorba.Culture.Services;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.igorba.Culture.Models.ApiAnswer;
import ru.igorba.Culture.Models.Event;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@Service
public class RestService {
    private final RestTemplate rest;
    private final HttpEntity<String> entity;

    public RestService(Environment env) {

        rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", env.getProperty("X-API-KEY"));
        MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
        headers.setContentType(mediaType);
        entity = new HttpEntity<>("", headers);
    }

    public ApiAnswer getEvents(URI uri) {

        ResponseEntity<String> formEntity = rest.exchange(uri, HttpMethod.GET, entity, String.class);
        JSONObject jAnswer = (JSONObject) JSONValue.parse(formEntity.getBody());

        Integer total = (Integer) jAnswer.get("total");
        if (total == null) total = 0;

        JSONArray jArray = (JSONArray) jAnswer.get("data");
        List<Event> list = jArray.stream().map(a -> {
            try {
                return getEventByJSONObject((JSONObject) a);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).toList();


        ApiAnswer answer = new ApiAnswer();
        answer.setTotal(total);
        answer.setData(list);
        return answer;
    }

    private static Event getEventByJSONObject(JSONObject object) throws ParseException {
        Event event = new Event();
        JSONObject data = (JSONObject) object.get("data");
        JSONObject general = (JSONObject) data.get("general");
        event.setId(Long.valueOf((String) object.get("nativeId")));

        JSONObject category = (JSONObject) general.get("category");
        event.setCategory((String) category.get("name"));

        event.setFree((Boolean) general.get("isFree"));
        if (!event.isFree())
        {
            event.setPrice((Integer) general.get("price"));
        }

        event.setName((String) general.get("name"));
        event.setOrganizer((String) general.get("organizer"));
        event.setShortDescription((String) general.get("shortDescription"));
        event.setDescription((String) general.get("description"));


        String[] month = {"января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        event.setRawStart((String) general.get("start"));
        event.setRawEnd((String) general.get("end"));
        start.setTime(format.parse((String) general.get("start")));
        end.setTime(format.parse((String) general.get("end")));
        event.setStart(String.format("%d %s %d",
                start.get(Calendar.DAY_OF_MONTH),
                month[start.get(Calendar.MONTH)],
                start.get(Calendar.YEAR)));
        event.setEnd(String.format("%d %s %d",
                end.get(Calendar.DAY_OF_MONTH),
                month[end.get(Calendar.MONTH)],
                end.get(Calendar.YEAR)));

        JSONObject image = (JSONObject) general.get("image");
        event.setImageUrl((String) image.get("url"));

        JSONArray gallery = (JSONArray) general.get("gallery");
        if (gallery != null && !gallery.isEmpty()) {
            List<String> galleryList = new ArrayList<>();
            gallery.forEach(img -> galleryList.add(((JSONObject)img).get("url").toString()));
            event.setGallery(galleryList);
        }

        JSONArray seances = (JSONArray) general.get("seances");
        if (seances != null && !seances.isEmpty()) {
            List<String> seancesList = new ArrayList<>();
            seances.forEach(s -> seancesList.add(((JSONObject)s).get("start").toString()));
            event.setSeances(seancesList);
        }

        return event;
    }

}
