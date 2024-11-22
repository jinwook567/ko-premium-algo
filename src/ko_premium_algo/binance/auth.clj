(ns ko-premium-algo.binance.auth
  (:require [environ.core :refer [env]]
            [ko-premium-algo.lib.time :refer [now millis]]
            [buddy.core.mac :refer [hash]]
            [clj-http.client :as client]
            [buddy.core.codecs :as codecs]))

(def ^:private BINANCE-SECRET-KEY (env :binance-secret-key))
(def ^:private BINANCE-ACCESS-KEY (env :binance-access-key))

(defn- make-base-payload []
  {:timestamp (millis (now)) :recvWindow 3000})

(defn- make-signature [query]
  (codecs/bytes->hex (hash (client/generate-query-string query) {:alg :hmac+sha256 :key BINANCE-SECRET-KEY})))

(defn make-payload
  ([] (make-payload {}))
  ([query] (let [base-payload (make-base-payload)]
             (merge query {:signature (make-signature (merge query base-payload))} base-payload))))

(defn add-secret-key [header]
  (merge header {"X-MBX-APIKEY" BINANCE-ACCESS-KEY}))
