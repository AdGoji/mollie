(ns com.adgoji.mollie.address
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]))

(s/def ::organization-name string?)
(s/def ::title string?)
(s/def ::given-name string?)
(s/def ::family-name string?)
(s/def ::email ::common/email)
(s/def ::phone string?)
(s/def ::street-and-number string?)
(s/def ::street-additional string?)
(s/def ::postal-code string?)
(s/def ::city string?)
(s/def ::region string?)
(s/def ::country string?)
