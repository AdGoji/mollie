(ns com.adgoji.mollie.mandate
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common])
  (:import
   (java.time LocalDate)))

(s/def ::resource #{"mandate"})
(s/def ::id string?)
(s/def ::mode ::common/mode)
(s/def ::status #{:valid :pending :invalid})
(s/def ::method #{:directdebit :creditcard :paypal})
(s/def ::mandate-reference (s/nilable string?))
(s/def ::signature-date (partial instance? LocalDate))
(s/def ::created-at inst?)
