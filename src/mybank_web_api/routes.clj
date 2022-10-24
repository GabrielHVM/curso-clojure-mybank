(ns mybank-web-api.routes
  (:require [io.pedestal.http.route :as route]
            [com.stuartsierra.component :as component]))

(defrecord Routes [controllers]
  component/Lifecycle

  (start [this]
    (let [interceptors (:interceptors-fn controllers)

          verify-if-account-exist-interceptor {:name  ::account-checker
                                               :enter (:account-id-checker interceptors)}

          withdraw-interceptor {:name  ::withdraw
                                :enter (:withdraw interceptors)}

          deposit-interceptor {:name  ::deposit
                               :enter (:deposit interceptors)}

          account-balance-interceptor {:name  ::account-balance
                                       :enter (:get-balance interceptors)}]
      (assoc this :routes-expanded (route/expand-routes
                                     #{["/saldo/:id"
                                        :get [verify-if-account-exist-interceptor account-balance-interceptor] :route-name :saldo]
                                       ["/deposito/:id"
                                        :post [verify-if-account-exist-interceptor deposit-interceptor] :route-name :deposito]
                                       ["/saque/:id"
                                        :post [verify-if-account-exist-interceptor withdraw-interceptor] :route-name :saque]}))))
  (stop [this]
    (dissoc this :routes-expanded)))

(defn new-routes []
  (->Routes {}))