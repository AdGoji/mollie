(ns com.adgoji.mollie.chargeback
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.reason :as reason]
   [com.adgoji.mollie.payment :as payment]))

(s/def ::resource #{"chargeback"})
(s/def ::id string?)
(s/def ::amount ::common/amount)
(s/def ::settlement-amount ::common/amount)
(s/def ::created-at inst?)
(s/def ::reason
  (common/only-keys :req [::reason/code
                          ::reason/description]))
(s/def ::reversed-at inst?)
(s/def ::payment-id ::payment/id)
