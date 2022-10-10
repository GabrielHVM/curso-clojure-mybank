(ns logic.core)

(defn update-account!
  [accounts account-id value f]
  (swap! accounts (fn [accounts-map]
                    (update-in accounts-map [account-id :saldo] #(f % value)))))

(defn account-exists?
  [account-id accounts]
  (account-id accounts))