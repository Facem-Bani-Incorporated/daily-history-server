package com.facem_bani_inc.daily_history_server.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ECategory {

    @JsonProperty("war_conflict")
    WAR_CONFLICT,

    @JsonProperty("tech_innovation")
    TECH_INNOVATION,

    @JsonProperty("science_discovery")
    SCIENCE_DISCOVERY,

    @JsonProperty("politics_state")
    POLITICS_STATE,

    @JsonProperty("culture_arts")
    CULTURE_ARTS,

    @JsonProperty("natural_disaster")
    NATURAL_DISASTER,

    @JsonProperty("exploration")
    EXPLORATION,

    @JsonProperty("religion_phil")
    RELIGION_PHIL,

    @JsonProperty("media")
    MEDIA,

    @JsonProperty("sport")
    SPORT,

    @JsonProperty("personalities")
    PERSONALITIES
}
