(ns com.adgoji.mollie.api
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.api.customers :as customers]
   [com.adgoji.mollie.api.mandates :as mandates]
   [com.adgoji.mollie.api.payments :as payments]
   [com.adgoji.mollie.api.subscriptions :as subscriptions]
   [com.adgoji.mollie.client :as client]
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.customer.request :as customer.request]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.mandate.request :as mandate.request]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.payment.request :as payment.request]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.mollie.subscription.request :as subscription.request]))

;;; Public API

;;;; API client

(defn new-client
  "Create a new Mollie API client."
  [params]
  (client/new-client params))

;;;; Customers

(defn create-customer
  "Create a new Mollie customer."
  [client customer]
  (customers/create client customer))

(s/fdef create-customer
  :args (s/cat :client map?
               :customer ::customer.request/create)
  :ret ::mollie/customer)

(defn get-customer-by-id
  "Fetch a single customer by `customer-id`."
  [client customer-id]
  (customers/get-by-id client customer-id))

(s/fdef get-customer-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id)
  :ret ::mollie/customer)

(defn update-customer-by-id
  "Update a single customer by `customer-id`."
  [client customer-id data]
  (customers/update-by-id client customer-id data))

(s/fdef update-customer-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id
               :data ::customer.request/update)
  :ret ::mollie/customer)

(defn delete-customer-by-id
  "Delete a single customer by `customer-id`."
  [client customer-id]
  (customers/delete-by-id client customer-id))

(s/fdef delete-customer-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id)
  :ret nil?)

(defn get-customers-list
  "Fetch all customers."
  [client opts]
  (customers/get-list client opts))

(s/fdef get-customers-list
  :args (s/cat :client map?
               :opts (s/keys* :opt-un
                              [::pagination/from
                               ::pagination/limit]))
  :ret ::mollie/customers-list)

;;;; Payments

(defn create-payment
  "Create a new payment.

  If `customer-id` is provided, create a new payment for customer. To
  create a recurring payment `customer-id` is mandatory.

  For some reason it's not possible to create a recurring payment for
  customer by providing `customer-id` in the request body (Mollie
  returns an error that payment method is not enabled), but it can be
  created without any issues by providing `customer-id` as a path
  parameter (second arity)."
  ([client payment]
   (payments/create client payment))
  ([client payment customer-id]
   (payments/create client payment customer-id)))

(s/fdef create-payment
  :args (s/or :anonymous (s/cat :client map?
                                :payment ::payment.request/create)
              :for-customer (s/cat :client map?
                                   :payment ::payment.request/create
                                   :customer-id ::customer/id))
  :ret ::mollie/payment)

(defn get-payment-by-id
  "Fetch a single payment by `payment-id`."
  [client payment-id]
  (payments/get-by-id client payment-id))

(s/fdef get-payment-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id)
  :ret ::mollie/payment)

(defn update-payment-by-id
  "Update a single payment by `payment-id`."
  [client payment-id data]
  (payments/update-by-id client payment-id data))

(s/fdef update-payment-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id
               :data ::payment.request/update)
  :ret ::mollie/payment)

(defn cancel-payment-by-id
  "Cancel a single payment by `payment-id`."
  [client payment-id]
  (payments/cancel-by-id client payment-id))

(s/fdef cancel-payment-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id)
  :ret ::mollie/payment)

(defn get-payments-list
  "Fetch all payments.

  If `customer-id` is provided, fetch only payments for a given
  customer.

  If `customer-id` and `subscription-id` are provided, fetch only
  payments for a given customer and a given subscription."
  ([client opts]
   (payments/get-list client opts))
  ([client customer-id opts]
   (payments/get-list client customer-id opts))
  ([client customer-id subscription-id opts]
   (payments/get-list client customer-id subscription-id opts)))

(s/fdef get-payments-list
  :args (s/or :all (s/cat :client map?
                          :opts (s/keys :opt-un
                                        [::pagination/from
                                         ::pagination/limit]))
              :by-customer (s/cat :client map?
                                  :customer-id ::customer/id
                                  :opts (s/keys :opt-un
                                                [::pagination/from
                                                 ::pagination/limit]))
              :by-subscription (s/cat :client map?
                                      :customer-id ::customer/id
                                      :subscription-id ::subscription/id
                                      :opts (s/keys :opt-un
                                                    [::pagination/from
                                                     ::pagination/limit])))
  :ret ::mollie/payments-list)

;;;; Mandates

(defn create-mandate
  "Create a new mandate for a given `customer-id`."
  [client customer-id mandate]
  (mandates/create client customer-id mandate))

(s/fdef create-mandate
  :args (s/cat :client map?
               :customer-id ::customer/id
               :mandate ::mandate.request/create)
  :ret ::mollie/mandate)

(defn get-mandate-by-id
  "Fetch a single customer's mandate by `mandate-id`."
  [client customer-id mandate-id]
  (mandates/get-by-id client customer-id mandate-id))

(s/fdef get-mandate-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id
               :mandate-id ::mandate/id)
  :ret ::mollie/mandate)

(defn revoke-mandate-by-id
  "Revoke a specific customer's mandate by `mandate-id`."
  [client customer-id mandate-id]
  (mandates/revoke-by-id client customer-id mandate-id))

(s/fdef revoke-mandate-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id
               :mandate-id ::mandate/id)
  :ret nil?)

(defn get-mandates-list
  "Fetch all customer's mandates."
  [client customer-id opts]
  (mandates/get-list client customer-id opts))

(s/fdef get-mandates-list
  :args (s/cat :client map?
               :customer-id ::customer-id
               :opts (s/keys :opt-un [::pagination/from
                                      ::pagination/limit]))
  :ret ::mollie/mandates-list)

;;;; Subscriptions

(defn create-subscription
  "Create a new subscription for a given `customer-id`."
  [client customer-id subscription]
  (subscriptions/create client customer-id subscription))

(s/fdef create-subscription
  :args (s/cat :client map?
               :customer-id ::customer/id
               :subscription ::subscription.request/create)
  :ret ::mollie/subscription)

(defn get-subscription-by-id
  "Fetch a single customer's subscription by `subscription-id`."
  [client customer-id subscription-id]
  (subscriptions/get-by-id client customer-id subscription-id))

(s/fdef get-subscription-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id
               :subscription-id ::subscription/id)
  :ret ::mollie/subscription)

(defn update-subscription-by-id
  "Update a single customer's subscription by `subscription-id`."
  [client customer-id subscription-id data]
  (subscriptions/update-by-id client customer-id subscription-id data))

(s/fdef update-subscription-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id
               :subscription-id ::subscription/id
               :data ::subscription.request/update)
  :ret ::mollie/subscription)

(defn cancel-subscription-by-id
  "Cancel a single customer's subscription by `subscription-id`."
  [client customer-id subscription-id]
  (subscriptions/cancel-by-id client customer-id subscription-id))

(s/fdef cancel-subscription-by-id
  :args (s/cat :client map?
               :customer-id ::customer/id
               :subscription-id ::subscription/id)
  :ret ::mollie/subscription)

(defn get-subscriptions-list
  ([client opts]
   (subscriptions/get-list client opts))
  ([client customer-id opts]
   (subscriptions/get-list client customer-id opts)))

(s/fdef get-subscriptions-list
  :args (s/or :all (s/cat :client map?
                          :opts (s/keys :opt-un [::pagination/from
                                                 ::pagination/limit]))
              :by-customer (s/cat :client map?
                                  :customer-id ::customer/id
                                  :opts (s/keys :opt-un [::pagination/from
                                                         ::pagination/limit])))
  :ret ::mollie/subscriptions-list)
