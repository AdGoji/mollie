(ns com.adgoji.mollie.api.customers
  (:require
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.client :as mollie.client]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.customer.request :as customer.request]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.utils.spec :as spec])
  (:import
   (java.time Instant)))

(defn- transform-customer
  [{:keys [id
           email
           name
           locale
           metadata
           resource
           created-at
           mode
           links]}]
  (cond-> {::customer/id         id
           ::customer/email      email
           ::customer/name       name
           ::customer/locale     locale
           ::customer/metadata   metadata
           ::customer/resource   resource
           ::customer/created-at (Instant/parse created-at)
           ::customer/mode       (keyword mode)
           ::link/self           (spec/qualify-link (:self links))
           ::link/dashboard      (spec/qualify-link (:dashboard links))
           ::link/documentation  (spec/qualify-link (:documentation links))}
    (:mandates links)      (assoc ::link/mandates       (spec/qualify-link (:mandates links)))
    (:subscriptions links) (assoc ::link/subscriptions  (spec/qualify-link (:subscriptions links)))
    (:payments links)      (assoc ::link/payments       (spec/qualify-link (:payments links)))))

(defn create
  "Create a new Mollie customer."
  [client customer]
  (let [body (spec/check customer ::customer.request/create)]
    (mollie.client/http-post client
                             "/v2/customers"
                             {:body                 body
                              :response-transformer transform-customer
                              :spec                 ::mollie/customer})))

(defn get-by-id
  "Fetch a single customer by `customer-id`."
  [client customer-id]
  (mollie.client/http-get client
                          (str "/v2/customers/"
                               (spec/check customer-id ::customer/id))
                          {:response-transformer transform-customer
                           :spec                 ::mollie/customer}))

(defn update-by-id
  "Update a single customer by `customer-id`."
  [client customer-id data]
  (let [body (spec/check data ::customer.request/update)]
    (mollie.client/http-patch client
                              (str "/v2/customers/"
                                   (spec/check customer-id ::customer/id))
                              {:body                 body
                               :response-transformer transform-customer
                               :spec                 ::mollie/customer})))

(defn delete-by-id
  "Delete a single customer by `customer-id`."
  [client customer-id]
  (mollie.client/http-delete client
                             (str "/v2/customers/"
                                  (spec/check customer-id ::customer/id))))

(defn- transform-customers
  [response]
  (let [customers       (->> (get-in response [:embedded :customers])
                             (into [] (map transform-customer)))
        next-params     (-> (get-in response [:links :next :href])
                            (mollie.client/extract-page-params))
        previous-params (-> (get-in response [:links :previous :href])
                            (mollie.client/extract-page-params))
        self-params     (-> (get-in response [:links :self :href])
                            (mollie.client/extract-page-params))]
    {::mollie/customers    customers
     ::pagination/count    (:count response)
     ::pagination/next     next-params
     ::pagination/previous previous-params
     ::pagination/self     self-params}))

(defn get-list
  "Fetch all customers."
  [client opts]
  (let [{:keys [from limit]}
        (spec/check opts ::pagination/opts)

        fetch-fn
        (if limit
          mollie.client/http-get
          (partial mollie.client/fetch-all ::mollie/customers))]
    (fetch-fn client
              "/v2/customers"
              {:response-transformer transform-customers
               :spec                 ::mollie/customers-list
               :query-params         (cond-> {}
                                       from  (assoc :from from)
                                       limit (assoc :limit limit))})))
