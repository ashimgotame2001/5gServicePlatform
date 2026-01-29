package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Nokia NAC Metadata responses
 * Used for OpenID configuration and OAuth authorization server metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NokiaNacMetadataDTO {

    /**
     * Metadata type (object, string, etc.)
     */
    private String type;

    /**
     * Metadata title
     */
    private String title;

    /**
     * Properties map (for object types)
     */
    private Map<String, PropertyDTO> properties;

    /**
     * Required fields (for object types)
     */
    private java.util.List<String> required;

    /**
     * Nested DTO for property definition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PropertyDTO {
        private String type;
        private Integer maxLength;
        private String title;
    }
}
