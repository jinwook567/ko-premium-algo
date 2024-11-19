(ns ko-premium-algo.job.candidates
  (:require [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.trade.market :refer [base-asset quote-asset]]
            [ko-premium-algo.trade.ticker :refer [market price]]
            ))

(def black-list ["TON"])

(defn filter-black-list [tickers]
  (filter #(and
            (not (some (fn [coin] (= (base-asset (market %)) coin)) black-list))
            (not (some (fn [coin] (= (quote-asset (market %)) coin)) black-list))) tickers))

(defn base-asset-tickers [type target-base-asset]
  (ticker type (filter #(= (base-asset %) target-base-asset) (markets type))))


(defn find-ticker [tickers target-base-asset target-quote-asset]
  (some #(when (and (= target-quote-asset (quote-asset (market %)))
                    (= target-base-asset (base-asset (market %)))) %) tickers))

(defn candidates [exchange1 exchange1-base-asset exchange2 exchange2-base-asset]
  (let [ex1-tickers (filter-black-list (base-asset-tickers exchange1 exchange1-base-asset))
        ex2-tickers (filter-black-list (base-asset-tickers exchange2 exchange2-base-asset))]
    (->> (map #(list % (find-ticker ex2-tickers exchange2-base-asset (quote-asset (market %)))) ex1-tickers)
         (filter #(some? (second %)))
         (map (fn [route]
                {:route route
                 :weight (* (price (first route)) (/ 1 (price (second route))))
                 :base-weight (or (when-let [ticker (find-ticker ex1-tickers exchange1-base-asset exchange2-base-asset)] (price ticker))
                                  (when-let [ticker (find-ticker ex2-tickers exchange2-base-asset exchange1-base-asset)] (/ 1 (price ticker))))})))))

(defn min-route [route]
  (reduce (fn [ret candidate]
            (if (> (:weight ret) (:weight candidate))
              candidate ret)) (map #(assoc % :diff  (/ (:weight %) (:base-weight %))) route)))


(min-route (candidates :upbit "KRW" :binance "USDT"))
(min-route (candidates :binance "USDT" :upbit "KRW"))