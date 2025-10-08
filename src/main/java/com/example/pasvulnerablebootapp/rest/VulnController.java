package com.example.pasvulnerablebootapp.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
public class VulnController {

    /**
     * VULNERABLE endpoint (XSS)
     *
     * Example: GET /vuln/xss?input=<script>alert('xss')</script>
     *
     * This method reflects raw user input into an HTML response without encoding => XSS.
     * Only include this in a safe testing environment.
     */
    @GetMapping(path = "/vuln/xss")
    public ResponseEntity<String> vulnerableXss(@RequestParam(name = "input", required = false, defaultValue = "") String input) {
        String body = "<!doctype html>\n" +
                "<html>\n" +
                "  <head><meta charset=\"utf-8\"><title>XSS Demo</title></head>\n" +
                "  <body>\n" +
                "    <h1>Vulnerable XSS Example</h1>\n" +
                "    <p>User input:</p>\n" +
                "    <div>" + input + "</div>\n" +
                "  </body>\n" +
                "</html>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * SAFE endpoint (output-encoded)
     *
     * Example: GET /vuln/xss-safe?input=<script>alert('xss')</script>
     *
     * This encodes the input before embedding it into HTML, mitigating reflected XSS.
     */
    @GetMapping(path = "/vuln/xss-safe")
    public ResponseEntity<String> safeXss(@RequestParam(name = "input", required = false, defaultValue = "") String input) {
        // Use Spring's HtmlUtils to HTML-escape user input
        String escaped = HtmlUtils.htmlEscape(input);

        String body = "<!doctype html>\n" +
                "<html>\n" +
                "  <head><meta charset=\"utf-8\"><title>XSS Demo (Safe)</title></head>\n" +
                "  <body>\n" +
                "    <h1>Safe XSS Example</h1>\n" +
                "    <p>User input (escaped):</p>\n" +
                "    <div>" + escaped + "</div>\n" +
                "  </body>\n" +
                "</html>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
