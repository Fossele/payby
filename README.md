# Payby

Payby is a Spring Boot billing service that integrates Stripe for payment intents and subscription management. It stores subscription state, payment history, and webhook event data in PostgreSQL.

## Features

- Create Stripe payment intents
- Create, update, and cancel subscriptions
- Update a subscription's default payment method
- Retrieve subscription details and payment history
- Verify and process Stripe webhooks asynchronously
- Persist billing and webhook audit data with Spring Data JPA

## Requirements

- Java 21
- PostgreSQL
- A Stripe account with test API credentials

## Configuration

The application defaults to a local PostgreSQL database named `payby_db`. Override these values with environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payby_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
```

Create the database before starting the application:

```sql
CREATE DATABASE payby_db;
```

## Running locally

Start the application with the Maven wrapper:

```bash
./mvnw spring-boot:run
```

Run the test suite:

```bash
./mvnw test
```

The service starts on `http://localhost:8080`.

## API

All endpoints are under `/api/v1`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/billing/customers/{customerId}/subscription` | Get a customer's subscription |
| `GET` | `/billing/customers/{customerId}/history` | Get payment history |
| `POST` | `/billing/intents?customerId=...&amount=...&currency=...` | Create a payment intent |
| `POST` | `/billing/subscriptions?customerId=...&priceId=...` | Create a subscription |
| `DELETE` | `/billing/subscriptions/{subscriptionId}?immediately=false` | Cancel a subscription |
| `PUT` | `/billing/subscriptions/{subscriptionId}/tier?newPriceId=...` | Change subscription tier |
| `PUT` | `/billing/subscriptions/{subscriptionId}/payment-method?customerId=...&paymentMethodId=...` | Update the default payment method |
| `POST` | `/webhooks/stripe` | Receive verified Stripe webhook events |

Payment amounts use Stripe's smallest currency unit, such as cents for USD.

## Stripe webhooks

Configure Stripe to send events to:

```text
http://localhost:8080/api/v1/webhooks/stripe
```

The endpoint requires Stripe's `Stripe-Signature` header and validates it using `STRIPE_WEBHOOK_SECRET`.

## Project structure

```text
src/main/java/com/fossele/payby/
├── controller/   REST and Stripe webhook endpoints
├── entity/       JPA persistence models
├── repository/   Spring Data repositories
├── service/      Stripe and asynchronous webhook processing
└── state/        Subscription state definitions
```

