(ns com.adgoji.mollie.order
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.address :as address]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.order-line :as order-line]
   [com.adgoji.mollie.link :as link])
  (:import
   (java.time LocalDate)))

(s/def ::resource #{"order"})
(s/def ::id string?)
(s/def ::profile-id ::common/profile-id)
(s/def ::method ::common/payment-method)
(s/def ::mode ::common/mode)
(s/def ::amount (s/keys :req [::amount/currency ::amount/value]))
(s/def ::amount-captured (s/keys :req [::amount/currency ::amount/value]))
(s/def ::amount-refunded (s/keys :req [::amount/currency ::amount/value]))
(s/def ::status
  #{:created
    :paid
    :authorized
    :canceled
    :shipping
    :completed
    :expired})
(s/def ::is-cancelable boolean?)
(s/def ::billing-address
  (s/keys :req [::address/organization-name
                ::address/title
                ::address/given-name
                ::address/family-name
                ::address/street-and-number
                ::address/city
                ::address/country
                ::address/email
                ::address/phone]
          :opt [::address/street-additional
                ::address/postal-code
                ::address/region]))
(s/def ::shopper-country-must-match-billing-country boolean?)
(s/def ::consumer-date-of-birth (partial instance? LocalDate))
(s/def ::order-number string?)
(s/def ::shipping-address
  (s/keys :req [::address/organization-name
                ::address/title
                ::address/given-name
                ::address/family-name
                ::address/street-and-number
                ::address/city
                ::address/country
                ::address/email
                ::address/phone]
          :opt [::address/street-additional
                ::address/postal-code
                ::address/region]))
(s/def ::locale ::common/locale)
(s/def ::metadata map?)
(s/def ::redirect-url string?)
(s/def ::cancel-url string?)
(s/def ::webhook-url string?)
(s/def ::created-at inst?)
(s/def ::expires-at inst?)
(s/def ::expired-at inst?)
(s/def ::paid-at inst?)
(s/def ::authorized-at inst?)
(s/def ::canceled-at inst?)
(s/def ::completed-at inst?)
(s/def ::line
  (s/keys :req [::order-line/resource
                ::order-line/id
                ::order-line/order-id
                ::order-line/type
                ::order-line/name
                ::order-line/status
                ::order-line/is-cancelable
                ::order-line/quantity
                ::order-line/quantity-shipped
                ::order-line/amount-shipped
                ::order-line/quantity-refunded
                ::order-line/amount-refunded
                ::order-line/quantity-canceled
                ::order-line/amount-canceled
                ::order-line/shippable-quantity
                ::order-line/refundable-quantity
                ::order-line/cancelable-quantity
                ::order-line/unit-price
                ::order-line/total-amount
                ::order-line/vat-rate
                ::order-line/vat-amount
                ::order-line/sku
                ::order-line/created-at
                ::link/product-url
                ::link/image-url]
          :opt [::order-line/discount-amount]))
(s/def ::lines
  (s/coll-of ::line :into [] :distinct true))
