package com.example.auth.controller;

import com.example.auth.dto.OAuth2TokenRequest;
import com.example.auth.dto.OAuth2TokenResponse;
import com.example.auth.service.OAuth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OAuth2TokenController {

    private final OAuth2ClientService oAuth2ClientService;

    // NHẬN form-urlencoded (chuẩn client_credentials)
    @PostMapping(
            value = "/oauth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OAuth2TokenResponse> token(@RequestParam MultiValueMap<String, String> form) {
        OAuth2TokenRequest req = OAuth2TokenRequest.builder()
                .grantType(form.getFirst("grant_type"))
                .clientId(form.getFirst("client_id"))
                .clientSecret(form.getFirst("client_secret"))
                .scope(form.getFirst("scope"))
                .audience(form.getFirst("audience"))
                .build();

        OAuth2TokenResponse result = oAuth2ClientService.generateServiceToken(req);
        return ResponseEntity.ok(result);
    }

    // (TÙY CHỌN) Giữ thêm bản nhận JSON để tương thích ngược
    @PostMapping(
            value = "/oauth/token",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OAuth2TokenResponse> tokenJson(@RequestBody OAuth2TokenRequest req) {
        OAuth2TokenResponse result = oAuth2ClientService.generateServiceToken(req);
        return ResponseEntity.ok(result);
    }
}