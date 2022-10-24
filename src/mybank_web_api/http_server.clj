(ns mybank-web-api.http-server
  (:require [io.pedestal.http :as http]

            [io.pedestal.interceptor :as i]
            [com.stuartsierra.component :as component])
  (:gen-class))

(defonce server (atom nil))

(defn start-server!
  [service-map]
  (->> service-map
       http/create-server
       http/start
       (reset! server)))

(defn stop-server! []
  (http/stop @server))

(defn restart-server!
  [service-map]
  (stop-server!)
  (start-server! service-map))

(defn- insert-database-in-context
  [database context]
  (assoc context :database database))

(defrecord BankServer [database adapter routes]
  component/Lifecycle

  (start [this]
    (let [bank-database (:data database)

          database-interceptor-fn (partial insert-database-in-context bank-database)

          db-interceptor {:name :db-interceptor
                          :enter database-interceptor-fn}
          coerce-body-interceptor {:name  ::coerce-body-interceptor
                                   :leave (:coerce-body adapter)}
          service-map-base {::http/routes (:routes-expanded routes)
                            ::http/port 9999
                            ::http/type :jetty
                            ::http/join? false}
          service-map (-> service-map-base
                          (http/default-interceptors)
                          (update ::http/interceptors conj (i/interceptor db-interceptor))
                          (update ::http/interceptors conj (i/interceptor coerce-body-interceptor)))]
      (try
        (start-server! service-map)
        (println "Server Started successfully")
        (catch Exception e
          (println "Error executing server start: " (.getMessage e))
          (println "Trying server restart..." (.getMessage e))
          (try
            (restart-server! service-map)
            (println "Server Restarted successfully!")
            (catch Exception e (println "Error executing server restart: " (.getMessage e))))))
      (assoc this :http-server server)))
  (stop [this]
    (stop-server!)))

(defn new-server []
  (->BankServer {} {} {}))