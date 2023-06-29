(ns com.adgoji.mollie.payment
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.common :as common]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.directdebit :as directdebit]
   [com.adgoji.mollie.creditcard :as creditcard]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.ideal :as ideal]))

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
(s/def ::subscription-id string?)       ;TODO: use real ::subscription/id

(s/def ::details-directdebit
  (s/merge (s/keys :req [::directdebit/transfer-reference
                         ::directdebit/creditor-identifier
                         ::directdebit/consumer-name
                         ::directdebit/consumer-account
                         ::directdebit/consumer-bic
                         ::directdebit/due-date]
                   :opt [::directdebit/signature-date
                         ::directdebit/bank-reason-code
                         ::directdebit/bank-reason
                         ::directdebit/end-to-end-identifier
                         ::directdebit/mandate-reference
                         ::directdebit/batch-reference
                         ::directdebit/file-reference])
           (s/map-of #{::directdebit/transfer-reference
                       ::directdebit/creditor-identifier
                       ::directdebit/consumer-name
                       ::directdebit/consumer-account
                       ::directdebit/consumer-bic
                       ::directdebit/due-date
                       ::directdebit/signature-date
                       ::directdebit/bank-reason-code
                       ::directdebit/bank-reason
                       ::directdebit/end-to-end-identifier
                       ::directdebit/mandate-reference
                       ::directdebit/batch-reference
                       ::directdebit/file-reference}
                     any?)))

(s/def ::details-creditcard
  (s/merge (s/keys :opt [::creditcard/card-holder
                         ::creditcard/card-number
                         ::creditcard/card-fingerprint
                         ::creditcard/card-audience
                         ::creditcard/card-label
                         ::creditcard/card-expiry-date
                         ::creditcard/card-country-code
                         ::creditcard/card-security
                         ::creditcard/fee-region
                         ::creditcard/failure-reason
                         ::creditcard/failure-message
                         ::creditcard/wallet])
           (s/map-of #{::creditcard/card-holder
                       ::creditcard/card-number
                       ::creditcard/card-fingerprint
                       ::creditcard/card-audience
                       ::creditcard/card-label
                       ::creditcard/card-expiry-date
                       ::creditcard/card-country-code
                       ::creditcard/card-security
                       ::creditcard/fee-region
                       ::creditcard/failure-reason
                       ::creditcard/failure-message
                       ::creditcard/wallet}
                     any?)))

(s/def ::details-ideal
  (s/merge (s/keys :opt [::ideal/consumer-name
                         ::ideal/consumer-account
                         ::ideal/consumer-bic])
           (s/map-of #{::ideal/consumer-name
                       ::ideal/consumer-account
                       ::ideal/consumer-bic}
                     any?)))

(s/def ::details
  (s/nilable
    (s/or :directdebit ::details-directdebit
          :creditcard ::details-creditcard
          :ideal ::details-ideal)))
