package ru.igorba.Culture.Models;

import lombok.Data;
import java.util.List;

@Data
public class ApiAnswer {
    private List<Event> data;
    private int total = 0;
}
