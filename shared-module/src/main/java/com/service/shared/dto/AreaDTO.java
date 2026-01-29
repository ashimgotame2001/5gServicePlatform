package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Area definition
 * Used for geofencing, location-based services, and area-based network operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaDTO {

    /**
     * Area type mapping
     * Keys are numeric (0-5), values are area type codes (C, I, R, L, E)
     * C = Circle, I = Irregular, R = Rectangle, L = Line, E = Ellipse
     */
    private Map<String, String> areaType;

    /**
     * Center point of the area
     */
    private CenterDTO center;

    /**
     * Radius information (can be a number or object depending on area type)
     */
    private Object radius;

    /**
     * Nested DTO for center coordinates
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CenterDTO {
        
        /**
         * Latitude coordinate
         */
        private Double latitude;
        
        /**
         * Longitude coordinate
         */
        private Double longitude;
    }
}
