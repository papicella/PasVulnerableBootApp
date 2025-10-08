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

    /**
     * VULNERABLE endpoint (HardCodedSecret)
     *
     * Example: GET /vuln/secret
     *
     * This demonstrates a secret embedded in source code and returned in an HTTP response.
     * DO NOT commit secrets to source control or return them in responses in real apps.
     */
    @GetMapping(path = "/vuln/secret")
    public ResponseEntity<String> vulnerableHardCodedSecret() {
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
     * SAFER endpoint (no hard-coded secret)
     *
     * Example: GET /vuln/secret-safe
     *
     * Safer approaches:
     *  - don't store secrets in source control
     *  - store secrets in environment variables, secret managers (Vault, AWS Secrets Manager, Kubernetes secrets)
     *  - never return the secret in API responses; return only existence/health or masked values if needed
     */
    @Value("${external.api.key:}")  // try to inject from configuration/environment; defaults to empty
    private String externalApiKey;

    @GetMapping(path = "/vuln/secret-safe")
    public ResponseEntity<String> safeHardCodedSecret() {
        // Best practice: do not reveal the secret. Only indicate whether it's available.
        String body;
        if (externalApiKey == null || externalApiKey.isEmpty()) {
            body = "<!doctype html>\n" +
                    "<html>\n" +
                    "  <head><meta charset=\"utf-8\"><title>Secret (Safe)</title></head>\n" +
                    "  <body>\n" +
                    "    <h1>Safe Secret Handling</h1>\n" +
                    "    <p>No API key configured.</p>\n                   </body>\n" +
                    "</html>";
        } else {
            // Mask the secret when displaying to avoid leaking it (e.g., for debugging)
            String masked = maskSecret(externalApiKey);
            body = "<!doctype html>\n" +
                    "<html>\n" +
                    "  <head><meta charset=\"utf-8\"><title>Secret (Safe)</title></head>\n" +
                    "  <body>\n" +
                    "    <h1>Safe Secret Handling</h1>\n" +
                    "    <p>API key is configured. Value (masked):</p>\n" +
                    "    <pre>" + masked + "</pre>\n" +
                    "  </body>\n" +
                    "</html>";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            return "********";
        }
        int keep = 4;
        String start = secret.substring(0, keep);
        String end = secret.substring(secret.length() - keep);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < secret.length() - (keep * 2); i++) {
            stars.append('*');
        }
        return start + stars + end;
    }

    /**
     * VULNERABLE endpoint (Broken Access Control)
     *
     * Example: GET /vuln/admin-data?userId=bob
     *
     * This endpoint returns sensitive account information for any requested userId
     * without performing authentication or authorization checks. An attacker can
     * enumerate or access arbitrary user data simply by changing the userId parameter.
     */
    @GetMapping(path = "/vuln/admin-data")
    public ResponseEntity<String> brokenAccessControl(@RequestParam(name = "userId", required = false, defaultValue = "alice") String userId) {
        // Simulated sensitive data (DO NOT expose real PII in responses)
        String simulatedSensitiveData = String.format(
                "Account summary for user '%s':\n" +
                        "- fullName: %s\n" +
                        "- email: %s@example.com\n" +
                        "- balance: $%s\n" +
                        "- ssn: %s\n",
                userId,
                userId.substring(0, 1).toUpperCase() + userId.substring(1),
                userId,
                "10,000.00",
                "123-45-6789"
        );

        String body = "<!doctype html>\n" +
                "<html>\n" +
                "  <head><meta charset=\"utf-8\"><title>Broken Access Control Demo</title></head>\n" +
                "  <body>\n" +
                "    <h1>Broken Access Control (Vulnerable)</h1>\n" +
                "    <p>Requested userId: " + userId + "</p>\n" +
                "    <pre>" + simulatedSensitiveData + "</pre>\n" +
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
    @GetMapping(path = "/vuln/command")
    public ResponseEntity<String> commandInjection(@RequestParam(name = "cmd", required = false, defaultValue = "date") String cmd) throws Exception {
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
