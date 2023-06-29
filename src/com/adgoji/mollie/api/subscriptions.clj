(ns com.adgoji.mollie.api.subscriptions
  (:require
   [clojure.spec.alpha :as s]
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.client :as mollie.client]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.mollie.subscription.request :as subscription.request]
   [com.adgoji.utils.decimal :as decimal]
   [com.adgoji.utils.spec :as spec])
  (:import
   (java.time Instant LocalDate)))

(defn- transform-subscription
  [{:keys [resource
           id
           mode
           created-at
           status
           amount
           times
           times-remaining
           interval
           start-date
           description
           method
           next-payment-date
           mandate-id
           canceled-at
           webhook-url
           metadata
           links]}]
  (cond-> {::subscription/resource        resource
           ::subscription/id              id
           ::subscription/mode            (keyword mode)
           ::subscription/created-at      (Instant/parse created-at)
           ::subscription/status          (keyword status)
           ::subscription/amount          (spec/qualify-amount amount)
           ::subscription/times           times
           ::subscription/times-remaining times-remaining
           ::subscription/interval        interval
           ::subscription/start-date      (LocalDate/parse start-date)
           ::subscription/description     description
           ::subscription/method          (keyword method)
           ::link/self                    (spec/qualify-link (:self links))
           ::link/customer                (spec/qualify-link (:customer links))}
    next-payment-date      (assoc ::subscription/next-payment-date (LocalDate/parse next-payment-date))
    mandate-id             (assoc ::subscription/mandate-id mandate-id)
    canceled-at            (assoc ::subscription/canceled-at (Instant/parse canceled-at))
    webhook-url            (assoc ::subscription/webhook-url webhook-url)
    metadata               (assoc ::subscription/metadata metadata)
    (:profile links)       (assoc ::link/profile (spec/qualify-link (:profile links)))
    (:payments links)      (assoc ::link/payments (spec/qualify-link (:payments links)))
    (:documentation links) (assoc ::link/documentation (spec/qualify-link (:documentation links)))))

(defn create
  "Create a new subscription for given `customer-id`."
  [client customer-id subscription]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::subscription.request/create subscription)]}
  (let [body (update-in subscription [:amount :value] decimal/format)]
    (mollie.client/http-post client
                             (format "/v2/customers/%s/subscriptions" customer-id)
                             {:body                 body
                              :response-transformer transform-subscription
                              :spec                 ::mollie/subscription})))

(defn get-by-id
  "Fetch a single customer's subscription by `subscription-id`."
  [client customer-id subscription-id]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::subscription/id subscription-id)]}
  (mollie.client/http-get client
                          (format "/v2/customers/%s/subscriptions/%s"
                                  customer-id
                                  subscription-id)
                          {:response-transformer transform-subscription
                           :spec                 ::mollie/subscription}))

(defn update-by-id
  "Update a single customer's subscription by `subscription-id`."
  [client customer-id subscription-id data]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::subscription/id subscription-id)
         (s/valid? ::subscription.request/update data)]}
  (let [body (cond-> data
               (:amount data) (update-in [:amount :valud] decimal/format))]
    (mollie.client/http-patch client
                              (format "/v2/customers/%s/subscriptions/%s"
                                      customer-id
                                      subscription-id)
                              {:body                 body
                               :response-transformer transform-subscription
                               :spec                 ::mollie/subscription})))

(defn cancel-by-id
  "Cancel a single customer's subscription by `subscription-id`."
  [client customer-id subscription-id]
  {:pre [(s/valid? ::customer/id customer-id)
         (s/valid? ::subscription/id subscription-id)]}
  (mollie.client/http-delete client
                             (format "/v2/customers/%s/subscriptions/%s"
                                     customer-id
                                     subscription-id)
                             {:response-transformer transform-subscription
                              :spec                 ::mollie/subscription}))

(defn- transform-subscriptions
  [response]
  (let [subscriptions   (->> (get-in response [:embedded :subscriptions])
                             (into [] (map transform-subscription)))
        next-params     (-> (get-in response [:links :next :href])
                            (mollie.client/extract-page-params))
        previous-params (-> (get-in response [:links :previous :href])
                            (mollie.client/extract-page-params))
        self-params     (-> (get-in response [:links :self :href])
                            (mollie.client/extract-page-params))]
    {::mollie/subscriptions subscriptions
     ::pagination/count     (:count response)
     ::pagination/next      next-params
     ::pagination/previous  previous-params
     ::pagination/self      self-params}))

(defn- get-list-generic
  [client endpoint {:keys [from limit]}]
  (let [fetch-fn (if limit
                   mollie.client/http-get
                   (partial mollie.client/fetch-all ::mollie/subscriptions))]
    (fetch-fn client
              endpoint
              {:response-transformer transform-subscriptions
               :spec                 ::mollie/subscriptions-list
               :query-params         (cond-> {}
                                       from  (assoc :from from)
                                       limit (assoc :limit limit))})))

(defn get-list
  "Get all subscriptions."
  ([client opts]
   {:pre [(s/valid? (s/keys :opt-un [::pagination/from ::pagination/limit]) opts)]}
   (get-list-generic client "/v2/subscriptions" opts))
  ([client customer-id opts]
   {:pre [(s/valid? ::customer/id customer-id)
          (s/valid? (s/keys :opt-un [::pagination/from ::pagination/limit]) opts)]}
   (get-list-generic client
                     (format "/v2/customers/%s/subscriptions" customer-id)
                     opts)))
