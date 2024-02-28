(ns com.adgoji.mollie.utils
  (:require
   [clojure.edn :as edn]
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.api :as mollie.api]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.mollie.mandate :as mandate]
   [clojure.spec.test.alpha :as spec.test]))

(def ^:private secrets-file "/.adgoji-mollie-secrets.edn")
(def ^:private secrets
  (try
    (-> (str (System/getenv "HOME") secrets-file)
        slurp
        edn/read-string)
    (catch Exception _ {})))

(def api-key
  (or (System/getenv "MOLLIE_API_KEY")
      (:api-key secrets)))

(def profile-id
  (or (System/getenv "MOLLIE_PROFILE_ID")
      (:profile-id secrets)))

(def test-purpose "api-client-test")

(def default-metadata {:purpose test-purpose})

(def ^:dynamic *mollie-client* nil)

(defn new-client []
  (mollie.api/new-client {:api-key           api-key
                          :check-response?   true
                          :throw-exceptions? true}))

(defn cleanup-payments [customer-id]
  (doseq [{payment-id ::payment/id}
          (->> (mollie.api/get-payments-list *mollie-client* customer-id {})
               (::mollie/payments)
               (sequence (filter ::payment/is-cancelable)))]
    (mollie.api/cancel-payment-by-id *mollie-client* payment-id)))

(defn cleanup-subscriptions [customer-id]
  (doseq [{subscription-id ::subscription/id}
          (->> (mollie.api/get-subscriptions-list *mollie-client* customer-id {})
               (::mollie/subscriptions)
               (sequence (remove (fn [sub]
                                   (= (::subscription/status sub) :canceled)))))]
    (mollie.api/cancel-subscription-by-id *mollie-client* customer-id subscription-id)))

(defn cleanup-mandates [customer-id]
  (doseq [{mandate-id ::mandate/id}
          (->> (mollie.api/get-mandates-list *mollie-client* customer-id {})
               (::mollie/mandates))]
    (mollie.api/revoke-mandate-by-id *mollie-client* customer-id mandate-id)))

(defn- customers-to-cleaunup []
  (->> (mollie.api/get-customers-list *mollie-client* {})
       (::mollie/customers)
       (sequence (filter (fn [customer]
                           (= test-purpose (get-in customer [::customer/metadata :purpose])))))))
(defn mollie-cleanup []
  (doseq [{customer-id ::customer/id} (customers-to-cleaunup)]
    (cleanup-payments customer-id)
    (cleanup-subscriptions customer-id)
    (cleanup-mandates customer-id)
    (mollie.api/delete-customer-by-id *mollie-client* customer-id)))

(defn with-mollie-client [f]
  (spec.test/instrument)
  (binding [*mollie-client* (new-client)]
    (try
      (f)
      (finally
        (mollie-cleanup)))))
