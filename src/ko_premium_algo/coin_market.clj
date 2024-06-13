(ns ko-premium-algo.coin-market
  (:require [ko-premium-algo.market-binance :as binance]
            [ko-premium-algo.market-upbit :as upbit]
            [ko-premium-algo.coin :as coin]
            [ko-premium-algo.market :as market]))

(def market-types
  (list :binance :upbit))

(defn market-current-exchange-rates [market-type]
  (cond
    (= market-type :binance) binance/current-exchange-rates
    (= market-type :upbit) upbit/current-exchange-rates))

(defn market-pairs [market-type]
  (cond
    (= market-type :binance) binance/pairs
    (= market-type :upbit) upbit/pairs))

(defn market-io-status [market-type]
  (cond
    (= market-type :binance) binance/io-status
    (= market-type :upbit) upbit/io-status))

(defn market-trade-available-coins [market-type]
  (cond
    (= market-type :binance) binance/trade-available-coins
    (= market-type :upbit) upbit/trade-available-coins))

(defn market-exchange-info [market-type]
  (cond
    (= market-type :binance) binance/exchange-info
    (= market-type :upbit) upbit/exchange-info))

(defn io-available-coins [io-status]
  (map :coin (filter coin/can-io? io-status)))

(defn coin-market-pairs [type]
  (let [pairs ((market-pairs type))
        exchange-rates ((market-current-exchange-rates type) pairs)]
    (map #(market/make-market-pair %1 %2) exchange-rates pairs)))

(defn market-fee [market-type]
  (cond
    (= market-type :binance) binance/fee
    (= market-type :upbit) upbit/fee))
