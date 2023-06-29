(ns com.adgoji.mollie.amount
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::currency string?)
(s/def ::value decimal?)
