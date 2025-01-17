(ns ko-premium-algo.lib.file
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn- extract-extension [path]
  (let [filename (last (str/split path #"/"))]
    (when (str/includes? filename ".")
      (last (str/split filename #"\.")))))

(defn- system [generate parse path]
  (fn
    ([type content]
     (cond
       (= type :save) (spit path (generate content))))
    ([type]
     (cond
       (= type :read) (if (.exists (io/file path)) (parse (slurp path)) nil)))))

(defmulti make-file-manager extract-extension)

(defmethod make-file-manager "json" [path]
  (system json/generate-string json/parse-string path))

(defmethod make-file-manager "edn" [path]
  (system identity read-string path))
