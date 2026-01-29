package com.service.shared.dto.request;

import com.service.shared.dto.AreaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LocationVerificationDto extends LocationRetrievalDTO {
    private AreaDTO area;
}
