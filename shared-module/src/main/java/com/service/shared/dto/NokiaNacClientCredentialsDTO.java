package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Nokia NAC client credentials response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NokiaNacClientCredentialsDTO {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;
}
