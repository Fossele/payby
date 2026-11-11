package com.fossele.payby.state;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionStateTest {

    @Test
    void testValidTransitions() {
        assertTrue(SubscriptionState.INCOMPLETE.canTransitionTo(SubscriptionState.ACTIVE));
        assertTrue(SubscriptionState.ACTIVE.canTransitionTo(SubscriptionState.CANCELED));
        assertTrue(SubscriptionState.PAST_DUE.canTransitionTo(SubscriptionState.ACTIVE));
        assertTrue(SubscriptionState.PAST_DUE.canTransitionTo(SubscriptionState.UNPAID));
    }

    @Test
    void testInvalidTransitions() {
        assertFalse(SubscriptionState.CANCELED.canTransitionTo(SubscriptionState.ACTIVE));
        assertFalse(SubscriptionState.INCOMPLETE.canTransitionTo(SubscriptionState.PAST_DUE));
        assertFalse(SubscriptionState.ACTIVE.canTransitionTo(SubscriptionState.INCOMPLETE));
    }
}