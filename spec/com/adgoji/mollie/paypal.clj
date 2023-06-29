(ns com.adgoji.mollie.paypal
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::consumer-name string?)
(s/def ::consumer-account string?)
