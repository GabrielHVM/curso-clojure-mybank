(ns diplomat.http-server-test
  (:require [clojure.test :refer :all]))

(defn test-request [server verb url]
  (test-http/response-for (::http/service-fn @server) verb url))
(defn test-post [server verb url body]
  (test-http/response-for (::http/service-fn @server) verb url :body body))


(test-request server :get "/saldo/1")
(test-request server :get "/saldo/2")
(test-request server :get "/saldo/3")
(test-request server :get "/saldo/4")
(test-post server :post "/deposito/1" "199.93")
(test-post server :post "/deposito/4" "325.99")
(test-post server :post "/saque/1" "199.93")
(test-post server :post "/saque/5" "199.93")
(test-post server :post "/deposito/5" "325.99")