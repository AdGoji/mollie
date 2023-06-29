(ns com.adgoji.mollie.payment.request
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.mandate :as mandate])
  (:import
   (java.time LocalDate)))

(s/def ::amount (s/keys :req-un [::amount/currency ::amount/value]))
(s/def ::description string?)
(s/def ::redirect-url string?)
(s/def ::cancel-url string?)
(s/def ::webhook-url string?)
(s/def ::locale ::common/locale)
(s/def ::method (s/or :single ::common/payment-method
                      :list (s/coll-of ::common/payment-method :distinct true :into [])))
(s/def ::restrict-payment-methods-to-country string?)
(s/def ::metadata map?)
(s/def ::sequence-type #{:oneoff :first :recurring})
(s/def ::customer-id ::customer/id)
(s/def ::mandate-id ::mandate/id)
(s/def ::issuer string?)
(s/def ::profile-id ::common/profile-id)
(s/def ::billing-email ::common/email)
(s/def ::due-date (partial instance? LocalDate))

(s/def ::create-oneoff
  (s/keys :req-un [::amount
                   ::description
                   ::redirect-url]
          :opt-un [::cancel-url
                   ::webhook-url
                   ::locale
                   ::method
                   ::restrict-payment-methods-to-country
                   ::metadata
                   ::sequence-type
                   ::customer-id
                   ::mandate-id
                   ::issuer
                   ::billing-email
                   ::due-date
                   ::profile-id]))

(s/def ::create-first
  (s/keys :req-un [::amount
                   ::description
                   ::redirect-url
                   ::sequence-type
                   ::customer-id]
          :opt-un [::cancel-url
                   ::webhook-url
                   ::locale
                   ::method
                   ::restrict-payment-methods-to-country
                   ::metadata
                   ::mandate-id
                   ::issuer
                   ::billing-email
                   ::due-date
                   ::profile-id]))

(s/def ::create-recurring
  (s/keys :req-un [::amount
                   ::description
                   ::sequence-type]
          :opt-un [::customer-id
                   ::redirect-url
                   ::cancel-url
                   ::webhook-url
                   ::locale
                   ::method
                   ::restrict-payment-methods-to-country
                   ::metadata
                   ::mandate-id
                   ::issuer
                   ::billing-email
                   ::due-date
                   ::profile-id]))

(s/def ::create
  (s/merge (s/or :oneoff ::create-oneoff
                 :first ::create-first
                 :recurring ::create-recurring)
           (s/map-of #{:amount
                       :description
                       :redirect-url
                       :cancel-url
                       :webhook-url
                       :locale
                       :method
                       :restrict-payment-methods-to-country
                       :metadata
                       :sequence-type
                       :customer-id
                       :mandate-id
                       :issuer
                       :billing-email
                       :due-date
                       :profile-id}
                     any?)))

(s/def ::update
  (s/merge (s/keys :opt-un [::description
                            ::redirect-url
                            ::cancel-url
                            ::webhook-url
                            ::locale
                            ::method
                            ::restrict-payment-methods-to-country
                            ::metadata
                            ::issuer
                            ::billing-email
                            ::due-date])
           (s/map-of #{:amount
                       :description
                       :redirect-url
                       :cancel-url
                       :webhook-url
                       :locale
                       :method
                       :restrict-payment-methods-to-country
                       :metadata
                       :issuer
                       :billing-email
                       :due-date}
                     any?)))
