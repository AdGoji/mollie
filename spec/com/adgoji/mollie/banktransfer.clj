(ns com.adgoji.mollie.banktransfer
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::bank-name string?)
(s/def ::bank-account string?)
(s/def ::bank-bic string?)
(s/def ::transfer-reference string?)
(s/def ::due-date (partial instance? java.time.LocalDate))
