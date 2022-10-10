(ns mybank-web-api.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test-http]
            [io.pedestal.interceptor :as i]
            [db.core :as db]
            [logic.core :as api-logic])
  (:gen-class))

(defonce server (atom nil))

(defn add-contas-atom [context]
  (update context :request assoc :contas db/contas))

(def contas-interceptor
  {:name  :contas-interceptor
   :enter add-contas-atom})

(defn get-saldo [request]
  (let [id-conta (-> request :path-params :id keyword)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (id-conta @db/contas "conta inválida!")}))

(defn make-withdraw
  [request]
    (let [id-conta (-> request :path-params :id keyword)
          withdrawal-value (-> request :body slurp parse-double)]
      (if (api-logic/account-exists? id-conta @db/contas)
        (do
          (api-logic/update-account! db/contas id-conta withdrawal-value -)
          {:status  200
           :headers {"Content-Type" "text/plain"}
           :body    {:id-conta       id-conta
                     :valor-do-saque withdrawal-value
                     :novo-saldo     (id-conta @db/contas)}})
        {:status 404
         :headers {"Content-Type" "text/plain"}
         :body {:message "Account not founded"}})
      ))

(defn make-deposit [request]
  (let [id-conta (-> request :path-params :id keyword)
        valor-deposito (-> request :body slurp parse-double)]

    (if (api-logic/account-exists? id-conta @db/contas)
      (do
        (api-logic/update-account! db/contas id-conta valor-deposito +)
          {:status 200
           :headers {"Content-Type" "text/plain"}
           :body {:id-conta   id-conta
                  :novo-saldo (id-conta @db/contas)}})
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body {:message "Account not founded"}})))

(def routes
  (route/expand-routes
    #{["/saldo/:id" :get get-saldo :route-name :saldo]
      ["/deposito/:id" :post make-deposit :route-name :deposito]
      ["/saque/:id" :post make-withdraw :route-name :saque]}))


(def service-map-simple {::http/routes routes
                         ::http/port   9999
                         ::http/type   :jetty
                         ::http/join?  false})

(def service-map (-> service-map-simple
                     (http/default-interceptors)
                     (update ::http/interceptors conj (i/interceptor contas-interceptor))))

(defn create-server []
  (http/create-server
    service-map))

(defn start []
  (reset! server (http/start (create-server))))

(defn reset-server
  []
  (http/stop @server)
  (reset! server nil)
  (reset! server (http/start (create-server))))

(defn test-request [server verb url]
  (test-http/response-for (::http/service-fn @server) verb url))
(defn test-post [server verb url body]
  (test-http/response-for (::http/service-fn @server) verb url :body body))

(comment
  (start)
  (http/stop @server)
  (reset-server)


  (test-request server :get "/saldo/1")
  (test-request server :get "/saldo/2")
  (test-request server :get "/saldo/3")
  (test-request server :get "/saldo/4")
  (test-post server :post "/deposito/1" "199.93")
  (test-post server :post "/deposito/4" "325.99")
  (test-post server :post "/saque/1" "199.93")
  (test-post server :post "/saque/5" "199.93")
  (test-post server :post "/deposito/5" "325.99")

  ;curl http://localhost:9999/saldo/1
  ;curl -d "199.99" -X POST http://localhost:9999/deposito/1
  )
