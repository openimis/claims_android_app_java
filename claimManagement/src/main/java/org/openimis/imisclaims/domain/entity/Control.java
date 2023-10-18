package org.openimis.imisclaims.domain.entity;

import androidx.annotation.NonNull;

public class Control {

    @NonNull
    private final String name;
    @NonNull
    private final String usage;
    @NonNull
    private final String adjustability;

    public Control(
            @NonNull String name,
            @NonNull String usage,
            @NonNull String adjustability
    ) {
        this.name = name;
        this.usage = usage;
        this.adjustability = adjustability;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getUsage() {
        return usage;
    }

    @NonNull
    public String getAdjustability() {
        return adjustability;
    }
}
