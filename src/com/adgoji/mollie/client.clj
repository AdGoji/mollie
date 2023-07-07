(ns com.adgoji.mollie.client
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.utils.spec :as spec]
   [hato.client :as hc])
  (:import
   (java.net URI URISyntaxException)))

(def ^:private default-base-url "https://api.mollie.com")

(defn new-client
  [{:keys [base-url api-key check-response]
    :or   {base-url       default-base-url
           check-response false}}]
  {:client         (hc/build-http-client
                     {:connect-timeout 10000
                      :redirect-policy :always})
   :base-url       base-url
   :api-key        api-key
   :check-response check-response})

(defn- parse-json
  [body]
  (with-open [r (io/reader body)]
    (json/read r {:eof-error? false
                  :bigdec     true
                  :key-fn     csk/->kebab-case-keyword})))

(defn- request
  ([method req endpoint]
   (request method req endpoint {}))
  ([method {:keys [client base-url api-key check-response]} endpoint opts]
   (let [{:keys [body query-params response-transformer spec]
          :or   {response-transformer identity
                 spec                 any?}}
         opts

         response
         (-> (hc/request
               (cond-> {:method            method
                        :http-client       client
                        :url               (str base-url endpoint)
                        :content-type      :json
                        :headers           {"authorization" (str "Bearer " api-key)}
                        :as                :stream
                        :throw-exceptions? false}
                 body         (assoc :body (json/write-str body {:key-fn csk/->camelCaseString}))
                 query-params (assoc :query-params query-params)))
             (update :body parse-json))]
     (if (< (:status response) 400)
       (cond-> (:body response)
         :always        (response-transformer)
         check-response (spec/check spec))
       (throw (ex-info "Mollie API error" response))))))

(def http-get (partial request :get))
(def http-post (partial request :post))
(def http-patch (partial request :patch))
(def http-delete (partial request :delete))

(defn- fetch-single
  [client endpoint {:keys [query-params] :as opts} page-params]
  (let [new-query-params (cond-> {}
                           (seq query-params) (into query-params)
                           (seq page-params)  (into page-params))]
    (http-get client endpoint (assoc opts :query-params new-query-params))))

(defn extract-page-params
  [uri]
  (when uri
    (try
      (into {}
            (comp
              (map #(str/split % #"="))
              (map (fn [[k v]]
                     [(keyword "com.adgoji.mollie.pagination" k) v])))
            (str/split (.getQuery (URI. uri)) #"&"))
      (catch URISyntaxException _ nil))))

(defn fetch-all
  [items-key client endpoint opts]
  (let [items (->> (iteration (partial fetch-single client endpoint opts)
                              :kf ::pagination/next
                              :vf items-key
                              :initk {})
                   (into [] cat))]
    {items-key          items
     ::pagination/count (count items)}))
