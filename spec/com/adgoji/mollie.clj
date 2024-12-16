(ns com.adgoji.mollie
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.mollie.directdebit :as directdebit]
   [com.adgoji.mollie.creditcard :as creditcard]
   [com.adgoji.mollie.paypal :as paypal]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.ideal :as ideal]
   [com.adgoji.mollie.refund :as refund]
   [com.adgoji.mollie.chargeback :as chargeback]))

;;; Customer

(s/def ::customer
  (common/only-keys :req [::customer/id
                          ::customer/email
                          ::customer/name
                          ::customer/locale
                          ::customer/metadata
                          ::customer/resource
                          ::customer/created-at
                          ::customer/mode
                          ::link/self
                          ::link/dashboard
                          ::link/documentation]
                    :opt [::link/mandates
                          ::link/subscriptions
                          ::link/payments]))

(s/def ::customers
  (s/coll-of ::customer :distinct true :into []))

(s/def ::customers-list
  (common/only-keys :req [::customers
                          ::pagination/count]
                    :opt [::pagination/next
                          ::pagination/previous
                          ::pagination/self]))

;;; Refunds

(s/def ::refund
  (common/only-keys :req [::refund/resource
                          ::refund/id
                          ::refund/amount
                          ::refund/description
                          ::refund/status
                          ::refund/payment-id
                          ::refund/created-at
                          ::link/self
                          ::link/payment]
                    :opt [::refund/settlement-id
                          ::refund/settlement-amount
                          ::refund/lines
                          ::refund/order-id
                          ::refund/metadata
                          ::link/documentation
                          ::link/settlement
                          ::link/order]))

(s/def ::refunds
  (s/coll-of ::refund :distinct true :into []))

(s/def ::refunds-list
  (common/only-keys :req [::refunds
                          ::pagination/count]
                    :opt [::pagination/next
                          ::pagination/previous
                          ::pagination/self]))

;;; Chargebacks

(s/def ::chargeback
  (common/only-keys :req [::chargeback/resource
                          ::chargeback/id
                          ::chargeback/amount
                          ::chargeback/settlement-amount
                          ::chargeback/created-at
                          ::chargeback/payment-id
                          ::link/self
                          ::link/payment]
                    :opt [::chargeback/reason
                          ::chargeback/reversed-at
                          ::link/settlement
                          ::link/documentation]))

(s/def ::chargebacks
  (s/coll-of ::chargeback :distinct true :into []))

(s/def ::chargebacks-list
  (common/only-keys :req [::chargebacks
                          ::pagination/count]
                    :opt [::pagination/next
                          ::pagination/previous
                          ::pagination/self]))

;;; Embedded

(s/def ::embedded
  (common/only-keys :opt [::refunds
                          ::chargebacks]))

;;; Payment

(defmulti payment-spec ::payment/method)

(defmethod payment-spec :directdebit
  [_]
  (common/only-keys :req [::payment/resource
                          ::payment/id
                          ::payment/mode
                          ::payment/created-at
                          ::payment/status
                          ::payment/amount
                          ::payment/description
                          ::payment/redirect-url
                          ::payment/method
                          ::payment/profile-id
                          ::directdebit/transfer-reference
                          ::directdebit/creditor-identifier
                          ::directdebit/consumer-name
                          ::directdebit/consumer-account
                          ::directdebit/consumer-bic
                          ::directdebit/due-date
                          ::link/self
                          ::link/dashboard
                          ::link/documentation]
                    :opt [::payment/is-cancelable
                          ::payment/authorized-at
                          ::payment/paid-at
                          ::payment/canceled-at
                          ::payment/expires-at
                          ::payment/expired-at
                          ::payment/failed-at
                          ::payment/amount-refunded
                          ::payment/amount-remaining
                          ::payment/amount-captured
                          ::payment/amount-charged-back
                          ::payment/settlement-amount
                          ::payment/cancel-url
                          ::payment/webhook-url
                          ::payment/locale
                          ::payment/country-code
                          ::payment/restrict-payment-methods-to-country
                          ::payment/metadata
                          ::payment/settlement-id
                          ::payment/order-id
                          ::payment/sequence-type
                          ::payment/customer-id
                          ::payment/mandate-id
                          ::payment/subscription-id
                          ::embedded
                          ::directdebit/signature-date
                          ::directdebit/bank-reason-code
                          ::directdebit/bank-reason
                          ::directdebit/end-to-end-identifier
                          ::directdebit/mandate-reference
                          ::directdebit/batch-reference
                          ::directdebit/file-reference
                          ::link/checkout
                          ::link/mobile-app-checkout
                          ::link/refunds
                          ::link/chargebacks
                          ::link/captures
                          ::link/settlement
                          ::link/order
                          ::link/change-payment-state
                          ::link/mandate
                          ::link/subscription
                          ::link/customer]))

(defmethod payment-spec :creditcard
  [_]
  (common/only-keys :req [::payment/resource
                          ::payment/id
                          ::payment/mode
                          ::payment/created-at
                          ::payment/status
                          ::payment/amount
                          ::payment/description
                          ::payment/redirect-url
                          ::payment/method
                          ::payment/profile-id
                          ::link/self
                          ::link/dashboard
                          ::link/documentation]
                    :opt [::payment/is-cancelable
                          ::payment/authorized-at
                          ::payment/paid-at
                          ::payment/canceled-at
                          ::payment/expires-at
                          ::payment/expired-at
                          ::payment/failed-at
                          ::payment/amount-refunded
                          ::payment/amount-remaining
                          ::payment/amount-captured
                          ::payment/amount-charged-back
                          ::payment/settlement-amount
                          ::payment/cancel-url
                          ::payment/webhook-url
                          ::payment/locale
                          ::payment/country-code
                          ::payment/restrict-payment-methods-to-country
                          ::payment/metadata
                          ::payment/settlement-id
                          ::payment/order-id
                          ::payment/sequence-type
                          ::payment/customer-id
                          ::payment/mandate-id
                          ::payment/subscription-id
                          ::embedded
                          ::creditcard/card-holder
                          ::creditcard/card-number
                          ::creditcard/card-fingerprint
                          ::creditcard/card-audience
                          ::creditcard/card-label
                          ::creditcard/card-expiry-date
                          ::creditcard/card-country-code
                          ::creditcard/card-security
                          ::creditcard/fee-region
                          ::creditcard/failure-reason
                          ::creditcard/failure-message
                          ::creditcard/wallet
                          ::link/checkout
                          ::link/mobile-app-checkout
                          ::link/refunds
                          ::link/chargebacks
                          ::link/captures
                          ::link/settlement
                          ::link/order
                          ::link/change-payment-state
                          ::link/mandate
                          ::link/subscription
                          ::link/customer]))

(defmethod payment-spec :ideal
  [_]
  (common/only-keys :req [::payment/resource
                          ::payment/id
                          ::payment/mode
                          ::payment/created-at
                          ::payment/status
                          ::payment/amount
                          ::payment/description
                          ::payment/redirect-url
                          ::payment/method
                          ::payment/profile-id
                          ::link/self
                          ::link/dashboard
                          ::link/documentation]
                    :opt [::payment/is-cancelable
                          ::payment/authorized-at
                          ::payment/paid-at
                          ::payment/canceled-at
                          ::payment/expires-at
                          ::payment/expired-at
                          ::payment/failed-at
                          ::payment/amount-refunded
                          ::payment/amount-remaining
                          ::payment/amount-captured
                          ::payment/amount-charged-back
                          ::payment/settlement-amount
                          ::payment/cancel-url
                          ::payment/webhook-url
                          ::payment/locale
                          ::payment/country-code
                          ::payment/restrict-payment-methods-to-country
                          ::payment/metadata
                          ::payment/settlement-id
                          ::payment/order-id
                          ::payment/sequence-type
                          ::payment/customer-id
                          ::payment/mandate-id
                          ::payment/subscription-id
                          ::embedded
                          ::ideal/consumer-name
                          ::ideal/consumer-account
                          ::ideal/consumer-bic
                          ::link/checkout
                          ::link/mobile-app-checkout
                          ::link/refunds
                          ::link/chargebacks
                          ::link/captures
                          ::link/settlement
                          ::link/order
                          ::link/change-payment-state
                          ::link/mandate
                          ::link/subscription
                          ::link/customer]))

(defmethod payment-spec :paypal
  [_]
  (common/only-keys :req [::payment/resource
                          ::payment/id
                          ::payment/mode
                          ::payment/created-at
                          ::payment/status
                          ::payment/amount
                          ::payment/description
                          ::payment/redirect-url
                          ::payment/method
                          ::payment/profile-id
                          ::link/self
                          ::link/dashboard
                          ::link/documentation]
                    :opt [::payment/is-cancelable
                          ::payment/authorized-at
                          ::payment/paid-at
                          ::payment/canceled-at
                          ::payment/expires-at
                          ::payment/expired-at
                          ::payment/failed-at
                          ::payment/amount-refunded
                          ::payment/amount-remaining
                          ::payment/amount-captured
                          ::payment/amount-charged-back
                          ::payment/settlement-amount
                          ::payment/cancel-url
                          ::payment/webhook-url
                          ::payment/locale
                          ::payment/country-code
                          ::payment/restrict-payment-methods-to-country
                          ::payment/metadata
                          ::payment/settlement-id
                          ::payment/order-id
                          ::payment/sequence-type
                          ::payment/customer-id
                          ::payment/mandate-id
                          ::payment/subscription-id
                          ::embedded
                          ::paypal/paypal-reference
                          ::paypal/paypal-payer-id
                          ::paypal/seller-protection
                          ::paypal/paypal-fee
                          ::link/checkout
                          ::link/mobile-app-checkout
                          ::link/refunds
                          ::link/chargebacks
                          ::link/captures
                          ::link/settlement
                          ::link/order
                          ::link/change-payment-state
                          ::link/mandate
                          ::link/subscription
                          ::link/customer]))

(defmethod payment-spec nil
  [_]
  (common/only-keys :req [::payment/resource
                          ::payment/id
                          ::payment/mode
                          ::payment/created-at
                          ::payment/status
                          ::payment/amount
                          ::payment/description
                          ::payment/redirect-url
                          ::payment/method
                          ::payment/profile-id
                          ::link/self
                          ::link/dashboard
                          ::link/documentation]
                    :opt [::payment/is-cancelable
                          ::payment/authorized-at
                          ::payment/paid-at
                          ::payment/canceled-at
                          ::payment/expires-at
                          ::payment/expired-at
                          ::payment/failed-at
                          ::payment/amount-refunded
                          ::payment/amount-remaining
                          ::payment/amount-captured
                          ::payment/amount-charged-back
                          ::payment/settlement-amount
                          ::payment/cancel-url
                          ::payment/webhook-url
                          ::payment/locale
                          ::payment/country-code
                          ::payment/restrict-payment-methods-to-country
                          ::payment/metadata
                          ::payment/settlement-id
                          ::payment/order-id
                          ::payment/sequence-type
                          ::payment/customer-id
                          ::payment/mandate-id
                          ::payment/subscription-id
                          ::embedded
                          ::link/checkout
                          ::link/mobile-app-checkout
                          ::link/refunds
                          ::link/chargebacks
                          ::link/captures
                          ::link/settlement
                          ::link/order
                          ::link/change-payment-state
                          ::link/mandate
                          ::link/subscription
                          ::link/customer]))

(s/def ::payment (s/multi-spec payment-spec ::payment/method))

(s/def ::payments
  (s/coll-of ::payment :distinct true :into []))

(s/def ::payments-list
  (common/only-keys :req [::payments
                          ::pagination/count]
                    :opt [::pagination/next
                          ::pagination/previous
                          ::pagination/self]))

(s/def ::embed-val #{:captures
                     :refunds
                     :chargebacks})

(s/def ::embed
  (s/coll-of ::embed-val :into [] :distinct true))

;;; Mandate

(defmulti mandate-spec ::mandate/method)

(defmethod mandate-spec :directdebit
  [_]
  (common/only-keys :req [::mandate/resource
                          ::mandate/id
                          ::mandate/mode
                          ::mandate/status
                          ::mandate/method
                          ::mandate/mandate-reference
                          ::mandate/created-at
                          ::directdebit/consumer-name
                          ::directdebit/consumer-account
                          ::directdebit/consumer-bic
                          ::link/self
                          ::link/customer
                          ::link/documentation]
                    :opt [::mandate/signature-date]))

(defmethod mandate-spec :creditcard
  [_]
  (common/only-keys :req [::mandate/resource
                          ::mandate/id
                          ::mandate/mode
                          ::mandate/status
                          ::mandate/method
                          ::mandate/mandate-reference
                          ::mandate/created-at
                          ::creditcard/card-fingerprint
                          ::creditcard/card-expiry-date
                          ::link/self
                          ::link/customer
                          ::link/documentation]
                    :opt [::creditcard/card-number
                          ::creditcard/card-holder
                          ::creditcard/card-label
                          ::mandate/signature-date]))

(defmethod mandate-spec :paypal
  [_]
  (common/only-keys :req [::mandate/resource
                          ::mandate/id
                          ::mandate/mode
                          ::mandate/status
                          ::mandate/method
                          ::mandate/mandate-reference
                          ::mandate/created-at
                          ::paypal/consumer-name
                          ::paypal/consumer-account
                          ::link/self
                          ::link/customer
                          ::link/documentation]
                    :opt [::mandate/signature-date]))

(s/def ::mandate (s/multi-spec mandate-spec ::mandate/method))

(s/def ::mandates
  (s/coll-of ::mandate :distinct true :into []))

(s/def ::mandates-list
  (common/only-keys :req [::mandates
                          ::pagination/count]
                    :opt [::pagination/next
                          ::pagination/previous
                          ::pagination/self]))

;;; Subscription

(s/def ::subscription
  (common/only-keys :req [::subscription/resource
                          ::subscription/id
                          ::subscription/mode
                          ::subscription/created-at
                          ::subscription/status
                          ::subscription/amount
                          ::subscription/interval
                          ::subscription/start-date
                          ::subscription/description
                          ::subscription/method
                          ::link/self
                          ::link/customer]
                    :opt [::subscription/next-payment-date
                          ::subscription/times
                          ::subscription/times-remaining
                          ::subscription/mandate-id
                          ::subscription/canceled-at
                          ::subscription/webhook-url
                          ::subscription/metadata
                          ::link/profile
                          ::link/payments
                          ::link/documentation]))

(s/def ::subscriptions
  (s/coll-of ::subscription :distinct true :into []))

(s/def ::subscriptions-list
  (common/only-keys :req [::subscriptions
                          ::pagination/count]
                    :opt [::pagination/next
                          ::pagination/previous
                          ::pagination/self]))
