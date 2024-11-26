package com.tourism.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /*@Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Hibernate5Module()
                .configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true));
        return mapper;
    }*/
}