package com.fossele.payby.state;


import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum SubscriptionState {
    INCOMPLETE,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    UNPAID;

    private static final Map<SubscriptionState, Set<SubscriptionState>> VALID_TRANSITIONS = Map.of(
            INCOMPLETE, EnumSet.of(ACTIVE, CANCELED),
            ACTIVE, EnumSet.of(PAST_DUE, CANCELED),
            PAST_DUE, EnumSet.of(ACTIVE, UNPAID, CANCELED),
            UNPAID, EnumSet.of(ACTIVE, CANCELED),
            CANCELED, EnumSet.noneOf(SubscriptionState.class)
    );

    public boolean canTransitionTo(SubscriptionState targetState) {
        if (this == targetState) return true;
        return VALID_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(SubscriptionState.class)).contains(targetState);
    }
}