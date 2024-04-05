(defproject ko-premium-algo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]
                 ]
  :plugins [[lein-ring "0.12.5"] [lein-cljfmt "0.9.2"]]
  :ring {:handler ko-premium-algo.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
