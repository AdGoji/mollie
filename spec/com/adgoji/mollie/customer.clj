(ns com.adgoji.mollie.customer
  (:require
   [com.adgoji.common :as common]
   [clojure.spec.alpha :as s]))

(s/def ::id string?)
(s/def ::email (s/nilable ::common/email))
(s/def ::name (s/nilable string?))
(s/def ::locale (s/nilable ::common/locale))
(s/def ::metadata ::common/metadata)
(s/def ::created-at inst?)
(s/def ::mode ::common/mode)
(s/def ::resource #{"customer"})
