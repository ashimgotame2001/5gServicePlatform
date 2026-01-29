package com.service.shared.controller;

import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.service.NokiaNacMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Nokia NAC Metadata endpoints
 * Provides access to OpenID configuration and OAuth authorization server metadata
 */
@RestController
@RequestMapping("/nokia-nac/metadata")
@RequiredArgsConstructor
public class NokiaNacMetadataController {

    private final NokiaNacMetadataService nokiaNacMetadataService;

    @GetMapping("/openid-configuration")
    @MethodCode(value = "NM001", description = "Get OpenID configuration metadata")
    public ResponseEntity<GlobalResponse> getOpenIdConfiguration() {
        return ResponseEntity.ok(nokiaNacMetadataService.getOpenIdConfiguration());
    }

    @GetMapping("/security.txt")
    @MethodCode(value = "NM002", description = "Get security.txt")
    public ResponseEntity<GlobalResponse> getSecurityTxt() {
        return ResponseEntity.ok(nokiaNacMetadataService.getSecurityTxt());
    }

    @GetMapping("/oauth-authorization-server")
    @MethodCode(value = "NM003", description = "Get OAuth authorization server metadata")
    public ResponseEntity<GlobalResponse> getOAuthAuthorizationServer() {
        return ResponseEntity.ok(nokiaNacMetadataService.getOAuthAuthorizationServer());
    }
}
