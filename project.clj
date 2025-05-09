(defproject ko-premium-algo "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]
                 [environ "1.2.0"]
                 [buddy/buddy-sign "3.1.0"]
                 [buddy/buddy-core "1.12.0-430"]
                 [org.clojure/core.async "1.7.701"]
                 [org.clojure/math.combinatorics "0.3.0"]]
  :plugins [[lein-environ "1.2.0"]])
