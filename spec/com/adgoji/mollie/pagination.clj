(ns com.adgoji.mollie.pagination
  (:require
   [clojure.spec.alpha :as s]))

(defn- limit?
  [limit-value]
  (let [limit-parsed (parse-long limit-value)]
    (if (and limit-parsed (nat-int? limit-parsed))
      limit-parsed
      ::s/invalid)))

(s/def ::count nat-int?)
(s/def ::from string?)

(defmulti limit-spec type)
(defmethod limit-spec String [_] (s/conformer limit?))
(defmethod limit-spec Long [_] nat-int?)

(s/def ::limit (s/multi-spec limit-spec :limit))

(s/def ::next (s/nilable (s/keys :req [::limit] :opt [::from])))
(s/def ::previous (s/nilable (s/keys :req [::limit] :opt [::from])))
(s/def ::self (s/keys :req [::limit] :opt [::from]))

(s/def ::opts (s/keys :opt-un [::from ::limit]))
