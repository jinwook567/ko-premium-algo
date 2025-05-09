(ns crypto.model.wallet.address)

(defn make-address [primary-address secondary-address]
  {:primary-address primary-address :secondary-address secondary-address})

(defn primary-address [address]
  (:primary-address address))

(defn secondary-address [address]
  (:secondary-address address))
