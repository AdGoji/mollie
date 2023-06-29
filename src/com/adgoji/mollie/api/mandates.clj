(ns com.adgoji.mollie.api.mandates
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie.client :as mollie.client]
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.creditcard :as creditcard]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.directdebit :as directdebit]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.mandate.request :as request]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.paypal :as paypal]
   [com.adgoji.utils.spec :as spec])
  (:import
   (java.time Instant LocalDate)))

(defn- transform-mandate
  [{:keys [resource
           id
           mode
           status
           method
           mandate-reference
           signature-date
           created-at
           links]

    {:keys [consumer-name
            consumer-account
            consumer-bic
            card-holder
            card-number
            card-label
            card-fingerprint
            card-expiry-date]} :details}]
  (let [details (case (keyword method)
                  :directdebit {::directdebit/consumer-name    consumer-name
                                ::directdebit/consumer-account consumer-account
                                ::directdebit/consumer-bic     consumer-bic}
                  :creditcard  {::creditcard/card-holder      card-holder
                                ::creditcard/card-number      card-number
                                ::creditcard/card-label       card-label
                                ::creditcard/card-fingerprint card-fingerprint
                                ::creditcard/card-expiry-date card-expiry-date}
                  :paypal      {::paypal/consumer-name    consumer-name
                                ::paypal/consumer-account consumer-account}
                  {})]
    (cond-> {::mandate/resource          resource
             ::mandate/id                id
             ::mandate/mode              (keyword mode)
             ::mandate/status            (keyword status)
             ::mandate/method            (keyword method)
             ::mandate/details           details
             ::mandate/mandate-reference mandate-reference
             ::mandate/created-at        (Instant/parse created-at)
             ::link/self                 (spec/qualify-link (:self links))
             ::link/customer             (spec/qualify-link (:customer links))
             ::link/documentation        (spec/qualify-link (:documentation links))}
      signature-date (assoc ::mandate/signature-date (LocalDate/parse signature-date)))))

(defn create
  "Create a new mandate for given `customer-id`."
  [client customer-id mandate]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::request/create mandate)]}
  (mollie.client/http-post client
                           (format "/v2/customers/%s/mandates" customer-id)
                           {:body                 mandate
                            :response-transformer transform-mandate
                            :spec                 ::mollie/mandate}))

(defn get-by-id
  "Fetch a single customer's mandate by `mandate-id`."
  [client customer-id mandate-id]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::mandate/id mandate-id)]}
  (mollie.client/http-get client
                          (format "/v2/customers/%s/mandates/%s"
                                  customer-id
                                  mandate-id)
                          {:response-transformer transform-mandate
                           :spec                 ::mollie/mandate}))

(defn revoke-by-id
  "Revoke a specific customer's mandate by `mandate-id`."
  [client customer-id mandate-id]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::mandate/id mandate-id)]}
  (mollie.client/http-delete client
                             (format "/v2/customers/%s/mandates/%s"
                                     customer-id
                                     mandate-id)))

(defn- transform-mandates
  [response]
  (let [mandates        (->> (get-in response [:embedded :mandates])
                             (into [] (map transform-mandate)))
        next-params     (-> (get-in response [:links :next :href])
                            (mollie.client/extract-page-params))
        previous-params (-> (get-in response [:links :previous :href])
                            (mollie.client/extract-page-params))
        self-params     (-> (get-in response [:links :self :href])
                            (mollie.client/extract-page-params))]
    {::mollie/mandates     mandates
     ::pagination/count    (:count response)
     ::pagination/next     next-params
     ::pagination/previous previous-params
     ::pagination/self     self-params}))

(defn get-list
  "Fetch all customer's mandates."
  [client customer-id {:keys [from limit] :as opts}]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? (s/keys :opt-un [::pagination/from ::pagination/limit]) opts)]}
  (let [fetch-fn (if limit
                   mollie.client/http-get
                   (partial mollie.client/fetch-all ::mollie/mandates))]
    (fetch-fn client
              (format "/v2/customers/%s/mandates" customer-id)
              {:response-transformer transform-mandates
               :spec                 ::mollie/mandates-list
               :query-params         (cond-> {}
                                       from  (assoc :from from)
                                       limit (assoc :limit limit))})))
