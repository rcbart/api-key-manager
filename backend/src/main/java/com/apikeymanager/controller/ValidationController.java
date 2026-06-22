package com.apikeymanager.controller;

import com.apikeymanager.dto.ValidateKeyRequest;
import com.apikeymanager.dto.ValidateKeyResponse;
import com.apikeymanager.service.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Called by other services to check whether a key is currently valid. The
 * key itself travels in the {@code X-API-Key} header (never the JSON body,
 * so it's less likely to end up in a body-logging middleware somewhere
 * upstream); an optional JSON body carries {@code requiredScope} and/or an
 * explicit {@code sourceIp} to check against the key's allowlist. If
 * {@code sourceIp} isn't supplied, the request's own remote address is used.
 *
 * This endpoint deliberately returns 200 with {@code valid: false} (not a
 * 4xx) for an invalid/revoked/expired/rate-limited key -- it's answering a
 * question ("is this allowed?"), not asserting that the request itself was
 * malformed. The 429 status is intentionally not used for rate-limiting
 * here, since "the underlying key is rate limited" and "you're calling this
 * validate endpoint too fast" are different things.
 */
@RestController
@RequestMapping("/api/validate")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping
    public ResponseEntity<ValidateKeyResponse> validate(
            @RequestHeader(name = "X-API-Key", required = false) String apiKeyHeader,
            @Valid @RequestBody(required = false) ValidateKeyRequest request,
            HttpServletRequest httpRequest) {
        ValidateKeyRequest body = request != null ? request : new ValidateKeyRequest();

        String sourceIp = body.getSourceIp() != null ? body.getSourceIp() : httpRequest.getRemoteAddr();

        ValidateKeyResponse result = validationService.validate(apiKeyHeader, sourceIp, body.getRequiredScope());

        // Still 200 OK either way (see class doc) -- the body's "valid"
        // field is the real answer. HttpStatus kept explicit for clarity.
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
