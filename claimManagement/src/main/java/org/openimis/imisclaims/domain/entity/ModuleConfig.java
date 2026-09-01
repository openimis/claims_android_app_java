package org.openimis.imisclaims.domain.entity;

import androidx.annotation.NonNull;

public class ModuleConfig {

    @NonNull
    private final String config;
    @NonNull
    private final String module;

    public ModuleConfig(
            @NonNull String config,
            @NonNull String module
    ) {
        this.config = config;
        this.module = module;
    }

    @NonNull
    public String getConfig() {
        return config;
    }

    @NonNull
    public String getModule() {
        return module;
    }
}