package com.jobmailai.backend.config;

import com.jobmailai.backend.entity.User;
import com.jobmailai.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String pictureUrl = oAuth2User.getAttribute("picture");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setName(name);
            user.setPictureUrl(pictureUrl);
            user.setGoogleId(googleId);
        } else {
            user = new User();
            user.setName(name);
            user.setPictureUrl(pictureUrl);
            user.setGoogleId(googleId);
            user.setEmail(email);
        }

        userRepository.save(user);
        System.out.println("User saved/updated in DB: " + user.getEmail());

        String jwtToken = jwtService.generateToken(authentication);
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/auth/token") // Point to our own backend
                .queryParam("token", jwtToken)
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
