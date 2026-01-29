package com.service.shared.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceDetailDTO extends DeviceInfoDTO{
    String networkAccessIdentifier;
    String ipv6Address;
}
