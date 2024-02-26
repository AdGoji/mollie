(ns com.adgoji.mollie.order-line
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]))

(s/def ::resource #{"orderline"})
(s/def ::id string?)
;; Just `int?` to avoid cyclic requires with order namespace.
(s/def ::order-id int?)
(s/def ::type
  #{:physical
    :discount
    :digital
    :shipping_fee
    :store_credit
    :gift_card
    :surcharge})
(s/def ::name string?)
(s/def ::status
  #{:created
    :authorized
    :paid
    :shipping
    :canceled
    :completed})
(s/def ::is-cancelable boolean?)
(s/def ::quantity int?)
(s/def ::quantity-shipped int?)
(s/def ::amount-shipped ::common/amount)
(s/def ::quantity-refunded int?)
(s/def ::amount-refunded ::common/amount)
(s/def ::quantity-canceled int?)
(s/def ::amount-canceled ::common/amount)
(s/def ::shippable-quantity int?)
(s/def ::refundable-quantity int?)
(s/def ::cancelable-quantity int?)
(s/def ::unit-price ::common/amount)
(s/def ::discount-amount ::common/amount)
(s/def ::total-amount ::common/amount)
(s/def ::vat-rate string?)
(s/def ::vat-amount ::common/amount)
(s/def ::sku string?)
(s/def ::created-at inst?)
