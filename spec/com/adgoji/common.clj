(ns com.adgoji.common
  (:require
   [clojure.spec.alpha :as s]))

;;; Constants

(def ^:private email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def interval-regex #"^\d+\s(months*|weeks*|days*)")

;;; Helpers

(defmacro only-keys
  [& {:keys [req req-un opt opt-un] :as args}]
  `(s/merge (s/map-of ~(set (concat req
                                    (map (comp keyword name) req-un)
                                    opt
                                    (map (comp keyword name) opt-un)))
                      any?)
            (s/keys ~@(apply concat (vec args)))))

;;; Common

(s/def ::email (s/and string? (partial re-matches email-regex)))
(s/def ::mode #{:test :live})
(s/def ::locale string?)
(s/def ::payment-method
  #{:applepay
    :bancontact
    :banktransfer
    :belfius
    :creditcard
    :directdebit
    :eps
    :giftcard
    :giropay
    :ideal
    :kbc
    :mybank
    :paypal
    :paysafecard
    :przelewy24
    :sofort})
(s/def ::metadata (s/nilable map?))
(s/def ::profile-id string?)            ;TODO: Use real profile ID when implemented
