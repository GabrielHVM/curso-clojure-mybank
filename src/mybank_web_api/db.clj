(ns mybank-web-api.db
  (:require [com.stuartsierra.component :as component]))

(defrecord Database []
  component/Lifecycle

  (start [this]
    (let [database-file (-> "resources/contas.edn"
                            slurp
                            read-string)]
      (assoc this :data (atom database-file))))

  (stop [this]
    (assoc this :store nil)))

(defn new-database []
  (->Database))