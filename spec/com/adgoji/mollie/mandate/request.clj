(ns com.adgoji.mollie.mandate.request
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]))

(s/def ::method #{:directdebit :paypal})
(s/def ::consumer-name string?)
(s/def ::consumer-account string?)
(s/def ::consumer-bic string?)
(s/def ::consumer-email ::common/email)
(s/def ::signature-date inst?)
(s/def ::mandate-reference string?)
(s/def ::paypal-billing-agreement-id string?)

(defmulti create-spec :method)

(defmethod create-spec :directdebit
  [_]
  (common/only-keys :req-un [::method
                             ::consumer-name
                             ::consumer-account]
                    :opt-un [::consumer-bic
                             ::signature-date
                             ::mandate-reference]))

(defmethod create-spec :paypal
  [_]
  (common/only-keys :req-un [::method
                             ::consumer-name
                             ::consumer-account]
                    :opt-un [::consumer-bic
                             ::signature-date
                             ::mandate-reference]))

(s/def ::create (s/multi-spec create-spec :method))
