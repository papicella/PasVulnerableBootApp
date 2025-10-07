package com.example.pasvulnerablebootapp.web;

import com.example.pasvulnerablebootapp.rest.GetStock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class StockController {

    private final GetStock getStock;

    public StockController(GetStock getStock) {
        this.getStock = getStock;
    }

    @GetMapping("/findstock")
    public String stockPage(@RequestParam(name = "symbol", required = false) String symbol, Model model) {
        model.addAttribute("symbol", symbol);
        if (symbol != null && !symbol.trim().isEmpty()) {
            try {
                ResponseEntity<?> response = getStock.restPage(symbol);
                model.addAttribute("stockResult", response.getBody());
                model.addAttribute("stockStatus", response.getStatusCode().value());
            } catch (Exception ex) {
                model.addAttribute("stockResult", null);
                model.addAttribute("stockStatus", 500);
                model.addAttribute("stockError", ex.getMessage());
            }
        }
        return "stock";
    }
}
