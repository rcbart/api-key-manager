package com.apikeymanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.apikeymanager.dto.ValidateKeyResponse;
import com.apikeymanager.service.ValidationService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test for the validation endpoint's HTTP contract. Security filters
 * are disabled here (this endpoint is permitAll() in real life anyway, and
 * @WebMvcTest doesn't load the full SecurityConfig) -- the focus is on
 * request/response wiring, not authentication.
 */
@WebMvcTest(ValidationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidationService validationService;

    @Test
    void returns200WithValidTrueForAGoodKey() throws Exception {
        when(validationService.validate(eq("ak_live_good"), any(), isNull()))
                .thenReturn(ValidateKeyResponse.valid("My key", Set.of("read:orders"), null));

        mockMvc.perform(post("/api/validate")
                        .header("X-API-Key", "ak_live_good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.name").value("My key"));
    }

    @Test
    void returns200WithValidFalseAndAReasonForABadKey() throws Exception {
        when(validationService.validate(eq("ak_live_revoked"), any(), isNull()))
                .thenReturn(ValidateKeyResponse.invalid("revoked"));

        mockMvc.perform(post("/api/validate")
                        .header("X-API-Key", "ak_live_revoked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reason").value("revoked"));
    }

    @Test
    void passesAnExplicitSourceIpAndRequiredScopeThrough() throws Exception {
        when(validationService.validate(eq("ak_live_x"), eq("203.0.113.5"), eq("write:orders")))
                .thenReturn(ValidateKeyResponse.valid("My key", Set.of("write:orders"), null));

        mockMvc.perform(post("/api/validate")
                        .header("X-API-Key", "ak_live_x")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceIp\":\"203.0.113.5\",\"requiredScope\":\"write:orders\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        verify(validationService).validate("ak_live_x", "203.0.113.5", "write:orders");
    }

    @Test
    void missingApiKeyHeaderStillReachesTheServiceAsNull() throws Exception {
        when(validationService.validate(isNull(), any(), isNull()))
                .thenReturn(ValidateKeyResponse.invalid("missing_key"));

        mockMvc.perform(post("/api/validate").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reason").value("missing_key"));
    }
}
