(ns ko-premium-algo.transfer.history)

(defn make-history [id intent created-at state]
  {:id id :intent intent :created-at created-at :state state})

(defn id [history]
  (:id history))

(defn intent [history]
  (:intent history))

(defn created-at [history]
  (:created-at history))

(defn state [history]
  (:state history))
