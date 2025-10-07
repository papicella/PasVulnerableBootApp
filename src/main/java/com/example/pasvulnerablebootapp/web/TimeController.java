package com.example.pasvulnerablebootapp.web;

import com.example.pasvulnerablebootapp.rest.GetTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/home")
public class TimeController {

    private final GetTime getTime;

    public TimeController(GetTime getTime) {
        this.getTime = getTime;
    }

    @GetMapping("/findtime")
    public String timePage(@RequestParam(name = "city", required = false) String city, Model model) {
        model.addAttribute("city", city);

        if (city != null && !city.trim().isEmpty()) {
            ResponseEntity<?> response = getTime.getTime(city);
            model.addAttribute("status", response.getStatusCode().value());

            Object body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body instanceof Map<?, ?> map) {
                model.addAttribute("result", map);
                // Format the date for display
                Object datetimeObj = map.get("datetime");
                if (datetimeObj instanceof String datetimeStr) {
                    try {
                        ZonedDateTime zdt = ZonedDateTime.parse(datetimeStr);
                        String dayOfWeek = zdt.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                        int dayOfMonth = zdt.getDayOfMonth();
                        String daySuffix;
                        if (dayOfMonth >= 11 && dayOfMonth <= 13) {
                            daySuffix = "th";
                        } else {
                            switch (dayOfMonth % 10) {
                                case 1: daySuffix = "st"; break;
                                case 2: daySuffix = "nd"; break;
                                case 3: daySuffix = "rd"; break;
                                default: daySuffix = "th";
                            }
                        }
                        String month = zdt.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                        int year = zdt.getYear();
                        String formattedDate = String.format("%s %d%s of %s %d", dayOfWeek, dayOfMonth, daySuffix, month, year);
                        model.addAttribute("formattedDate", formattedDate);
                    } catch (Exception e) {
                        model.addAttribute("formattedDate", "Invalid date");
                    }
                }
            } else {
                model.addAttribute("error", body);
            }
        } else {
            model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
            model.addAttribute("error", Map.of("error", "city parameter is required"));
        }

        return "time"; // forwards to templates/time.html
    }
}
