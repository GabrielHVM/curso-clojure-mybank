(ns mybank-web-api.logic)

(defn account-exist?
  [account-id accounts]
  (account-id accounts))

(defn- account-operation
  [account operation value]
  (let [function (case operation
                   :withdraw -
                   :deposit +)]
    (update account :saldo function value)))

(defn withdraw-off-account
  [value account]
  (account-operation account :withdraw value))

(defn deposit-into-an-account
  [value account]
  (account-operation account :deposit value))