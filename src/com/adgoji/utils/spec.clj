(ns com.adgoji.utils.spec
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.amount :as amount]))

(defn check
  [v spec]
  (let [parsed (s/conform spec v)]
    (if (s/invalid? parsed)
      (throw (ex-info "Spec check failed" (s/explain-data spec v)))
      parsed)))

(defn qualify-link
  [link]
  (when link
    {::link/href (:href link)
     ::link/type (:type link)}))

(defn qualify-amount
  [amount]
  (when amount
    {::amount/currency (:currency amount)
     ::amount/value    (bigdec (:value amount))}))
