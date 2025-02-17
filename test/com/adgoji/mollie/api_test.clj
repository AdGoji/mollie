(ns com.adgoji.mollie.api-test
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [cognitect.anomalies :as anomalies]
   [com.adgoji.mollie :as mollie]
   [com.adgoji.mollie.amount :as amount]
   [com.adgoji.mollie.api :as sut]
   [com.adgoji.mollie.creditcard :as creditcard]
   [com.adgoji.mollie.customer :as customer]
   [com.adgoji.mollie.directdebit :as directdebit]
   [com.adgoji.mollie.link :as link]
   [com.adgoji.mollie.mandate :as mandate]
   [com.adgoji.mollie.pagination :as pagination]
   [com.adgoji.mollie.payment :as payment]
   [com.adgoji.mollie.subscription :as subscription]
   [com.adgoji.mollie.utils :as utils]
   [hato.client :as hc])
  (:import
   (clojure.lang ExceptionInfo)
   (java.time LocalDate)))

(use-fixtures :once utils/with-mollie-client)

(defn- ensure-customer []
  (if-let [customers (->> (sut/get-customers-list utils/*mollie-client*
                                                  {:limit 50})
                          ::mollie/customers
                          (sequence utils/test-customer-xf)
                          seq)]
    (rand-nth customers)
    (sut/create-customer utils/*mollie-client*
                         {:metadata utils/default-metadata})))

(defn- ensure-customer-without-mandate []
  (if-let [customers (->> (sut/get-customers-list utils/*mollie-client*
                                                  {:limit 50})
                          ::mollie/customers
                          (sequence (comp (remove ::link/mandates)
                                          utils/test-customer-xf))
                          seq)]
    (let [customer (rand-nth customers)]
      (if-not (->> (sut/get-mandates-list utils/*mollie-client*
                                          (::customer/id customer)
                                          {:limit 50})
                   ::mollie/mandates
                   seq)
        customer
        (recur)))
    (sut/create-customer utils/*mollie-client*
                         {:metadata utils/default-metadata})))

(defn- ensure-mandate [customer-id]
  (if-let [mandates (->> (sut/get-mandates-list utils/*mollie-client*
                                                customer-id
                                                {:limit 50})
                         ::mollie/mandates
                         (sequence (filter #(= (::mandate/status %) :valid)))
                         seq)]
    (first mandates)
    (sut/create-mandate utils/*mollie-client*
                        customer-id
                        {:method           :directdebit
                         :consumer-name    "Test Consumer"
                         :consumer-account "NL55INGB0000000000"})))

(defn- ensure-subscription []
  (let [customer     (ensure-customer)
        customer-id  (::customer/id customer)
        mandate      (ensure-mandate customer-id)
        subscription (if-let [subscriptions
                              (->> (sut/get-subscriptions-list utils/*mollie-client*
                                                               customer-id
                                                               {:limit 50})
                                   ::mollie/subscriptions
                                   (sequence (filter #(= (::subscription/status %) :active)))
                                   seq)]
                       (first subscriptions)
                       (sut/create-subscription utils/*mollie-client*
                                                customer-id
                                                {:amount
                                                 {:value    10.00M
                                                  :currency "EUR"}
                                                 :interval    "1 days"
                                                 :description (str "Auto generated subscription "
                                                                   (random-uuid))
                                                 :mandate-id  (::mandate/id mandate)}))]
    {:customer     customer
     :mandate      mandate
     :subscription subscription}))

(defn- ensure-payment
  ([]
   (if-let [payments (->> (sut/get-payments-list utils/*mollie-client*
                                                 {:limit 50})
                          ::mollie/payments
                          seq)]
     (rand-nth payments)
     (sut/create-payment utils/*mollie-client*
                         {:amount
                          {:value    10.00M
                           :currency "EUR"}
                          :description  "Auto generated test payment"
                          :redirect-url "https://example.com"})))
  ([customer-id]
   (if-let [payments (->> (sut/get-payments-list utils/*mollie-client*
                                                 customer-id
                                                 {:limit 50})
                          ::mollie/payments
                          seq)]
     (rand-nth payments)
     (sut/create-payment utils/*mollie-client*
                         {:amount
                          {:value    10.00M
                           :currency "EUR"}
                          :description  "Auto generated test payment"
                          :redirect-url "https://example.com"}
                         customer-id))))

(defn- ensure-cancelable-payment []
  (if-let [payments (->> (sut/get-payments-list utils/*mollie-client*
                                                {:limit 50})
                         ::mollie/payments
                         (sequence (filter ::payment/is-cancelable))
                         seq)]
    (rand-nth payments)
    (let [customer-id (::customer/id (ensure-customer))]
      (ensure-mandate customer-id)
      (sut/create-payment utils/*mollie-client*
                          {:amount
                           {:value    10.00M
                            :currency "EUR"}
                           :description   "Auto generated test payment"
                           :sequence-type :recurring}
                          customer-id))))

(deftest exception-handling-test
  (testing "IOException"
    (with-redefs [hc/request (fn [_] (throw (java.io.IOException. "Connection reset")))]
      (try
        (sut/create-customer utils/*mollie-client* {:metadata utils/default-metadata})
        (catch Exception e
          (is (= "Mollie API error" (ex-message e)))
          (is (= {::anomalies/category ::anomalies/fault
                  :error               {:message "Connection reset"}}
                 (ex-data e))))))))

(deftest create-customer-test
  (testing "Create customer without data"
    (let [customer (sut/create-customer utils/*mollie-client* {:metadata utils/default-metadata})]
      (is (= {::customer/resource "customer"
              ::customer/metadata utils/default-metadata
              ::customer/email    nil
              ::customer/locale   nil
              ::customer/mode     :test
              ::customer/name     nil}
             (dissoc customer
                     ::customer/id
                     ::customer/created-at
                     ::link/dashboard
                     ::link/self
                     ::link/documentation)))))

  (testing "Create customer with extra data"
    (let [metadata (into utils/default-metadata {:some "Test", :data 123})
          customer (sut/create-customer utils/*mollie-client*
                                        {:name     "Customer name"
                                         :email    "test@email.com"
                                         :locale   "en_NL"
                                         :metadata metadata})]
      (is (= {::customer/resource "customer"
              ::customer/metadata metadata
              ::customer/email    "test@email.com"
              ::customer/locale   "en_NL"
              ::customer/mode     :test
              ::customer/name     "Customer name"}
             (dissoc customer
                     ::customer/id
                     ::customer/created-at
                     ::link/dashboard
                     ::link/self
                     ::link/documentation))))))

(deftest get-customer-by-id-test
  (testing "Customer exists"
    (let [customer (ensure-customer)]
      (is (= (assoc customer
                    ::link/documentation {::link/type "text/html"
                                          ::link/href "https://docs.mollie.com/reference/v2/customers-api/get-customer"})
             (sut/get-customer-by-id utils/*mollie-client* (::customer/id customer))))))

  (testing "Customer was deleted"
    (let [customer-id (::customer/id (ensure-customer))
          client      (sut/new-client {:api-key           utils/api-key
                                       :check-response?   true
                                       :throw-exceptions? false})]
      (sut/delete-customer-by-id client customer-id)
      (is (= {::anomalies/category ::anomalies/not-found
              :error
              {:status 410
               :title  "Gone"
               :detail "The customer is no longer available"
               :links
               {:documentation
                {:href "https://docs.mollie.com/overview/handling-errors"
                 :type "text/html"}}}}
             (sut/get-customer-by-id client customer-id))))))

(deftest update-customer-by-id-test
  (let [customer     (ensure-customer)
        new-email    "new@email.com"
        new-metadata (into utils/default-metadata {:random (rand-int 1000)})]
    (is (= (assoc customer
                  ::customer/email new-email
                  ::customer/metadata new-metadata
                  ::link/documentation {::link/type "text/html"
                                        ::link/href "https://docs.mollie.com/reference/v2/customers-api/update-customer"})
           (sut/update-customer-by-id utils/*mollie-client*
                                      (::customer/id customer)
                                      {:email    new-email
                                       :metadata new-metadata})))))

(deftest delete-customer-by-id-test
  (let [customer-id (::customer/id (ensure-customer))
        client      utils/*mollie-client*]
    (is (nil? (sut/delete-customer-by-id client customer-id)))
    (is (thrown-with-msg? ExceptionInfo
                          #"Mollie API error"
                          (sut/get-customer-by-id client customer-id)))))

(deftest get-customers-list-test
  (testing "Fetch full list of customers"
    (ensure-customer)
    (let [response (sut/get-customers-list utils/*mollie-client* {})]
      (is (pos-int? (::pagination/count response)))
      (is (nil? (::pagination/next response)))
      (is (nil? (::pagination/previous response)))
      (is (nil? (::pagination/self response)))
      (is (vector? (::mollie/customers response)))
      (is (= (count (::mollie/customers response))
             (::pagination/count response)))))

  (testing "Fetch with `limit` parameter"
    (ensure-customer)
    (let [response (sut/get-customers-list utils/*mollie-client* {:limit 1})]
      (is (= 1 (::pagination/count response)))
      (is (= 1 (count (::mollie/customers response))))))

  (testing "Fetch with `from` parameter"
    (let [customer (ensure-customer)
          response (sut/get-customers-list
                    utils/*mollie-client*
                    {:from (::customer/id customer)})]
      (is (= customer (first (::mollie/customers response))))))

  (testing "Fetch with both `from` and `limit` parameters"
    (let [customer (ensure-customer)
          response (sut/get-customers-list
                    utils/*mollie-client*
                    {:from  (::customer/id customer)
                     :limit 1})]
      (is (= [customer] (::mollie/customers response))))))

(deftest create-payment-test
  (testing "Create an one-off payment without specified method"
    (let [amount-value    10.0M
          amount-currency "EUR"
          description     "Unit test payment"
          redirect-url    "https://example.com"
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description  description
                                               :redirect-url redirect-url})]
      (is (= {::payment/description   description
              ::payment/resource      "payment"
              ::payment/profile-id    utils/profile-id
              ::link/documentation
              {::link/type "text/html",
               ::link/href "https://docs.mollie.com/reference/v2/payments-api/create-payment"}
              ::payment/mode          :test
              ::payment/method        nil
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/redirect-url  redirect-url
              ::payment/status        :open
              ::payment/sequence-type :oneoff}
             (dissoc payment
                     ::payment/id
                     ::payment/created-at
                     ::payment/expires-at
                     ::link/dashboard
                     ::link/self
                     ::link/checkout)))))

  (testing "Create an one-off payment with payment method"
    (let [amount-value    10.0M
          amount-currency "EUR"
          description     "Unit test payment"
          redirect-url    "https://example.com"
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description  description
                                               :redirect-url redirect-url
                                               :method       :ideal})]
      (is (= {::payment/description   description
              ::payment/resource      "payment"
              ::payment/profile-id    utils/profile-id
              ::link/documentation
              {::link/type "text/html",
               ::link/href "https://docs.mollie.com/reference/v2/payments-api/create-payment"}
              ::payment/mode          :test
              ::payment/method        :ideal
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/redirect-url  redirect-url
              ::payment/status        :open
              ::payment/sequence-type :oneoff}
             (dissoc payment
                     ::payment/id
                     ::payment/created-at
                     ::payment/expires-at
                     ::link/dashboard
                     ::link/self
                     ::link/checkout)))))

  (testing "Create a first recurring payment (mandate is not required)"
    (let [customer        (ensure-customer-without-mandate)
          amount-value    0.01M
          amount-currency "EUR"
          description     "Start new subscription test"
          sequence-type   :first
          redirect-url    "https://example.com"
          customer-id     (::customer/id customer)
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description   description
                                               :sequence-type sequence-type
                                               :redirect-url  redirect-url
                                               :customer-id   customer-id})]
      (is (= {::payment/description   description
              ::payment/resource      "payment"
              ::payment/profile-id    utils/profile-id
              ::link/documentation
              {::link/type "text/html",
               ::link/href "https://docs.mollie.com/reference/v2/payments-api/create-payment"}
              ::link/customer         (::link/self customer)
              ::payment/mode          :test
              ::payment/method        nil
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/redirect-url  redirect-url
              ::payment/status        :open
              ::payment/sequence-type :first
              ::payment/customer-id   customer-id}
             (dissoc payment
                     ::payment/id
                     ::payment/created-at
                     ::payment/expires-at
                     ::link/dashboard
                     ::link/self
                     ::link/checkout)))))

  (testing "Create a one-off payment for a customer"
    (let [customer        (ensure-customer-without-mandate)
          amount-value    100.0M
          amount-currency "EUR"
          customer-id     (::customer/id customer)
          description     "One-off payment for a customer"
          redirect-url    "https://example.com"
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description  description
                                               :redirect-url redirect-url}
                                              customer-id)]
      (is (= {::payment/description   description
              ::payment/resource      "payment"
              ::payment/profile-id    utils/profile-id
              ::link/documentation
              {::link/type "text/html",
               ::link/href "https://docs.mollie.com/reference/v2/customers-api/create-customer-payment"}
              ::link/customer         (::link/self customer)
              ::payment/mode          :test
              ::payment/method        nil
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/redirect-url  redirect-url
              ::payment/status        :open
              ::payment/sequence-type :oneoff
              ::payment/customer-id   customer-id}
             (dissoc payment
                     ::payment/id
                     ::payment/created-at
                     ::payment/expires-at
                     ::link/dashboard
                     ::link/self
                     ::link/checkout)))))

  (testing "Create a first recurring payment for a customer (arity one)"
    (let [customer        (ensure-customer-without-mandate)
          amount-value    0.01M
          amount-currency "EUR"
          description     "Start new subscription test"
          sequence-type   :first
          redirect-url    "https://example.com"
          customer-id     (::customer/id customer)
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description   description
                                               :sequence-type sequence-type
                                               :redirect-url  redirect-url
                                               :customer-id   customer-id})]
      (is (= {::payment/description   description
              ::payment/resource      "payment"
              ::payment/profile-id    utils/profile-id
              ::link/documentation
              {::link/type "text/html"
               ::link/href "https://docs.mollie.com/reference/v2/payments-api/create-payment"}
              ::link/customer         (::link/self customer)
              ::payment/mode          :test
              ::payment/method        nil
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/redirect-url  redirect-url
              ::payment/status        :open
              ::payment/sequence-type :first
              ::payment/customer-id   customer-id}
             (dissoc payment
                     ::payment/id
                     ::payment/created-at
                     ::payment/expires-at
                     ::link/dashboard
                     ::link/self
                     ::link/checkout)))))

  (testing "Create a first recurring payment for a customer"
    (let [customer        (ensure-customer-without-mandate)
          amount-value    0.01M
          amount-currency "EUR"
          description     "Start new subscription test"
          sequence-type   :first
          redirect-url    "https://example.com"
          customer-id     (::customer/id customer)
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description   description
                                               :sequence-type sequence-type
                                               :redirect-url  redirect-url}
                                              customer-id)]
      (is (= {::payment/description   description
              ::payment/resource      "payment"
              ::payment/profile-id    utils/profile-id
              ::link/documentation
              {::link/type "text/html"
               ::link/href "https://docs.mollie.com/reference/v2/customers-api/create-customer-payment"}
              ::link/customer         (::link/self customer)
              ::payment/mode          :test
              ::payment/method        nil
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/redirect-url  redirect-url
              ::payment/status        :open
              ::payment/sequence-type :first
              ::payment/customer-id   customer-id}
             (dissoc payment
                     ::payment/id
                     ::payment/created-at
                     ::payment/expires-at
                     ::link/dashboard
                     ::link/self
                     ::link/checkout)))))

  (testing "Create a recurring payment for a customer"
    (let [customer        (ensure-customer)
          customer-id     (::customer/id customer)
          mandate         (ensure-mandate customer-id)
          amount-value    100.00M
          amount-currency "EUR"
          description     "Charge directly"
          sequence-type   :recurring
          payment         (sut/create-payment utils/*mollie-client*
                                              {:amount
                                               {:value    amount-value
                                                :currency amount-currency}
                                               :description   description
                                               :sequence-type sequence-type}
                                              customer-id)]
      (is (= {::payment/settlement-amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::payment/sequence-type        sequence-type
              ::directdebit/consumer-name    (::directdebit/consumer-name mandate)
              ::directdebit/consumer-account (::directdebit/consumer-account mandate)
              ::directdebit/consumer-bic     (::directdebit/consumer-bic mandate)
              ::link/customer                (::link/self customer)
              ::payment/description          "Charge directly"
              ::payment/customer-id          customer-id
              ::payment/resource             "payment"
              ::payment/profile-id           utils/profile-id
              ::payment/is-cancelable        true
              ::link/documentation
              {::link/type "text/html"
               ::link/href "https://docs.mollie.com/reference/v2/customers-api/create-customer-payment"}
              ::payment/mode                 :test
              ::payment/method               :directdebit
              ::payment/amount
              {::amount/value    amount-value
               ::amount/currency amount-currency}
              ::link/mandate                 nil
              ::payment/redirect-url         nil
              ::payment/status               :pending
              ::payment/mandate-id           (::mandate/id mandate)}
             (-> payment
                 (dissoc ::payment/id
                         ::payment/created-at
                         ::link/self
                         ::link/dashboard
                         ::link/change-payment-state
                         ::directdebit/transfer-reference
                         ::directdebit/creditor-identifier
                         ::directdebit/due-date
                         ::directdebit/signature-date)))))))

(deftest get-payment-by-id-test
  (let [payment (ensure-payment)]
    (is (= (assoc payment
                  ::link/documentation {::link/type "text/html"
                                        ::link/href "https://docs.mollie.com/reference/v2/payments-api/get-payment"})
           (sut/get-payment-by-id utils/*mollie-client* (::payment/id payment))))))

(deftest update-payment-by-id-test
  (let [payment         (ensure-payment)
        new-description "Updated description for payment"]
    (is (= (assoc payment
                  ::link/documentation {::link/type "text/html"
                                        ::link/href "https://docs.mollie.com/reference/v2/payments-api/update-payment"}
                  ::payment/description new-description)
           (sut/update-payment-by-id utils/*mollie-client*
                                     (::payment/id payment)
                                     {:description new-description})))))

(deftest cancel-payment-by-id-test
  (let [payment  (ensure-cancelable-payment)
        canceled (sut/cancel-payment-by-id utils/*mollie-client* (::payment/id payment))]
    (is (= (-> payment
               (assoc ::payment/status :canceled
                      ::link/documentation {::link/type "text/html"
                                            ::link/href "https://docs.mollie.com/reference/v2/payments-api/cancel-payment"})
               (dissoc ::payment/settlement-amount
                       ::payment/is-cancelable
                       ::payment/canceled-at
                       ::payment/expires-at
                       ::link/change-payment-state
                       ::link/checkout))
           (dissoc canceled ::payment/canceled-at)))))

(deftest get-payments-list-test
  ;; NOTE: This is an extremely slow test due to the huge amount of
  ;; test payments. It's better to disable it.
  #_(testing "Fetch full list of payments"
      (ensure-payment)
      (let [response (sut/get-payments-list utils/*mollie-client* {})]
        (is (pos-int? (::pagination/count response)))
        (is (nil? (::pagination/next response)))
        (is (nil? (::pagination/previous response)))
        (is (nil? (::pagination/self response)))
        (is (vector? (::mollie/payments response)))
        (is (= (count (::mollie/payments response))
               (::pagination/count response)))))

  (testing "Fetch with `limit` parameter"
    (ensure-payment)
    (let [response (sut/get-payments-list utils/*mollie-client* {:limit 1})]
      (is (= 1 (::pagination/count response) (count (::mollie/payments response))))))

  ;; NOTE: This is an extremely slow test due to the huge amount of
  ;; test payments. It's better to disable it.
  #_(testing "Fetch with `from` parameter"
      (let [payment  (ensure-payment)
            response (sut/get-payments-list
                      utils/*mollie-client*
                      {:from (::payment/id payment)})]
        (is (= payment (first (::mollie/payments response))))))

  (testing "Fetch with both `from` and `limit` parameters"
    (let [payment  (ensure-payment)
          response (sut/get-payments-list
                    utils/*mollie-client*
                    {:from  (::payment/id payment)
                     :limit 1})]
      (is (= [payment] (::mollie/payments response)))))

  (testing "Fetch all payments for a particular customer"
    (let [customer    (ensure-customer)
          customer-id (::customer/id customer)
          payment     (ensure-payment customer-id)
          response    (sut/get-payments-list utils/*mollie-client* customer-id {})]
      (is (pos-int? (count (::mollie/payments response))))
      (is (every? #{customer-id} (map ::payment/customer-id (::mollie/payments response))))
      (is (some #{(assoc payment ::link/documentation nil)}
                (::mollie/payments response)))))

  (testing "Fetch all payment for a particular subscription"
    (let [{:keys [subscription
                  customer]} (ensure-subscription)
          subscription-id    (::subscription/id subscription)
          customer-id        (::customer/id customer)
          response           (sut/get-payments-list utils/*mollie-client*
                                                    customer-id
                                                    subscription-id
                                                    {})]
      (is (every? #{customer-id} (map ::payment/customer-id (::mollie/payments response))))
      (is (every? #{subscription-id} (map ::payment/subscription-id (::mollie/payments response)))))))

(deftest create-mandate-test
  (testing "Create direct debit mandate"
    (let [customer    (ensure-customer)
          customer-id (::customer/id customer)]
      (is (= {::mandate/mandate-reference nil,
              ::link/customer             (::link/self customer)
              ::mandate/signature-date    (LocalDate/now)
              ::directdebit/consumer-account "NL55INGB0000000000"
              ::directdebit/consumer-name    "Test Consumer"
              ::directdebit/consumer-bic     "INGBNL2A"
              ::mandate/resource          "mandate"
              ::mandate/method            :directdebit
              ::link/documentation
              {::link/type "text/html"
               ::link/href "https://docs.mollie.com/reference/v2/mandates-api/create-mandate"}
              ::mandate/mode              :test
              ::mandate/status            :valid}
             (-> (sut/create-mandate utils/*mollie-client*
                                     customer-id
                                     {:method           :directdebit
                                      :consumer-name    "Test Consumer"
                                      :consumer-account "NL55INGB0000000000"})
                 (dissoc ::mandate/id
                         ::mandate/created-at
                         ::link/self)))))))

(deftest get-mandate-by-id-test
  (let [customer    (ensure-customer)
        customer-id (::customer/id customer)
        mandate     (ensure-mandate customer-id)]
    (is (= (assoc mandate
                  ::link/documentation
                  {::link/type "text/html"
                   ::link/href "https://docs.mollie.com/reference/v2/mandates-api/get-mandate"})
           (sut/get-mandate-by-id utils/*mollie-client* customer-id (::mandate/id mandate))))))

(deftest revoke-mandate-by-id-test
  (let [customer    (ensure-customer)
        customer-id (::customer/id customer)
        mandate     (ensure-mandate customer-id)]
    (is (nil? (sut/revoke-mandate-by-id utils/*mollie-client*
                                        customer-id
                                        (::mandate/id mandate))))))

(deftest get-mandates-list-test
  (testing "Fetch all mandates for a particular customer"
    (let [customer    (ensure-customer)
          customer-id (::customer/id customer)
          mandate     (ensure-mandate customer-id)
          response    (sut/get-mandates-list utils/*mollie-client* customer-id {})]
      (is (pos-int? (::pagination/count response)))
      (is (nil? (::pagination/next response)))
      (is (nil? (::pagination/previous response)))
      (is (nil? (::pagination/self response)))
      (is (vector? (::mollie/mandates response)))
      (is (= (count (::mollie/mandates response))
             (::pagination/count response)))
      ;; Value `card-fingerprint` is always different in the test mode.
      (is (some #{(-> mandate
                      (assoc ::link/documentation nil)
                      (dissoc ::creditcard/card-fingerprint))}
                (->> response
                     ::mollie/mandates
                     (sequence (map #(dissoc % ::creditcard/card-fingerprint))))))))

  (testing "Fetch with `limit` parameter"
    (let [customer    (ensure-customer)
          customer-id (::customer/id customer)
          _           (ensure-mandate customer-id)
          response    (sut/get-mandates-list utils/*mollie-client* customer-id {:limit 1})]
      (is (= 1 (::pagination/count response) (count (::mollie/mandates response))))))

  (testing "Fetch with `from` parameter"
    (let [customer    (ensure-customer)
          customer-id (::customer/id customer)
          mandate     (ensure-mandate customer-id)
          response    (sut/get-mandates-list
                       utils/*mollie-client*
                       customer-id
                       {:from (::mandate/id mandate)})]
      (is (= (dissoc mandate ::link/documentation ::creditcard/card-fingerprint)
             (-> response
                 ::mollie/mandates
                 first
                 (dissoc ::link/documentation ::creditcard/card-fingerprint))))))

  (testing "Fetch with `from` and `limit` parameters"
    (let [customer    (ensure-customer)
          customer-id (::customer/id customer)
          mandate     (ensure-mandate customer-id)
          response    (sut/get-mandates-list
                       utils/*mollie-client*
                       customer-id
                       {:from  (::mandate/id mandate)
                        :limit 1})]
      (is (= [(dissoc mandate ::link/documentation ::creditcard/card-fingerprint)]
             (->> response
                  ::mollie/mandates
                  (into [] (map #(dissoc %
                                         ::link/documentation
                                         ::creditcard/card-fingerprint)))))))))

(deftest create-subscription-test
  (let [customer    (ensure-customer)
        customer-id (::customer/id customer)
        _           (ensure-mandate customer-id)
        description (str "Test subscription" (random-uuid))
        start-date  (.plusMonths (LocalDate/now) 1)
        response    (sut/create-subscription utils/*mollie-client*
                                             customer-id
                                             {:amount
                                              {:value    10.00M
                                               :currency "EUR"}
                                              :interval    "1 months"
                                              :description description
                                              :start-date  start-date})]
    (is (= {::subscription/status            :active
            ::link/customer                  (::link/self customer)
            ::subscription/start-date        start-date
            ::subscription/resource          "subscription"
            ::subscription/amount            {::amount/value 10.00M ::amount/currency "EUR"}
            ::subscription/description       description
            ::subscription/interval          "1 month"
            ::link/documentation
            {::link/type "text/html"
             ::link/href "https://docs.mollie.com/reference/v2/subscriptions-api/create-subscription"}
            ::subscription/mode              :test
            ::subscription/method            nil
            ::subscription/next-payment-date (. (LocalDate/now) plusMonths 1)}
           (dissoc response
                   ::subscription/id
                   ::subscription/created-at
                   ::link/profile
                   ::link/self)))))

(deftest get-subscription-by-id-test
  (let [{:keys [customer subscription]} (ensure-subscription)]
    (is (= (assoc subscription
                  ::link/documentation {::link/type "text/html"
                                        ::link/href "https://docs.mollie.com/reference/v2/subscriptions-api/get-subscription"})
           (sut/get-subscription-by-id utils/*mollie-client*
                                       (::customer/id customer)
                                       (::subscription/id subscription))))))

(deftest update-subscription-by-id-test
  (let [{:keys [customer subscription]} (ensure-subscription)
        updated-description             (str "Updated subscription " (random-uuid))
        updated-amount                  20.00M
        updated-interval                "10 days"
        updated-next-payment-date       (. (LocalDate/now) plusDays 10)]
    (is (= (-> subscription
               (assoc ::subscription/description updated-description
                      ::subscription/interval updated-interval
                      ::subscription/amount {::amount/value    updated-amount
                                             ::amount/currency "EUR"}
                      ::subscription/next-payment-date updated-next-payment-date
                      ::link/documentation {::link/type "text/html"
                                            ::link/href "https://docs.mollie.com/reference/v2/subscriptions-api/update-subscription"}))
           (sut/update-subscription-by-id utils/*mollie-client*
                                          (::customer/id customer)
                                          (::subscription/id subscription)
                                          {:description updated-description
                                           :amount      {:currency "EUR"
                                                         :value    updated-amount}
                                           :interval    updated-interval})))))

(deftest cancel-subscription-by-id
  (let [{:keys [customer subscription]} (ensure-subscription)]
    (is (= (-> subscription
               (assoc ::subscription/status :canceled
                      ::link/documentation {::link/type "text/html"
                                            ::link/href "https://docs.mollie.com/reference/v2/subscriptions-api/cancel-subscription"})
               (dissoc ::subscription/next-payment-date
                       ::subscription/canceled-at))
           (-> (sut/cancel-subscription-by-id utils/*mollie-client*
                                              (::customer/id customer)
                                              (::subscription/id subscription))
               (dissoc ::subscription/canceled-at))))))

(deftest get-subscriptions-list-test
  (testing "Fetch all subscriptions for all customers"
    (ensure-subscription)
    (let [response (sut/get-subscriptions-list utils/*mollie-client* {})]
      (is (pos-int? (::pagination/count response)))
      (is (nil? (::pagination/next response)))
      (is (nil? (::pagination/previous response)))
      (is (nil? (::pagination/self response)))
      (is (= (::pagination/count response) (count (::mollie/subscriptions response))))))

  (testing "Fetch all subscriptions with `limit` parameter"
    (ensure-subscription)
    (let [response (sut/get-subscriptions-list utils/*mollie-client* {:limit 1})]
      (is (= 1 (::pagination/count response) (count (::mollie/subscriptions response))))))

  (testing "Fetch all subscriptions with `from` parameter"
    (let [{:keys [subscription]} (ensure-subscription)
          response               (sut/get-subscriptions-list
                                  utils/*mollie-client*
                                  {:from (::subscription/id subscription)})]
      (is (= (dissoc subscription ::link/documentation)
             (first (::mollie/subscriptions response))))))

  (testing "Fetch all subscriptions with both `from` and `limit` parameters"
    (let [{:keys [subscription]} (ensure-subscription)
          response               (sut/get-subscriptions-list
                                  utils/*mollie-client*
                                  {:from  (::subscription/id subscription)
                                   :limit 1})]
      (is (= [(dissoc subscription ::link/documentation)]
             (->> response
                  ::mollie/subscriptions
                  (into [] (map #(dissoc % ::link/documentation))))))))

  (testing "Fetch subscriptions for a particular customer"
    (let [{:keys [customer subscription]} (ensure-subscription)
          response                        (sut/get-subscriptions-list
                                           utils/*mollie-client*
                                           (::customer/id customer)
                                           {})]
      (is (some #{(dissoc subscription ::link/documentation)}
                (::mollie/subscriptions response))))))
