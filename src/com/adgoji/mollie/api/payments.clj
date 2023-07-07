(ns com.adgoji.mollie.api.payments
  (:require
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.client :as mollie.client]
   [com.adgoji.mollie.creditcard :as creditcard]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.directdebit :as directdebit]
   [com.adgoji.mollie.ideal :as ideal]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.payment.request :as payment.request]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.utils.decimal :as decimal]
   [com.adgoji.utils.spec :as spec])
  (:import
   (java.time Instant LocalDate)))

(defmulti get-details (comp keyword :method) :default ::default)

(defmethod get-details :directdebit
  [{{:keys [transfer-reference
            creditor-identifier
            consumer-name
            consumer-account
            consumer-bic
            due-date
            signature-date
            bank-reason-code
            bank-reason
            end-to-end-identifier
            mandate-reference
            batch-reference
            file-reference]} :details}]
  (cond-> {::directdebit/transfer-reference  transfer-reference
           ::directdebit/creditor-identifier creditor-identifier
           ::directdebit/consumer-name       consumer-name
           ::directdebit/consumer-account    consumer-account
           ::directdebit/consumer-bic        consumer-bic
           ::directdebit/due-date            (LocalDate/parse due-date)}
    signature-date        (assoc ::directdebit/signature-date (LocalDate/parse signature-date))
    bank-reason-code      (assoc ::directdebit/bank-reason-code bank-reason-code)
    bank-reason           (assoc ::directdebit/bank-reason bank-reason)
    end-to-end-identifier (assoc ::directdebit/end-to-end-identifier end-to-end-identifier)
    mandate-reference     (assoc ::directdebit/mandate-reference mandate-reference)
    batch-reference       (assoc ::directdebit/batch-reference batch-reference)
    file-reference        (assoc ::directdebit/file-reference file-reference)))

(defmethod get-details :creditcard
  [{{:keys [card-holder
            card-number
            card-fingerprint
            card-expiry-date
            card-audience
            card-label
            card-country-code
            card-security
            fee-region
            failure-reason
            failure-message
            wallet]} :details}]
  (cond-> {}
    card-holder       (assoc ::creditcard/card-holder card-holder)
    card-number       (assoc ::creditcard/card-number card-number)
    card-fingerprint  (assoc ::creditcard/card-fingerprint card-fingerprint)
    card-expiry-date  (assoc ::creditcard/card-expiry-date (LocalDate/parse card-expiry-date))
    card-audience     (assoc ::creditcard/card-audience (keyword card-audience))
    card-label        (assoc ::creditcard/card-label card-label)
    card-country-code (assoc ::creditcard/card-country-code card-country-code)
    card-security     (assoc ::creditcard/card-security (keyword card-security))
    fee-region        (assoc ::creditcard/fee-region fee-region)
    failure-reason    (assoc ::creditcard/failure-reason (keyword failure-reason))
    failure-message   (assoc ::creditcard/failure-message failure-message)
    wallet            (assoc ::creditcard/wallet (keyword wallet))))

(defmethod get-details :ideal
  [{{:keys [consumer-name
            consumer-account
            consumer-bic]} :details}]
  (cond-> {}
    consumer-name    (assoc ::ideal/consumer-name consumer-name)
    consumer-account (assoc ::ideal/consumer-account consumer-account)
    consumer-bic     (assoc ::ideal/consumer-bic consumer-bic)))

(defmethod get-details ::default
  [{:keys [method]}]
  (throw (ex-info "Payment method is not supported"
                  {:method method})))

(defn- transform-payment
  [{:keys [resource
           id
           mode
           created-at
           status
           amount
           description
           redirect-url
           method
           profile-id
           links
           is-cancelable
           authorized-at
           paid-at
           canceled-at
           expires-at
           expired-at
           failed-at
           amount-refunded
           amount-remaining
           amount-captured
           amount-charged-back
           settlement-amount
           cancel-url
           webhook-url
           locale
           country-code
           restrict-payment-methods-to-country
           metadata
           settlement-id
           order-id
           details
           sequence-type
           customer-id
           mandate-id
           subscription-id]
    :as   payment}]
  (cond-> {::payment/resource      resource
           ::payment/id            id
           ::payment/mode          (keyword mode)
           ::payment/created-at    (Instant/parse created-at)
           ::payment/status        (keyword status)
           ::payment/amount        (spec/qualify-amount amount)
           ::payment/description   description
           ::payment/redirect-url  redirect-url
           ::payment/method        (keyword method)
           ::payment/profile-id    profile-id
           ::payment/sequence-type (keyword sequence-type)
           ::link/self             (spec/qualify-link (:self links))
           ::link/dashboard        (spec/qualify-link (:dashboard links))
           ::link/documentation    (spec/qualify-link (:documentation links))}
    is-cancelable                       (assoc ::payment/is-cancelable is-cancelable)
    authorized-at                       (assoc ::payment/authorized-at (Instant/parse authorized-at))
    paid-at                             (assoc ::payment/paid-at (Instant/parse paid-at))
    canceled-at                         (assoc ::payment/canceled-at (Instant/parse canceled-at))
    expires-at                          (assoc ::payment/expires-at (Instant/parse expires-at))
    expired-at                          (assoc ::payment/expired-at (Instant/parse expired-at))
    failed-at                           (assoc ::payment/failed-at (Instant/parse failed-at))
    amount-refunded                     (assoc ::payment/amount-refunded (spec/qualify-amount amount-refunded))
    amount-remaining                    (assoc ::payment/amount-remaining (spec/qualify-amount amount-remaining))
    amount-captured                     (assoc ::payment/amount-captured (spec/qualify-amount amount-captured))
    amount-charged-back                 (assoc ::payment/amount-charged-back (spec/qualify-amount amount-charged-back))
    settlement-amount                   (assoc ::payment/settlement-amount (spec/qualify-amount settlement-amount))
    cancel-url                          (assoc ::payment/cancel-url cancel-url)
    webhook-url                         (assoc ::payment/webhook-url webhook-url)
    locale                              (assoc ::payment/locale locale)
    country-code                        (assoc ::payment/country-code country-code)
    restrict-payment-methods-to-country (assoc ::payment/restrict-payment-methods-to-country restrict-payment-methods-to-country)
    metadata                            (assoc ::payment/metadata metadata)
    settlement-id                       (assoc ::payment/settlement-id settlement-id)
    order-id                            (assoc ::payment/order-id order-id)
    customer-id                         (assoc ::payment/customer-id customer-id)
    mandate-id                          (assoc ::payment/mandate-id mandate-id)
    subscription-id                     (assoc ::payment/subscription-id subscription-id)
    (:checkout links)                   (assoc ::link/checkout
                                               (spec/qualify-link (:checkout links)))
    (:mobile-app-checkout links)        (assoc ::link/mobile-app-checkout
                                               (spec/qualify-link (:mobile-app-checkout links)))
    (:refunds links)                    (assoc ::link/refunds
                                               (spec/qualify-link (:refunds links)))
    (:chargebacks links)                (assoc ::link/chargebacks
                                               (spec/qualify-link (:chargebacks links)))
    (:captures links)                   (assoc ::link/captures
                                               (spec/qualify-link (:captures links)))
    (:settlement links)                 (assoc ::link/settlement
                                               (spec/qualify-link (:settlement links)))
    (:order links)                      (assoc ::link/order
                                               (spec/qualify-link (:order links)))
    (:customer links)                   (assoc ::link/customer
                                               (spec/qualify-link (:customer links)))
    (:mandate links)                    (assoc ::link/mandate
                                               (spec/qualify-link (:mandates links)))
    (:change-payment-state links)       (assoc ::link/change-payment-state
                                               (spec/qualify-link (:change-payment-state links)))
    details                             (into (get-details payment))))

(defn- create-generic
  [client endpoint payment]
  (let [body (update-in payment [:amount :value] decimal/format)]
    (mollie.client/http-post client
                             endpoint
                             {:body                 body
                              :response-transformer transform-payment
                              :spec                 ::mollie/payment})))

(defn create
  "Create a new Mollie payment."
  ([client payment]
   (create-generic client
                   "/v2/payments"
                   (spec/check payment ::payment.request/create)))
  ([client payment customer-id]
   (create-generic client
                   (format "/v2/customers/%s/payments"
                           (spec/check customer-id ::customer/id))
                   (spec/check payment ::payment.request/create))))

(defn get-by-id
  "Fetch a single payment by `payment-id`."
  [client payment-id]
  (mollie.client/http-get client
                          (str "/v2/payments/"
                               (spec/check payment-id ::payment/id))
                          {:response-transformer transform-payment
                           :spec                 ::mollie/payment}))

(defn update-by-id
  "Update a single payment by `payment-id`."
  [client payment-id data]
  (let [body (spec/check data ::payment.request/update)]
    (mollie.client/http-patch client
                              (str "/v2/payments/"
                                   (spec/check payment-id ::payment/id))
                              {:body                 body
                               :response-transformer transform-payment
                               :spec                 ::mollie/payment})))

(defn cancel-by-id
  "Cancel a single payment by `payment-id`."
  [client payment-id]
  (mollie.client/http-delete client
                             (str "/v2/payments/"
                                  (spec/check payment-id ::payment/id))
                             {:response-transformer transform-payment
                              :spec                 ::mollie/payment}))

(defn- transform-payments
  [response]
  (let [payments        (->> (get-in response [:embedded :payments])
                             (into [] (map transform-payment)))
        next-params     (-> (get-in response [:links :next :href])
                            (mollie.client/extract-page-params))
        previous-params (-> (get-in response [:links :previous :href])
                            (mollie.client/extract-page-params))
        self-params     (-> (get-in response [:links :self :href])
                            (mollie.client/extract-page-params))]
    {::mollie/payments     payments
     ::pagination/count    (:count response)
     ::pagination/next     next-params
     ::pagination/previous previous-params
     ::pagination/self     self-params}))

(defn- get-list-generic
  [client endpoint {:keys [from limit]}]
  (let [fetch-fn (if limit
                   mollie.client/http-get
                   (partial mollie.client/fetch-all ::mollie/payments))]
    (fetch-fn client
              endpoint
              {:response-transformer transform-payments
               :spec                 ::mollie/payments-list
               :query-params         (cond-> {}
                                       from  (assoc :from from)
                                       limit (assoc :limit limit))})))

(defn get-list
  "Fetch all payments."
  ([client opts]
   (get-list-generic client "/v2/payments" (spec/check opts ::pagination/opts)))
  ([client customer-id opts]
   (get-list-generic client
                     (format "/v2/customers/%s/payments"
                             (spec/check customer-id ::customer/id))
                     (spec/check opts ::pagination/opts)))
  ([client customer-id subscription-id opts]
   (get-list-generic client
                     (format "/v2/customers/%s/subscriptions/%s/payments"
                             (spec/check customer-id ::customer/id)
                             (spec/check subscription-id ::subscription/id))
                     (spec/check opts ::pagination/opts))))
