(ns ko-premium-algo.trade.order)

(defn execute-order [order-f intent]
  (order-f intent))

(def state-candidates
  #{:open :done :cancelled})

(defn make-order [id intent executed-qty created-at state]
  (assert (state-candidates state) "Invalid state")
  {:id id :intent intent :executed-qty executed-qty :created-at created-at :state state})

(defn id [order]
  (:id order))

(defn intent [order]
  (:intent order))

(defn executed-qty [order]
  (:executed-qty order))

(defn created-at [order]
  (:created-at order))

(defn state [order]
  (:state order))