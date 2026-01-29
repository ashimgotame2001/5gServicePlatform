package com.service.shared.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CheckDeviceSwap extends DeviceDTO{
     private int maxAge;
}
