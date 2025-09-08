package com.ist.idp.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;

@Service
@RequiredArgsConstructor
public class LinkedInOAuthService {

    private final RestTemplate restTemplate;

    @Value("${linkedin.api.client-id}")
    private String clientId;
    @Value("${linkedin.api.client-secret}")
    private String clientSecret;
    @Value("${linkedin.api.redirect-uri}")
    private String redirectUri;
    @Value("${linkedin.api.token-uri}")
    private String tokenUri;
    @Value("${linkedin.api.user-info-uri}")
    private String userInfoUri;


    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", redirectUri);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        LinkedInTokenResponse response = restTemplate.postForObject(tokenUri, request, LinkedInTokenResponse.class);

        return response != null ? response.accessToken() : null;
    }

     //Fetches user details from LinkedIn using the access token.
    public LinkedInUserDetails getUserDetails(String accessToken) {
        String url = userInfoUri;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, LinkedInUserDetails.class).getBody();
    }

    private record LinkedInTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("scope") String scope
    ) {}

    public record LinkedInUserDetails(
            String sub,
            String name,
            String picture,
            String email,
            @JsonProperty("email_verified") boolean emailVerified
    ) implements Serializable {}
}