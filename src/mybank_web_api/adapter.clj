(ns mybank-web-api.adapter
  (:require [clojure.data.json :as json]
            [com.stuartsierra.component :as component]))

(defn- accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn- transform-content
  [body content-type]
  (case content-type
    "text/html" body
    "text/plain" body
    "application/edn" (pr-str body)
    "application/json" (json/write-str body)))

(defn- coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(defn- coerce-body
  [context]
  (if-let [_ (get-in context [:response :headers "Content-Type"])]
    context
    (update-in context [:response] coerce-to (accepted-type context))))

(defrecord Adapter []
  component/Lifecycle

  (start [this]
    (assoc this :coerce-body coerce-body))

  (stop [this]
    (dissoc this :coerce-body)))

(defn new-adapter []
  (->Adapter))