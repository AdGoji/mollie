(ns com.adgoji.mollie.api
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.api.customers :as customers]
   [com.adgoji.mollie.api.mandates :as mandates]
   [com.adgoji.mollie.api.payments :as payments]
   [com.adgoji.mollie.api.subscriptions :as subscriptions]
   [com.adgoji.mollie.client :as client]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.customer.request :as customer.request]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.mandate.request :as mandate.request]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.payment.request :as payment.request]
   [com.adgoji.mollie.refund :as refund]
   [com.adgoji.mollie.refund.request :as refund.request]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.mollie.subscription.request :as subscription.request]
   [com.adgoji.mollie.chargeback :as chargeback]))

;;; Public API

;;;; API client

(defn new-client
  "Create a new Mollie API client.

  Parameters:
  - `:api-key` required. Can be obtained from Mollie dashboard.
  - `:base-url` optionally override base URL for Mollie API.
  - `:check-response?` optional (default `false`). If `true`, all
    responses will be additionally checked against spec. An error
    will be thrown if response doesn't conform to a spec.
  - `:throw-exceptions?` optional (default `true`). If `true`, throw
    an exception if HTTP status code from Mollie API is >= 400.

  Example:
  ```clojure
  (def mollie-client
    (mollie.api/new-client
      {:api-key \"string\"
       :base-url \"https://api.mollie.com\"
       :check-response? true
       :throw-exceptions? false}))
  ```"
  [params]
  (client/new-client params))

;;;; Customers

(defn create-customer
  "Create a new Mollie customer.

  Customer can be used for Mollie checkout and recurring features. All
  customer keys are optional.

  Example:
  ```clojure
  (mollie.api/create-customer mollie-client {})
  ```"
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
               :opts (s/keys :opt-un
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
  parameter (second arity).

  Examples:

  - create one-off payment

  ```clojure
  (mollie.api/create-payment
    mollie-client
    {:amount
      {:value 100.00M
       :currency \"EUR\"}
     :description \"Mollie one-off payment\"
     :redirect-url \"https://example.com\"})
  ```

  - create first payment (customer is required)

  ```clojure
  (mollie.api/create-payment
    mollie-client
    {:amount
      {:value 100.00M
       :currency \"EUR\"}
     :description \"First payment to start a subscription\"
     :sequence-type :first
     :redirect-url \"https://example.com\"
     :customer-id \"cus_123\"})
  ```

  - create recurring payment (mandate is required)

  ```clojure
  (mollie.api/create-payment
    mollie-client
    {:amount
      {:value 100.00M
       :currency \"EUR\"}
     :description \"Next subscription payment\"
     :sequence-type :recurring})
  ```"
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
  "Fetch a single payment by `payment-id`.

  There is an option to embed related resources, such as `refunds`,
  `chargebacks` and `captures`. To do that, pass `:embed` option with
  a vector of required keywords:

  ```clojure
  (mollie.api/get-payment-by-id
    mollie-client
    \"payment-id\"
    {:embed [:refunds :chargebacks :captures]})
  ```

  NOTE: captures are not supported at the moment."
  ([client payment-id]
   (get-payment-by-id client payment-id {}))
  ([client payment-id opts]
   (payments/get-by-id client payment-id opts)))

(s/fdef get-payment-by-id
  :args (s/or :minimal (s/cat :client map?
                              :payment-id ::payment/id)
              :with-opts (s/cat :client map?
                                :payment-id ::payment/id
                                :opts (s/keys :opt-un [::mollie/embed])))
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

;;;;; Payment refunds

(defn create-payment-refund
  "Creates a refund for a specific payment. The refunded amount is
  credited to your customer usually either via a bank transfer or by
  refunding the amount to your customer's credit card.

  Amount currency must be the same as the corresponding payment.

  Example:

  ```clojure
  (mollie.api/create-payment-refund
    mollie-client
    \"payment-id\"
    {:amount {:value 10.00M :currency \"EUR\"}})
  ```"
  [client payment-id refund]
  (payments/create-refund client payment-id refund))

(s/fdef create-payment-refund
  :args (s/cat :client map?
               :payment-id ::payment/id
               :refund ::refund.request/create)
  :ret ::mollie/refund)

(defn get-payment-refund-by-id
  "Returns single refund by `payment-id` and `refund-id`."
  [client payment-id refund-id]
  (payments/get-refund-by-id client payment-id refund-id))

(s/fdef get-payment-refund-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id
               :refund-id ::refund/id)
  :ret ::mollie/refund)

(defn get-refunds-list
  "Fetch all refunds for a specific payment."
  [client payment-id opts]
  (payments/get-refunds-list client payment-id opts))

(s/fdef get-payment-refunds-list
  :args (s/cat :client map?
               :payment-id ::payment/id
               :opts (s/keys :opt-un
                             [::pagination/from
                              ::pagination/limit]))
  :ret ::mollie/refunds-list)

(defn cancel-payment-refund-by-id
  "For certain payment methods, like iDEAL, the underlying banking
  system will delay refunds until the next day. Until that time,
  refunds may be canceled manually in the Mollie Dashboard, or
  programmatically by using this endpoint.

  A refund can only be canceled while its status field is either
  `:queued` or `:pending`."
  [client payment-id refund-id]
  (payments/cancel-refund-by-id client payment-id refund-id))

(s/fdef cancel-payment-refund-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id
               :refund-id ::refund/id)
  :ret nil?)

;;;;; Payment chargebacks

(defn get-payment-chargeback-by-id
  "Retrieve a single chargeback by its ID. Note the original payment's
  ID is needed as well."
  [client payment-id chargeback-id]
  (payments/get-chargeback-by-id client payment-id chargeback-id))

(s/fdef get-payment-chargeback-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id
               :chargeback-id ::chargeback/id)
  :ret ::mollie/chargeback)

(defn get-chargebacks-list
  "Retrieve the chargebacks initiated for a specific payment."
  [client payment-id opts]
  (payments/get-chargebacks-list client payment-id opts))

(s/fdef get-payment-chargeback-by-id
  :args (s/cat :client map?
               :payment-id ::payment/id
               :opts (s/keys :opt-un
                             [::pagination/from
                              ::pagination/limit]))
  :ret ::mollie/chargebacks-list)

;;;; Mandates

(defn create-mandate
  "Create a new mandate for a given `customer-id`.

  A mandate essentially symbolizes the authorization a customer gave
  you to recurrently charge their card or bank account.

  Example:
  ```clojure
  (mollie.api/create-mandate
    mollie-client
    {:method :directdebit
     :consumer-name \"Test Customer\"
     :consumer-account \"NL55INGB0000000000\"})
  ```

  Creating a mandate explicitly is not strictly required to process
  recurring payments. In basic implementations it suffices
  to [[create-customer]], do a first payment with the customer, and
  then charge recurring payments on the customer. A mandate is created
  automatically for the first payment, and that mandate is
  automatically used for any consecutive recurring payments."
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
               :customer-id ::customer/id
               :opts (s/keys :opt-un [::pagination/from
                                      ::pagination/limit]))
  :ret ::mollie/mandates-list)

;;;; Subscriptions

(defn create-subscription
  "Create a new subscription for a given `customer-id`.

  Subscription description should be unique.

  Example:
  ```clojure
  (let [description (str \"Test subscription\" (random-uuid))]
    (mollie.api/create-subscription
      mollie-client
      {:amount
        {:value 100.00M
         :currency \"EUR\"}
       :interval \"1 months\"
       :description description}))
  ```"
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
  "Fetch a list of subscriptions.

  If `customer-id` is omitted, fetch all subscriptions for all
  customers."
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
