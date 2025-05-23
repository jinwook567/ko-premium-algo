(ns crypto.exchange.upbit.auth
  (:require [environ.core :refer [env]]
            [buddy.sign.jwt :as jwt]
            [buddy.core.hash :as hash]
            [clj-http.client :as client]
            [buddy.core.codecs :as codecs]))

(def ^:private UPBIT-SECRET-KEY (env :upbit-secret-key))
(def ^:private UPBIT-ACCESS-KEY (env :upbit-access-key))

(defn query-string [query]
  (client/generate-query-string-with-encoding query "UTF-8" :array))

(defn- make-base-payload []
  {:access_key UPBIT-ACCESS-KEY
   :nonce (java.util.UUID/randomUUID)})

(defn make-payload
  ([] (make-base-payload))
  ([query] (merge (make-base-payload)
                  {:query_hash (codecs/bytes->hex (hash/sha512 (query-string query)))
                   :query_hash_alg "SHA512"})))

(defn make-token [payload]
  (str "Bearer " (jwt/sign payload UPBIT-SECRET-KEY)))

(defn make-auth-header
  ([] (make-auth-header nil))
  ([query] {:Authorization (make-token (apply make-payload (remove empty? [query])))}))
