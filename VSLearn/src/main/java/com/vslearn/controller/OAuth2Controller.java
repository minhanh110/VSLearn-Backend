package com.vslearn.controller;

import com.vslearn.dto.response.ResponseData;
import com.vslearn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/oauth2")
public class OAuth2Controller {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/success")
    public ResponseEntity<?> oauth2Success(OAuth2AuthenticationToken authentication) {
        OAuth2User oauth2User = authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        if (email == null) {
            return ResponseEntity.badRequest()
                .body(ResponseData.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể lấy thông tin email từ Google")
                    .build());
        }
        
        return userService.handleOAuth2Login(email, name);
    }
} 