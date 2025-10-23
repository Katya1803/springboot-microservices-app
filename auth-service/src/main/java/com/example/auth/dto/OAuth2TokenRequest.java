package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2TokenRequest {

    @NotBlank(message = "Grant type is required")
    @JsonProperty("grant_type")
    private String grantType; // "client_credentials"

    @NotBlank(message = "Client ID is required")
    @JsonProperty("client_id")
    private String clientId;

    @NotBlank(message = "Client secret is required")
    @JsonProperty("client_secret")
    private String clientSecret;

    private String scope;

    private String audience; // Target service name
}