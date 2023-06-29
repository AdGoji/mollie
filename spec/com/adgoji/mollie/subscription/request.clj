(ns com.adgoji.mollie.subscription.request
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.common :as common])
  (:import
   (java.time LocalDate)))

(s/def ::amount (s/keys :req-un [::amount/value ::amount/currency]))
(s/def ::times int?)
(s/def ::interval (partial re-matches common/interval-regex))
(s/def ::start-date (partial instance? LocalDate))
(s/def ::description string?)
(s/def ::method (s/nilable #{:creditcard :directdebit :paypal}))
(s/def ::mandate-id ::mandate/id)
(s/def ::webhook-url string?)
(s/def ::metadata ::common/metadata)
(s/def ::profile-id ::common/profile-id)
(s/def ::application-fee
  (s/keys :req-un [::amount ::description]))

(s/def ::create
  (s/merge (s/keys :req-un [::amount
                            ::interval
                            ::description]
                   :opt-un [::times
                            ::start-date
                            ::method
                            ::mandate-id
                            ::webhook-url
                            ::metadata
                            ::profile-id
                            ::application-fee])
           (s/map-of #{:amount
                       :interval
                       :description
                       :times
                       :start-date
                       :method
                       :mandate-id
                       :webhook-url
                       :metadata
                       :profile-id
                       :application-fee}
                     any?)))

(s/def ::update
  (s/merge (s/keys :opt-un [::amount
                            ::description
                            ::interval
                            ::mandate-id
                            ::metadata
                            ::start-date
                            ::times
                            ::webhook-url])
           (s/map-of #{:amount
                       :description
                       :interval
                       :mandate-id
                       :metadata
                       :start-date
                       :times
                       :webhook-url}
                     any?)))
