package ru.igorba.Culture.Models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Events")
public class Event {
    private Long id;

    private String category;

    private boolean isFree;
    private int price = 0;

    private String name = "Null";
    private String organizer;
    private String shortDescription;
    private String description;
    private int ageRestriction;

    private String start;
    private String end;
    private String rawStart;
    private String rawEnd;

    private Set<String> tags;

    private String imageUrl;

    private String organizationName;
    private String subordination;
    private String locale;

    private List<String> gallery = new ArrayList<>();
    private List<String> seances = new ArrayList<>();
    private boolean deleted = false;

    public String getNextSeance(String from) throws ParseException {
        if (from == null) return this.start;
        SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat seanceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar seance = Calendar.getInstance();
        if (!seances.isEmpty()) {
            for (String s : seances) {
                if (fromFormat.parse(from).before(seanceFormat.parse(s))) {
                    seance.setTime(seanceFormat.parse(s));
                    break;
                }
            }
        }

        return parseDate(seance);
    }
    public String parseDate(Calendar date) {
        String[] month = {"января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        return String.format("%d %s %d",
                date.get(Calendar.DAY_OF_MONTH),
                month[date.get(Calendar.MONTH)],
                date.get(Calendar.YEAR));

    }

}
