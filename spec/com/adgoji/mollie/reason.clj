(ns com.adgoji.mollie.reason
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::code string?)
(s/def ::description string?)
