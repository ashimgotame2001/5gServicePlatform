package com.service.shared.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateSessionRequestDTO {
    private DeviceInfoDTO device;
    private ApplicationServerDTO applicationServer;
    private String qosProfile;
    private Integer duration;
}
