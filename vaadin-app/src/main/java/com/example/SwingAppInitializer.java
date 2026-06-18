package com.example;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.seed.DataSeeder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class SwingAppInitializer {

    @PostConstruct
    void init() {
        PersistenceUtil.init();
        DataSeeder.seedIfEmpty();
    }
}
