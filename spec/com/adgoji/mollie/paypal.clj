(ns com.adgoji.mollie.paypal
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.amount :as amount]))

(s/def ::consumer-name string?)
(s/def ::consumer-account string?)
(s/def ::session-id string?)
(s/def ::digital-goods boolean?)
(s/def ::paypal-reference string?)
(s/def ::paypal-payer-id string?)
(s/def ::seller-protection string?)
(s/def ::paypal-fee (s/keys :req [::amount/currency ::amount/value]))
