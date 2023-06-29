(ns com.adgoji.mollie.pagination
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::count nat-int?)
(s/def ::from string?)
(s/def ::limit nat-int?)
(s/def ::next (s/nilable (s/keys :req [::limit] :opt [::from])))
(s/def ::previous (s/nilable (s/keys :req [::limit] :opt [::from])))
(s/def ::self (s/keys :req [::limit] :opt [::from]))
