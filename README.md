# Mollie API for Clojure #

Clojure client implementation for [Mollie API](https://docs.mollie.com/index).

---
[![Clojars Project](https://img.shields.io/clojars/v/com.adgoji/mollie.svg)](https://clojars.org/com.adgoji/mollie)
[![cljdoc badge](https://cljdoc.org/badge/com.adgoji/mollie)](https://cljdoc.org/d/com.adgoji/mollie)

## Requirements ##

- Java 11
- Clojure 1.11

## Quick start ##

### Include the library to your project ###

See the [clojars](https://clojars.org/com.adgoji/mollie) page for the latest version.

### Add require ###

```clojure
(require '[com.adgoji.mollie.api :as mollie.api]
```

### Create a new client ###

```clojure
(def mollie-client
  (mollie.api/new-client {:api-key "your-api-key"}))
```

### Explore available functions ###

All functions are available in the `com.adgoji.mollie.api` namespace:

```clojure
(mollie.api/create-payment mollie-client
                           {:amount
                            {:value    100.00M
                             :currency "EUR"}
                            :description  "Buying some good stuff"
                            :redirect-url "https://example.com"})
;; => {:com.adgoji.mollie.payment/status :open,
;;     :com.adgoji.mollie.payment/redirect-url "https://example.com",
;;     :com.adgoji.mollie.payment/resource "payment",
;;     :com.adgoji.mollie.payment/id "tr_oXHpf8Rk8w",
;;     :com.adgoji.mollie.payment/amount
;;     #:com.adgoji.mollie.amount{:value 100.00M, :currency "EUR"},
;;     :com.adgoji.mollie.payment/description "Buying some good stuff",
;;     :com.adgoji.mollie.payment/created-at
;;     #object[java.time.Instant 0x6e7855d9 "2023-06-29T20:36:32Z"],
;;     :com.adgoji.mollie.link/self
;;     #:com.adgoji.mollie.link{:href "https://api.mollie.com/v2/payments/tr_oXHpf8Rk8w",
;;                              :type "application/hal+json"},
;;     :com.adgoji.mollie.payment/expires-at
;;     #object[java.time.Instant 0x25f9a736 "2023-06-29T20:51:32Z"],
;;     :com.adgoji.mollie.link/dashboard
;;     #:com.adgoji.mollie.link{:href "https://my.mollie.com/dashboard/org_17446479/payments/tr_oXHpf8Rk8w",
;;                              :type "text/html"},
;;     :com.adgoji.mollie.payment/mode :test,
;;     :com.adgoji.mollie.link/documentation
;;     #:com.adgoji.mollie.link{:href "https://docs.mollie.com/reference/v2/payments-api/create-payment",
;;                              :type "text/html"},
;;     :com.adgoji.mollie.payment/profile-id "pfl_25emLDBma5",
;;     :com.adgoji.mollie.payment/method nil,
;;     :com.adgoji.mollie.link/checkout
;;     #:com.adgoji.mollie.link{:href "https://www.mollie.com/checkout/select-method/oXHpf8Rk8w",
;;                              :type "text/html"},
;;     :com.adgoji.mollie.payment/details nil,
;;     :com.adgoji.mollie.payment/sequence-type :oneoff}

(require '[com.adgoji.mollie.payment :as payment])

(::payment/id *1)
;; => "tr_oXHpf8Rk8w"

(mollie.api/get-payment-by-id client "tr_oXHpf8Rk8w")
```

## Usage notes ##

### Specs ###

All functions in the `com.adgoji.mollie.api` namespace are covered by
[spec](https://clojure.org/guides/spec). If your editor supports spec (like CIDER), it's a very
convenient way to explore the library:

![spec screencast](https://github.com/AdGoji/mollie/assets/100711682/7d4c0ebb-93d9-40a2-8823-581cc48a696d)

### Response format ###

All responses are returned as hash maps with fully qualified keywords
as keys. Please browse specs for more details.

### Client options ###

- `:base-url` (string: optional) - redefine Mollie API base URL.
- `:api-key` (string: required)
- `:check-response` (boolean: optional) - additionally check if response
  conforms to spec. This is disabled by default, so if response is
  changed on the Mollie side, client will continue working (although
  results may be unpredictable).

### Pagination ###

All functions that return list of entities support pagination
parameters - `from` and `limit`. Both parameters are optional:
- if both parameters are omitted - return all entities from all pages.
- if only `from` parameter is provided - return all entities from all
  pages starting from the provided entity ID.
- if only `limit` parameter is provided - return only requested number
  of entities starting from the first entity.
- if both parameters are provided - return only requested number of
  entities starting from the provided entity ID.

## Features ##

- [x] API key authentication
- [x] Customers
  - [x] Create customer
  - [x] Update customer
  - [x] Delete customer
  - [x] Fetch customer by ID
  - [x] Get list of customers
  - [x] Create customer payment
  - [x] Get list of customer payments
- [x] Payments
  - [x] Create payment
  - [x] Get payment by ID
  - [x] Cancel payment by ID
  - [x] Update payment by ID
  - [x] Get list of payments
- [x] Mandates
  - [x] Create mandate
  - [x] Get mandate by ID
  - [x] Revoke mandate by ID
  - [x] Get list of mandates
- [x] Subscriptions
  - [x] Create subscription
  - [x] Get subscription by ID
  - [x] Update subscription by ID
  - [x] Delete subscription by ID
  - [x] Get list of subscriptions
- [ ] Methods
- [ ] Refunds
- [ ] Chargebacks
- [ ] Captures
- [ ] Orders
- [ ] Shipments
- [ ] Settlements
- [ ] Organizations
- [ ] Permissions
- [ ] Invoices
- [ ] Mollie connect
