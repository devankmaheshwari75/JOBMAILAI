package com.jobmailai.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/token")
public class TokenController {
    @GetMapping
    public String displayToken(@RequestParam("token") String token) {
        // We'll return some basic HTML to make the token easy to copy from the browser.
        return "<html><body>" +
                "<h1>Login Successful!</h1>" +
                "<h2>Your JWT Token:</h2>" +
                "<textarea rows='10' cols='100' readonly>" + token + "</textarea>" +
                "</body></html>";
    }
}
