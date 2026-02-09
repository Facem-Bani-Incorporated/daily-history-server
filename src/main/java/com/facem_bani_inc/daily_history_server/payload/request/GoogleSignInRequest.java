package com.facem_bani_inc.daily_history_server.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleSignInRequest {

    @NotBlank
    private String idToken;
}
