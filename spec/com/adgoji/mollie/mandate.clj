(ns com.adgoji.mollie.mandate
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.creditcard :as creditcard]
   [com.adgoji.mollie.directdebit :as directdebit]
   [com.adgoji.mollie.paypal :as paypal])
  (:import
   (java.time LocalDate)))

(s/def ::resource #{"mandate"})
(s/def ::id string?)
(s/def ::mode ::common/mode)
(s/def ::status #{:valid :pending :invalid})
(s/def ::method #{:directdebit :creditcard :paypal})

(s/def ::details-directdebit
  (s/keys :req [::directdebit/consumer-name
                ::directdebit/consumer-account
                ::directdebit/consumer-bic]))

(s/def ::details-creditcard
  (s/keys :req [::creditcard/card-holder
                ::creditcard/card-number
                ::creditcard/card-label
                ::creditcard/card-fingerprint
                ::creditcard/card-expiry-date]))

(s/def ::details-paypal
  (s/keys :req [::paypal/consumer-name
                ::paypal/consumer-account]))

(s/def ::details
  (s/or :directdebit ::details-directdebit
        :creditcard ::details-creditcard
        :paypal ::details-paypal))

(s/def ::mandate-reference (s/nilable string?))
(s/def ::signature-date (partial instance? LocalDate))
(s/def ::created-at inst?)
