(ns crypto.exchange.upbit.order
  (:require [model.intent :refer [market qty price side make-intent]]
            [model.order :refer [make-order]]
            [mode.time :refer [iso8601->time]]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [model.market :as m]
            [crypto.exchange.upbit.auth :as auth]))

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
                           (get response "volume")
                           (get response "price"))
              (get response "executed_volume")
              (iso8601->time (get response "created_at"))
              (order-state (get response "state"))))

(defn execute-order [intent]
  (let [request {:market (m/symbol (market intent))
                 :side (side intent)
                 :volume (str (qty intent))
                 :price (str (price intent))
                 :ord_type "limit"}]
    (->> (client/post "https://api.upbit.com/v1/orders"
                      {:body (json/encode request)
                       :headers (auth/make-auth-header (json/decode (json/encode request)))
                       :content-type :json})
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
  (concat (base-open-orders market 1)
          (->> (iterate inc 2)
               (map #(base-open-orders market %))
               (take-while #(= (count %) 100))
               (apply concat))))

(defn order [market id]
  (let [request {:market (m/symbol market)
                 :uuids [id]}]
    (->> (client/get "https://api.upbit.com/v1/orders/uuids"
                     {:query-params request
                      :headers (auth/make-auth-header request)
                      :multi-param-style :array})
         (#(first (json/parse-string (:body %))))
         (#(response->order market %)))))
