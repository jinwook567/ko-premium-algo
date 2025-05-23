(ns lib.async
  (:require [clojure.core.async :as async :refer [go-loop <! >! chan go close! timeout]]
            [mode.time :refer [millis]]))

(defn- to-vec [chan]
  (async/reduce #(conj %1 %2) [] chan))

(defn- execute-fun [fun]
  (let [result (fun)
        ch (chan 1)]
    (go (if (instance? clojure.core.async.impl.channels.ManyToManyChannel result)
          (>! ch (<! (to-vec result)))
          (>! ch result))
        (close! ch))
    ch))

(defn sequential [& funs]
  (let [ch (chan)]
    (go-loop [rest-funs funs]
      (if (seq rest-funs)
        (do
          (>! ch (<! (execute-fun (first rest-funs))))
          (recur (rest rest-funs)))
        (close! ch)))
    (to-vec ch)))

(defn concurrent [& funs]
  (let [ch (chan)]
    (go
      (doseq [fc (pmap #(execute-fun %) funs)]
        (>! ch (<! fc)))
      (close! ch))
    (to-vec ch)))

(defn poll-until [fun predicate duration]
  (let [ch (chan 1)]
    (go-loop []
      (let [result (fun)]
        (if (predicate result)
          (do
            (>! ch result)
            (close! ch))
          (do
            (<! (timeout (millis duration)))
            (recur)))))
    ch))
