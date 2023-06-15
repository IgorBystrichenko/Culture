package ru.igorba.Culture.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Filter {
    private String from;
    private String to;
    private String city;

    private String getRawData(String data) {
        int dd, mm, yyyy;
        String[] datas = data.split("-");
        yyyy = Integer.parseInt(datas[0]);
        mm = Integer.parseInt(datas[1]);
        dd = Integer.parseInt(datas[2]);
        return String.format("%d-%02d-%02d", yyyy, mm, dd);
    }

    public String getRawFrom() {
        if (from == null || from.equals("")) return null;
        return this.getRawData(from) + "T00:00:00.000Z";
    }

    public String getRawTo() {
        if (to == null || to.equals("")) return null;
        return getRawData(to) + "T23:59:59.999Z";
    }
}
