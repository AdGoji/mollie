(ns com.adgoji.mollie.ideal
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::consumer-name string?)
(s/def ::consumer-account string?)
(s/def ::consumer-bic string?)
