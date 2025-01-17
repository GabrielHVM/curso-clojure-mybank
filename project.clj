(defproject mybank-web-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.slf4j/slf4j-simple "2.0.3"]
                 [org.clojure/clojure "1.11.1"]
                 [io.pedestal/pedestal.service "0.5.10"]
                 [io.pedestal/pedestal.route "0.5.10"]
                 [io.pedestal/pedestal.jetty "0.5.10"]
                 [clj-http "3.12.3"]
                 [com.stuartsierra/component "1.1.0"]
                 [walmartlabs/system-viz "0.4.0"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot mybank-web-api.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
