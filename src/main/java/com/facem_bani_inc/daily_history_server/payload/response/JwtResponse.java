package com.facem_bani_inc.daily_history_server.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private boolean pro;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, String avatarUrl, boolean pro, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.pro = pro;
        this.roles = roles;
    }
}
