(ns com.adgoji.mollie.payment
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.subscription :as subscription]))

(s/def ::resource #{"payment"})
(s/def ::id string?)
(s/def ::mode ::common/mode)
(s/def ::created-at inst?)
(s/def ::status
  #{:open
    :canceled
    :pending
    :authorized
    :expired
    :failed
    :paid})
(s/def ::is-cancelable boolean?)
(s/def ::authorized-at inst?)
(s/def ::paid-at inst?)
(s/def ::canceled-at inst?)
(s/def ::expires-at inst?)
(s/def ::expired-at inst?)
(s/def ::failed-at inst?)
(s/def ::amount (s/keys :req [::amount/currency ::amount/value]))
(s/def ::amount-refunded ::amount)
(s/def ::amount-remaining ::amount)
(s/def ::amount-captured ::amount)
(s/def ::amount-charged-back ::amount)
(s/def ::settlement-amount ::amount)
(s/def ::description string?)
(s/def ::redirect-url (s/nilable string?))
(s/def ::cancel-url (s/nilable string?))
(s/def ::webhook-url string?)
(s/def ::locale ::common/locale)
(s/def ::country-code string?)
(s/def ::method (s/nilable ::common/payment-method))
(s/def ::restrict-payment-methods-to-country string?)
(s/def ::metadata map?)
(s/def ::profile-id string?)
(s/def ::settlement-id string?)
(s/def ::order-id string?)
(s/def ::sequence-type #{:oneoff :first :recurring})
(s/def ::customer-id ::customer/id)
(s/def ::mandate-id ::mandate/id)
(s/def ::subscription-id ::subscription/id)
