(ns ko-premium-algo.upbit.order
  (:require [ko-premium-algo.trade.intent :refer [market qty price side make-intent]]
            [ko-premium-algo.trade.order :refer [make-order]]
            [ko-premium-algo.lib.time :refer [iso8601->time]]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.upbit.auth :as auth]))

(defn- order-state [upbit-state]
  (cond
    (= upbit-state "wait") :open
    (= upbit-state "done") :done
    (= upbit-state "cancel") :cancelled
    :else (throw (ex-info "invalid state"
                          {:upbit-state upbit-state}))))

(defn- response->order [market response]
  (make-order (get response "uuid")
              (make-intent market
                           (keyword (get response "side"))
                           (Float/parseFloat (get response "volumn"))
                           (Float/parseFloat (get response "price")))
              (Float/parseFloat (get response "executed_volume"))
              (iso8601->time (get response "created_at"))
              (order-state (get response "state"))))

(defn execute-order [intent]
  (let [request {:market (m/symbol (market intent))
                 :side (side intent)
                 :volumn (str (qty intent))
                 :price (str (price intent))
                 :ord_type "limit"}]
    (->> (client/post "https://api.upbit.com/v1/orders"
                      {:body request :headers (auth/make-auth-header request)})
         (#(json/parse-string (:body %)))
         (#(response->order (market intent) %)))))

(defn- base-open-orders [market page]
  (let [request {:market (m/symbol market)
                 :page page}]
    (->> (client/get "https://api.upbit.com/v1/orders/open"
                     {:query-params request
                      :headers (auth/make-auth-header request)})
         (#(json/parse-string (:body %)))
         (map #(response->order market %)))))

(defn open-orders [market]
  (->> (iterate inc 1)
       (map #(base-open-orders market %))
       (take-while #(= (count %) 100))
       (apply concat)))

(defn order [market id]
  (let [request {:market (m/symbol market)
                 :uuids (auth/query-string [id])}]
    (->> (client/get "https://api.upbit.com/v1/orders/uuids"
                     {:query-params request}
                     {:headers (auth/make-auth-header request)})
         (#(json/parse-string (:body (first %))))
         (#(response->order market %)))))
