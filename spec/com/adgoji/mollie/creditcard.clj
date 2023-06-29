(ns com.adgoji.mollie.creditcard
  (:require
   [clojure.spec.alpha :as s])
  (:import
   (java.time LocalDate)))

(s/def ::card-holder string?)
(s/def ::card-number string?)
(s/def ::card-label
  (s/nilable #{"American Express"
               "Carta Si"
               "Carte Bleue"
               "Dankort"
               "Diners Club"
               "Discover"
               "JCB"
               "Laser"
               "Maestro"
               "Mastercard"
               "Unionpay"
               "Visa"}))
(s/def ::card-fingerprint string?)
(s/def ::card-expiry-date (partial instance? LocalDate))
(s/def ::card-audience (s/nilable #{:consumer :business}))
(s/def ::card-country-code string?)
(s/def ::card-security #{:normal :3dsecure})
(s/def ::fee-region
  #{"american-express"
    "amex-intra-eea"
    "carte-bancaire"
    "intra-eu"
    "intra-eu-corporate"
    "domestic"
    "maestro"
    "other"})
(s/def ::failure-reason
  #{:authentication-abandoned
    :authentication-failed
    :authentication-required
    :authentication-unavailable-acs
    :card-declined
    :card-expired
    :inactive-card
    :insufficient-funds
    :invalid-cvv
    :invalid-card-holder-name
    :invalid-card-number
    :invalid-card-type
    :possible-fraud
    :refused-by-issuer
    :unknown-reason})
(s/def ::failure-message string?)
(s/def ::wallet #{:applepay})
