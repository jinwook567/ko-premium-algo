(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [assets base-asset]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge]]
            [ko-premium-algo.job.weight :refer [route-weight edge-weight]]
            [ko-premium-algo.route.search :refer [make-route-finder higher-choice]]
            [ko-premium-algo.route.graph :refer [edges->graph]]
            [ko-premium-algo.wallet.terms :refer [fee limits]]
            [ko-premium-algo.wallet.unit :as unit]
            [ko-premium-algo.wallet.limits :refer [actions can-transfer?]]
            [ko-premium-algo.trade.fee :refer [value]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.gateway.terms :refer [terms]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [ko-premium-algo.trade.ticker :refer [market]]
            [clojure.math.combinatorics :as combo]))

(def fake-terms {:ask-terms
                 {:fee {:type :rate, :value 5.0E-4},
                  :limits
                  {:qty-range {:min 0, :max ##Inf, :step 1.0E-8},
                   :price-range {:min 0, :max ##Inf, :step nil},
                   :amount-range {:min 5000.0, :max 1.0E9, :step 0.01}}},
                 :bid-terms
                 {:fee {:type :rate, :value 5.0E-4},
                  :limits
                  {:qty-range {:min 0, :max ##Inf, :step 1.0E-8},
                   :price-range {:min 0, :max ##Inf, :step nil},
                   :amount-range {:min 5000.0, :max 1.0E9, :step 0.01}}}})

;; terms api 변경 필요
(defn make-exchange-edges [exchange markets]
  (let [tickers (ticker exchange markets)
        ;; terms-list (terms exchange markets)
        terms-list (map (fn [_] fake-terms) tickers)]
    (concat (map #(make-ask-edge exchange %1 %2) tickers terms-list)
            (map #(make-bid-edge exchange %1 %2) tickers terms-list))))

(defn make-bridge-edges [base-exchange quote-exchange units]
  (map #(make-withdraw-edge base-exchange quote-exchange %1 %2 %3)
       (transfer/terms base-exchange units)
       (transfer/terms quote-exchange units)
       units))



(defn all-bridge-edges [& exchange-list]
  (->> (combo/permuted-combinations exchange-list 2)
       (mapcat #(make-bridge-edges (first %) (second %) (transfer/units (first %))))))

(defn base-asset-exchange-edges [node]
  (->> (markets (:exchange node))
       (filter #(= (base-asset %) (:asset node)))
       (make-exchange-edges (:exchange node))))

(defn base-asset-edges [& nodes]
  (concat (mapcat base-asset-exchange-edges nodes)
          (apply all-bridge-edges (set (map :exchange nodes)))))

(defn make-finder [graph]
  (fn [base-node quote-node base-node-qty]
    (let [finder (make-route-finder graph (higher-choice (partial route-weight base-node-qty)))]
      (finder base-node quote-node))))

;; (def brd-edges (make-bridge-edges :upbit :binance (transfer/units :upbit)))
;; (def perfect-edges (base-asset-edges {:exchange :upbit :asset "KRW"} {:exchange :binance :asset "USDT"}))

;; (filter #(and (= :withdraw (->> % :meta :type))
;;               (= (->> % :start :asset) "USDT")) perfect-edges)

;; (filter #(and (= (->> % :meta :type) :withdraw)
;;               (nil? (->> % :meta :base-terms :fee))) brd-edges)

(def finder (make-finder (edges->graph (base-asset-edges {:exchange :upbit :asset "KRW"} {:exchange :binance :asset "USDT"}))))
(route-weight 5000 (finder {:exchange :upbit :asset "KRW"} {:exchange :binance :asset "USDT"}  5000))

;; 그래프 알고리즘에는 이제 이상 전혀 없고, edge-weight가 문제인듯.. 지금 이상한게 아래 간선에 대해서 결과값이 0이 나옴
((edge-weight {:meta
               {:type :bid,
                :symbol "KRW-BERA",
                :price 12030.0,
                :terms
                {:fee {:type :rate, :value 5.0E-4},
                 :limits
                 {:qty-range {:min 0, :max ##Inf, :step 1.0E-8},
                  :price-range {:min 0, :max ##Inf, :step nil},
                  :amount-range {:min 5000.0, :max 1.0E9, :step 0.01}}}},
               :start {:exchange :upbit, :asset "KRW"},
               :end {:exchange :upbit, :asset "BERA"}}) 5000)
;; (route-weight 5000 )

;; 여기서 0이 나와버리는데, withdraw edges가 많은 것에 대해서 처리를 못해주는지.. 뭔지 모르곘다
;; (finder {:exchange :upbit :asset "KRW"} {:exchange :binance :asset "USDT"} 3000)
;; (route-weight 300 )

;; qty를 계산해서 finder를 호출해주는게 꽤 일인데 경유지 알고리즘이 있으면 좋을듯 싶다.
