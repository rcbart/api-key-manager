package com.apikeymanager.controller;

import com.apikeymanager.dto.ApiKeySummaryResponse;
import com.apikeymanager.dto.CreateApiKeyRequest;
import com.apikeymanager.dto.CreateApiKeyResponse;
import com.apikeymanager.dto.UpdateApiKeyRequest;
import com.apikeymanager.service.ApiKeyService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Every endpoint here requires a valid admin JWT -- see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/keys")
public class ApiKeyAdminController {

    private final ApiKeyService apiKeyService;

    public ApiKeyAdminController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateApiKeyResponse create(@Valid @RequestBody CreateApiKeyRequest request, Principal principal) {
        return apiKeyService.create(request, principal.getName());
    }

    @GetMapping
    public List<ApiKeySummaryResponse> list() {
        return apiKeyService.listAll();
    }

    @GetMapping("/{id}")
    public ApiKeySummaryResponse getOne(@PathVariable UUID id) {
        return apiKeyService.getOne(id);
    }

    @PatchMapping("/{id}")
    public ApiKeySummaryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateApiKeyRequest request) {
        return apiKeyService.update(id, request);
    }

    @PostMapping("/{id}/revoke")
    public ApiKeySummaryResponse revoke(@PathVariable UUID id) {
        return apiKeyService.revoke(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        apiKeyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
