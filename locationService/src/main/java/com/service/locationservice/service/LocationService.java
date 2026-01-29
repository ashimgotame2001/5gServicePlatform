package com.service.locationservice.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.LocationRetrievalDTO;
import com.service.shared.dto.request.LocationVerificationDto;

public interface LocationService {

    /**
     * Verify if a device is at a specific location/area
     * 
     * @param request Location verification request with area definition
     * @param version API version (v1, v2, v3)
     * @return Verification result
     */
    GlobalResponse verifyLocation(LocationVerificationDto request, String version);

    /**
     * Retrieve current location of a device
     * 
     * @param request Location retrieval request
     * @return Device location information
     */
    GlobalResponse retrieveLocation(LocationRetrievalDTO request);
}
