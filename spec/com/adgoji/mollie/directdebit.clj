(ns com.adgoji.mollie.directdebit
  (:require
   [clojure.spec.alpha :as s])
  (:import
   (java.time LocalDate)))

(s/def ::consumer-name string?)
(s/def ::consumer-account string?)
(s/def ::consumer-bic string?)
(s/def ::transfer-reference string?)
(s/def ::creditor-identifier string?)
(s/def ::due-date (partial instance? LocalDate))
(s/def ::signature-date (partial instance? LocalDate))
(s/def ::bank-reason-code string?)
(s/def ::bank-reason string?)
(s/def ::end-to-end-identifier string?)
(s/def ::mandate-reference string?)
(s/def ::batch-reference string?)
(s/def ::file-reference string?)
