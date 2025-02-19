(ns ko-premium-algo.binance.order
  (:require [ko-premium-algo.trade.order :refer [make-order]]
            [ko-premium-algo.trade.intent :refer [make-intent market side qty price]]
            [ko-premium-algo.lib.time :refer [millis->time]]
            [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.binance.auth :as auth]
            [cheshire.core :as json]))

(defn- order-side [binance-side]
  (if (= binance-side "BUY") :bid :ask))

(defn- binance-side [intent-side]
  (if (= intent-side :bid) "BUY" "SELL"))

(defn- order-state [binance-state]
  (cond
    (= binance-state "NEW") :open
    (= binance-state "PARTIALLY_FILLED") :open
    (= binance-state "FILLED") :done
    (or (= binance-state "CANCELED")
        (= binance-state "REJECTED")
        (= binance-state "EXPIRED")
        (= binance-state "EXPIRED_IN_MATCH")) :cancelled
    :else (throw (ex-info "invalid state" {:state binance-state}))))

(defn- response->order [market response]
  (make-order (get response "orderId")
              (make-intent market
                           (order-side (get response "side"))
                           (get response "origQty")
                           (get response "price"))
              (get response "executedQty")
              (millis->time (or (get response "transactTime") (get response "time")))
              (order-state (get response "status"))))

(defn execute-order [intent]
  (let [request  {:symbol (m/symbol (market intent))
                  :side (binance-side (side intent))
                  :type "LIMIT"
                  :timeInForce "GTC"
                  :quantity (qty intent)
                  :price  (price intent)}]
    (->> (client/post "https://api.binance.com/api/v3/order"
                      {:query-params (auth/make-payload request)
                       :headers (auth/make-auth-header)})
         (#(json/parse-string (:body %)))
         (#(response->order (market intent) %)))))

(defn open-orders [market]
  (->> (client/get "https://api.binance.com/api/v3/openOrders"
                   {:query-params (auth/make-payload {:symbol (m/symbol market)})
                    :headers (auth/make-auth-header)})
       (#(json/parse-string (:body %)))
       (map #(response->order market %))))

(defn order [market id]
  (->> (client/get "https://api.binance.com/api/v3/order"
                   {:query-params (auth/make-payload {:symbol (m/symbol market) :orderId id})
                    :headers (auth/make-auth-header)})
       (#(json/parse-string (:body %)))
       (#(response->order market %))))
