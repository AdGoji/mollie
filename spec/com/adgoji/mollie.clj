(ns com.adgoji.mollie
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.subscription :as subscription]))

;;; Customer

(s/def ::customer
  (s/merge (s/keys :req [::customer/id
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
                         ::link/payments])
           (s/map-of #{::customer/id
                       ::customer/email
                       ::customer/name
                       ::customer/locale
                       ::customer/metadata
                       ::customer/resource
                       ::customer/created-at
                       ::customer/mode
                       ::link/self
                       ::link/dashboard
                       ::link/mandates
                       ::link/subscriptions
                       ::link/payments
                       ::link/documentation}
                     any?)))

(s/def ::customers
  (s/coll-of ::customer :distinct true :into []))

(s/def ::customers-list
  (s/merge (s/keys :req [::customers
                         ::pagination/count]
                   :opt [::pagination/next
                         ::pagination/previous
                         ::pagination/self])
           (s/map-of #{::customers
                       ::pagination/count
                       ::pagination/next
                       ::pagination/previous
                       ::pagination/self}
                     any?)))

;;; Payment

(s/def ::payment
  (s/merge (s/keys :req [::payment/resource
                         ::payment/id
                         ::payment/mode
                         ::payment/created-at
                         ::payment/status
                         ::payment/amount
                         ::payment/description
                         ::payment/redirect-url
                         ::payment/method
                         ::payment/profile-id
                         ::payment/details
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
                         ::link/customer])
           (s/map-of #{::payment/resource
                       ::payment/id
                       ::payment/mode
                       ::payment/created-at
                       ::payment/status
                       ::payment/amount
                       ::payment/description
                       ::payment/redirect-url
                       ::payment/method
                       ::payment/profile-id
                       ::payment/details
                       ::link/self
                       ::link/dashboard
                       ::link/documentation
                       ::payment/is-cancelable
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
                       ::link/customer}
                     any?)))

(s/def ::payments
  (s/coll-of ::payment :distinct true :into []))

(s/def ::payments-list
  (s/merge (s/keys :req [::payments
                         ::pagination/count]
                   :opt [::pagination/next
                         ::pagination/previous
                         ::pagination/self])
           (s/map-of #{::payments
                       ::pagination/count
                       ::pagination/next
                       ::pagination/previous
                       ::pagination/self}
                     any?)))

;;; Mandate

(s/def ::mandate
  (s/merge (s/keys :req [::mandate/resource
                         ::mandate/id
                         ::mandate/mode
                         ::mandate/status
                         ::mandate/method
                         ::mandate/details
                         ::mandate/mandate-reference
                         ::mandate/signature-date
                         ::mandate/created-at
                         ::link/self
                         ::link/customer
                         ::link/documentation])
           (s/map-of #{::mandate/resource
                       ::mandate/id
                       ::mandate/mode
                       ::mandate/status
                       ::mandate/method
                       ::mandate/details
                       ::mandate/mandate-reference
                       ::mandate/signature-date
                       ::mandate/created-at
                       ::link/self
                       ::link/customer
                       ::link/documentation}
                     any?)))

(s/def ::mandates
  (s/coll-of ::mandate :distinct true :into []))

(s/def ::mandates-list
  (s/merge (s/keys :req [::mandates
                         ::pagination/count]
                   :opt [::pagination/next
                         ::pagination/previous
                         ::pagination/self])
           (s/map-of #{::mandates
                       ::pagination/count
                       ::pagination/next
                       ::pagination/previous
                       ::pagination/self}
                     any?)))

;;; Subscription

(s/def ::subscription
  (s/merge (s/keys :req [::subscription/resource
                         ::subscription/id
                         ::subscription/mode
                         ::subscription/created-at
                         ::subscription/status
                         ::subscription/amount
                         ::subscription/times
                         ::subscription/times-remaining
                         ::subscription/interval
                         ::subscription/start-date
                         ::subscription/description
                         ::subscription/method
                         ::link/self
                         ::link/customer]
                   :opt [::subscription/next-payment-date
                         ::subscription/mandate-id
                         ::subscription/canceled-at
                         ::subscription/webhook-url
                         ::subscription/metadata
                         ::link/profile
                         ::link/payments
                         ::link/documentation])
           (s/map-of #{::subscription/resource
                       ::subscription/id
                       ::subscription/mode
                       ::subscription/created-at
                       ::subscription/status
                       ::subscription/amount
                       ::subscription/times
                       ::subscription/times-remaining
                       ::subscription/interval
                       ::subscription/start-date
                       ::subscription/description
                       ::subscription/method
                       ::link/self
                       ::link/customer
                       ::subscription/next-payment-date
                       ::subscription/mandate-id
                       ::subscription/canceled-at
                       ::subscription/webhook-url
                       ::subscription/metadata
                       ::link/profile
                       ::link/payments
                       ::link/documentation}
                     any?)))

(s/def ::subscriptions
  (s/coll-of ::subscription :distinct true :into []))

(s/def ::subscriptions-list
  (s/merge (s/keys :req [::subscriptions
                         ::pagination/count]
                   :opt [::pagination/next
                         ::pagination/previous
                         ::pagination/self])
           (s/map-of #{::subscriptions
                       ::pagination/count
                       ::pagination/next
                       ::pagination/previous
                       ::pagination/self}
                     any?)))
