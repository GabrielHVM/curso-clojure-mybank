(ns mybank-web-api.controllers
  (:require [mybank-web-api.logic :as l.web-api]
            [com.stuartsierra.component :as component]))

(defn- update-account!
  [bank account-id value operation]
  (swap! bank (fn [accounts-map]
                (update accounts-map account-id #(operation value %)))))

(defn- ok-response
  "Given a body, return the map with 200/ok response"
  [body]
  {:status 200 :body body})

(defn- not-found
  "Given a body, return the map with 404/not found response"
  [body]
  {:status 404 :body body})

(defn- verify-if-account-exist
  [context]
  (let [database (-> context :database)
        account-id (-> context :request :path-params :id keyword)]
    (if-not (l.web-api/account-exist? account-id @database)
      (assoc context :response (not-found {:message "Account not found!\n"}))
      context)))

(defn make-withdraw
  [context]
  (let [request (-> context :request)
        database (-> context :database)
        account-id (-> request :path-params :id keyword)
        withdrawal-value (-> request :body slurp parse-double)]
    (update-account! database account-id withdrawal-value l.web-api/withdraw-off-account)
    (assoc context :response (ok-response {:id-conta       account-id
                                           :valor-do-saque withdrawal-value
                                           :novo-saldo     (account-id @database)}))))

(defn make-deposit
  [context]
  (let [request (-> context :request)
        database (-> context :database)
        account-id (-> request :path-params :id keyword)
        deposit-value (-> request :body slurp parse-double)]
    (update-account! database account-id deposit-value l.web-api/deposit-into-an-account)
    (assoc context :response
                   (ok-response {:id-conta          account-id
                                 :valor-do-deposito deposit-value
                                 :novo-saldo        (account-id @database)}))))

(defn get-account-balance
  [context]
  (let [request (-> context :request)
        database (-> context :database)
        account-id (-> request :path-params :id keyword)
        account-balance (account-id @database)]
    (assoc context :response (ok-response {:id-conta    account-id
                                           :saldo-conta account-balance}))))


(defrecord Controllers []
  component/Lifecycle

  (start [this]
    (assoc this :interceptors-fn {:get-balance        get-account-balance
                                  :withdraw           make-withdraw
                                  :deposit            make-deposit
                                  :account-id-checker verify-if-account-exist}))

  (stop [this]
    (dissoc this :interceptors-fn)))

(defn new-controllers
  []
  (->Controllers))