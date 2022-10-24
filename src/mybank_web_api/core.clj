(ns mybank-web-api.core
  (:require [com.stuartsierra.component :as component]
            [mybank-web-api.db :as db]
            [mybank-web-api.controllers :as controllers]
            [mybank-web-api.routes :as routes]
            [mybank-web-api.adapter :as adapter]
            [mybank-web-api.http-server :as server])
  (:gen-class))

(def new-sys
  (component/system-map
    :database (db/new-database)
    :controllers (controllers/new-controllers)
    :adapter (adapter/new-adapter)
    :routes (component/using
              (routes/new-routes)
              [:controllers])
    :server (component/using
              (server/new-server)
              [:database :adapter :routes])))

(def sys (atom nil))

(defn main- []
  (reset! sys (component/start new-sys)))

(comment
  (require '[clj-http.client :as client])
  (client/post "http://localhost:9999/deposito/1" {:body "199.93"})
  (client/post "http://localhost:9999/saque/1" {:body "199.93"})
  (client/get "http://localhost:9999/saldo/1")
  (main-)
  (main)
  (:web-server @sys)
  (start)
  (http/stop @server)
  (component/stop new-sys)
  )