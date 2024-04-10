(ns ko-premium-algo.coin)

(defn make-coin [code]
  {:code code})

(defn code [coin] (:code coin))

(defn make-io-status-item [coin can-deposit can-withdraw net-type net-name busy]
  {:coin coin :can-deposit can-deposit :can-withdraw can-withdraw :net-type net-type :net-name net-name :busy busy})

(defn can-io? [io-status-item]
  (and (:can-deposit io-status-item) (:can-withdraw io-status-item) (not (:busy io-status-item))))