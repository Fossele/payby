package com.fossele.payby.service;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public PaymentIntent createPaymentIntent(Long amount, String currency, String customerId) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setCustomer(customerId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();
        return PaymentIntent.create(params);
    }

    public Subscription createSubscription(String customerId, String priceId) throws StripeException {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addAllExpand(java.util.List.of("latest_invoice.payment_intent"))
                .build();
        return Subscription.create(params);
    }

    public Subscription cancelSubscription(String subscriptionId, boolean immediately) throws StripeException {
        Subscription resource = Subscription.retrieve(subscriptionId);
        if (immediately) {
            return resource.cancel();
        } else {
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            return resource.update(params);
        }
    }

    public Subscription updateSubscriptionTier(String subscriptionId, String newPriceId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        String currentItemId = subscription.getItems().getData().get(0).getId();

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .addItem(
                        SubscriptionUpdateParams.Item.builder()
                                .setId(currentItemId)
                                .setPrice(newPriceId)
                                .build()
                )
                .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.ALWAYS_INVOICE)
                .build();

        return subscription.update(params);
    }

    public void updateDefaultPaymentMethod(String customerId, String subscriptionId, String paymentMethodId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        paymentMethod.attach(com.stripe.param.PaymentMethodAttachParams.builder().setCustomer(customerId).build());

        Subscription subscription = Subscription.retrieve(subscriptionId);
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setDefaultPaymentMethod(paymentMethodId)
                .build();
        subscription.update(params);
    }
}