(ns com.adgoji.mollie.refund.request
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]))

(s/def ::amount ::common/amount)
(s/def ::description string?)
(s/def ::metadata map?)
(s/def ::create
  (common/only-keys :req-un [::amount]
                    :opt-un [::description
                             ::metadata]))
