package com.example.pasvulnerablebootapp.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
public class NewVulnControllerPR {

    /**
     * VULNERABLE endpoint (XSS)
     *
     * Example: GET /vuln/xss?input=<script>alert('xss')</script>
     *
     * This method reflects raw user input into an HTML response without encoding => XSS.
     * Only include this in a safe testing environment.
     */
    @GetMapping(path = "/vuln/xss2")
    public ResponseEntity<String> vulnerableXss2(@RequestParam(name = "input", required = false, defaultValue = "") String input) {
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
     * VULNERABLE endpoint (HardCodedSecret)
     *
     * Example: GET /vuln/secret
     *
     * This demonstrates a secret embedded in source code and returned in an HTTP response.
     * DO NOT commit secrets to source control or return them in responses in real apps.
     */
    @GetMapping(path = "/vuln/secret2")
    public ResponseEntity<String> vulnerableHardCodedSecret2() {
        // <-- BAD: hard-coded secret in source code (example value only)
        String hardCodedApiKey = "AKIAEXAMPLEHARDCODEDKEY123456";

        String body = "<!doctype html>\n" +
                "<html>\n" +
                "  <head><meta charset=\"utf-8\"><title>Hardcoded Secret Demo</title></head>\n" +
                "  <body>\n" +
                "    <h1>Vulnerable Hard-Coded Secret</h1>\n" +
                "    <p>API Key (DO NOT do this):</p>\n                      <pre>" + hardCodedApiKey + "</pre>\n" +
                "  </body>\n" +
                "</html>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * VULNERABLE endpoint (Command Injection)
     *
     * Example: GET /vuln/command?cmd=date
     *
     * This method takes a user-supplied 'cmd' parameter and executes it directly on the host
     * using Runtime.exec(). Because input is executed as a shell command without validation
     * or sanitization, an attacker can run arbitrary commands on the server.
     *
     * DO NOT use this pattern in real applications.
     */
    @GetMapping(path = "/vuln/command2")
    public ResponseEntity<String> commandInjection2(@RequestParam(name = "cmd", required = false, defaultValue = "date") String cmd) throws Exception {
        StringBuilder output = new StringBuilder();
        try {
            // <-- HIGH RISK: executing raw user input as a system command
            Process proc = Runtime.getRuntime().exec(cmd);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            // also capture stderr (optional)
            try (BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String line;
                while ((line = errReader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // wait for process to finish (avoid zombies)
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
            Thread.currentThread().interrupt();
        }

        String body = "<!doctype html>\n" +
                "<html>\n" +
                "  <head><meta charset=\"utf-8\"><title>Command Injection Demo</title></head>\n" +
                "  <body>\n" +
                "    <h1>Command Injection (VULNERABLE)</h1>\n" +
                "    <p>Executed command: <code>" + HtmlUtils.htmlEscape(cmd) + "</code></p>\n" +
                "    <pre>" + HtmlUtils.htmlEscape(output.toString()) + "</pre>\n" +
                "  </body>\n" +
                "</html>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
