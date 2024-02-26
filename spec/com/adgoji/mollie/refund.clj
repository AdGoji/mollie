(ns com.adgoji.mollie.refund
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.settlement :as settlement]
   [com.adgoji.mollie.order :as order]))

(s/def ::resource #{"refund"})
(s/def ::id string?)
(s/def ::amount (s/keys :req [::amount/currency ::amount/value]))
(s/def ::description string?)
(s/def ::metadata map?)
(s/def ::status
  #{:queued
    :pending
    :processing
    :refunded
    :failed
    :canceled})
(s/def ::payment-id ::payment/id)
(s/def ::created-at inst?)
(s/def ::settlement-id ::settlement/id)
(s/def ::settlement-amount ::settlement/amount)
(s/def ::order-id ::order/id)
(s/def ::lines ::order/lines)
(s/def ::payment-id ::payment/id)
(s/def ::created-at inst?)
