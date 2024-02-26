(ns com.adgoji.mollie.settlement
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.amount :as amount]))

(s/def ::resource #{"settlement"})
(s/def ::id string?)
(s/def ::reference string?)
(s/def ::created-at inst?)
(s/def ::settled-at inst?)
(s/def ::status
  #{:open
    :pending
    :paidout
    :failed})
(s/def ::amount (s/keys :req [::amount/currency ::amount/value]))
(s/def ::periods map?)
