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

(s/def ::create-directdebit
  (s/merge (s/keys :req-un [::method
                            ::consumer-name
                            ::consumer-account]
                   :opt-un [::consumer-bic
                            ::signature-date
                            ::mandate-reference])
           (s/map-of #{:method
                       :consumer-name
                       :consumer-account
                       :consumer-bic
                       :signature-date
                       :mandate-reference}
                     any?)))

(s/def ::create-paypal
  (s/merge (s/keys :req-un [::method
                            ::consumer-name
                            ::consumer-email
                            ::paypal-billing-agreement-id]
                   :opt-un [::consumer-bic
                            ::signature-date
                            ::mandate-reference])
           (s/map-of #{:method
                       :consumer-name
                       :consumer-email
                       :paypal-billing-agreement-id
                       :consumer-bic
                       :signature-date
                       :mandate-reference}
                     any?)))

(s/def ::create
  (s/or :directdebit ::create-directdebit
        :paypal ::create-paypal))
