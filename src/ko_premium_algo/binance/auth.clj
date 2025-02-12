(ns ko-premium-algo.binance.auth
  (:require [environ.core :refer [env]]
            [ko-premium-algo.lib.time :refer [now time->millis]]
            [buddy.core.mac :refer [hash]]
            [clj-http.client :as client]
            [buddy.core.codecs :as codecs]))

(def ^:private BINANCE-SECRET-KEY (env :binance-secret-key))
(def ^:private BINANCE-ACCESS-KEY (env :binance-access-key))

(defn coerce-decimal-notation [query]
  (into {} (map #(vector %1 (if (number? %2) (.toPlainString (bigdec %2)) %2))
                (keys query)
                (vals query))))

(defn coerce-query [query]
  (into (sorted-map) (coerce-decimal-notation query)))

(coerce-query {:market {:base-asset "BTC", :quote-asset "CELR", :symbol "CELRBTC"}, :price 1.3E-7, :qty 833.0, :side :ask})

(defn- make-base-payload []
  {:timestamp (time->millis (now)) :recvWindow 3000})

(defn- make-signature [query]
  (codecs/bytes->hex (hash (client/generate-query-string (coerce-query query)) {:alg :hmac+sha256 :key BINANCE-SECRET-KEY})))

(defn make-payload
  ([] (make-payload {}))
  ([query] (let [base-payload (make-base-payload)]
             (coerce-query (merge query {:signature (make-signature (merge query base-payload))} base-payload)))))

(defn make-auth-header []
  {"X-MBX-APIKEY" BINANCE-ACCESS-KEY})
