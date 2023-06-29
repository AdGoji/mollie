(ns com.adgoji.mollie.customer.request
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]))

(s/def ::name string?)
(s/def ::email ::common/email)
(s/def ::locale ::common/locale)
(s/def ::metadata ::common/metadata)
(s/def ::create
  (s/merge (s/keys :opt-un [::name
                            ::email
                            ::locale
                            ::metadata])
           (s/map-of #{:name
                       :email
                       :locale
                       :metadata}
                     any?)))
(s/def ::update ::create)
