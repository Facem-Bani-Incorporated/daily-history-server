package com.facem_bani_inc.daily_history_server.security.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class AppleAuthConfig {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    @Bean
    public JWKSource<SecurityContext> appleJwkSource() throws Exception {
        return JWKSourceBuilder
                .create(URI.create(APPLE_JWKS_URL).toURL())
                .retrying(true)
                .build();
    }

    @Bean
    public ConfigurableJWTProcessor<SecurityContext> appleJwtProcessor(JWKSource<SecurityContext> appleJwkSource) {
        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, appleJwkSource));
        return processor;
    }
}
