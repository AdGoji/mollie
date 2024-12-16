(ns com.adgoji.mollie.payment.request
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.paypal :as paypal])
  (:import
   (java.time LocalDate)))

(s/def ::amount (s/keys :req-un [::amount/currency ::amount/value]))
(s/def ::description string?)
(s/def ::redirect-url string?)
(s/def ::cancel-url string?)
(s/def ::webhook-url string?)
(s/def ::locale ::common/locale)

(defmulti method-spec type)

(defmethod method-spec clojure.lang.Keyword
  [_]
  ::common/payment-method)

(defmethod method-spec clojure.lang.PersistentVector
  [_]
  (s/coll-of ::common/payment-method :distinct true :into []))

(s/def ::method (s/multi-spec method-spec :method-data-type))

(s/def ::restrict-payment-methods-to-country string?)
(s/def ::metadata map?)
(s/def ::sequence-type #{:oneoff :first :recurring})
(s/def ::customer-id ::customer/id)
(s/def ::mandate-id ::mandate/id)
(s/def ::issuer string?)
(s/def ::profile-id ::common/profile-id)
(s/def ::billing-email ::common/email)
(s/def ::due-date (partial instance? LocalDate))

(s/def ::create-oneoff
  (common/only-keys :req-un [::amount
                             ::description
                             ::redirect-url]
                    :opt-un [::cancel-url
                             ::webhook-url
                             ::locale
                             ::method
                             ::restrict-payment-methods-to-country
                             ::metadata
                             ::sequence-type
                             ::customer-id
                             ::mandate-id
                             ::issuer
                             ::billing-email
                             ::due-date
                             ::profile-id
                             ::paypal/session-id
                             ::paypal/digital-goods]))

(defmulti create-spec :sequence-type)

;; If there is no method specified, the default option is `oneoff`.
(defmethod create-spec :oneoff [_] ::create-oneoff)
(defmethod create-spec nil [_] ::create-oneoff)

;; Key `customer-id` is optional because it is possible to create a
;; first payment using a special endpoint where `customer-id` is
;; passed in path parameters.
(defmethod create-spec :first
  [_]
  (common/only-keys :req-un [::amount
                             ::description
                             ::redirect-url
                             ::sequence-type]
                    :opt-un [::customer-id
                             ::cancel-url
                             ::webhook-url
                             ::locale
                             ::method
                             ::restrict-payment-methods-to-country
                             ::metadata
                             ::mandate-id
                             ::issuer
                             ::billing-email
                             ::due-date
                             ::profile-id
                             ::paypal/session-id
                             ::paypal/digital-goods]))

(defmethod create-spec :recurring
  [_]
  (common/only-keys :req-un [::amount
                             ::description
                             ::sequence-type]
                    :opt-un [::customer-id
                             ::redirect-url
                             ::cancel-url
                             ::webhook-url
                             ::locale
                             ::method
                             ::restrict-payment-methods-to-country
                             ::metadata
                             ::mandate-id
                             ::issuer
                             ::billing-email
                             ::due-date
                             ::profile-id
                             ::paypal/session-id
                             ::paypal/digital-goods]))

(s/def ::create (s/multi-spec create-spec :sequence-type))

(s/def ::update
  (common/only-keys :opt-un [::description
                             ::redirect-url
                             ::cancel-url
                             ::webhook-url
                             ::locale
                             ::method
                             ::restrict-payment-methods-to-country
                             ::metadata
                             ::issuer
                             ::billing-email
                             ::due-date]))
