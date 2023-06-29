(ns com.adgoji.mollie.subscription
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.mandate :as mandate])
  (:import
   (java.time LocalDate)))

(s/def ::resource #{"subscription"})
(s/def ::id string?)
(s/def ::mode ::common/mode)
(s/def ::created-at inst?)
(s/def ::status #{:pending :active :canceled :suspended :completed})
(s/def ::amount (s/keys :req [::amount/value ::amount/currency]))
(s/def ::times int?)
(s/def ::times-remaining int?)
(s/def ::interval (partial re-matches common/interval-regex))
(s/def ::start-date (partial instance? LocalDate))
(s/def ::next-payment-date (partial instance? LocalDate))
(s/def ::description string?)
(s/def ::method (s/nilable #{:creditcard :directdebit :paypal}))
(s/def ::mandate-id ::mandate/id)
(s/def ::canceled-at inst?)
(s/def ::webhook-url string?)
(s/def ::metadata ::common/metadata)
