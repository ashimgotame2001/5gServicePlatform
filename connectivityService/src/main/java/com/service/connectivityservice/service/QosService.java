package com.service.connectivityservice.service;


import com.service.shared.dto.request.CreateSessionRequestDTO;
import com.service.shared.dto.request.DeviceRequestDTO;

public interface QosService {

    com.service.shared.dto.GlobalResponse getRetrieveSessions(DeviceRequestDTO request);

   com.service.shared.dto.GlobalResponse createSession(CreateSessionRequestDTO requestDTO);
    com.service.shared.dto.GlobalResponse getSession(String sessionId);
  com.service.shared.dto.GlobalResponse getSessions();

}
