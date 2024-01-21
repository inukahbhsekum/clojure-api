(defproject clojure-api "0.1.0-SNAPSHOT"
  :description "Basic clojure api"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[aero "1.1.6"]
                 [clj-http "3.12.3"]
                 [com.stuartsierra/component "1.1.0"]
                 [com.stuartsierra/component.repl "0.2.0"]
                 [io.pedestal/pedestal.jetty "0.6.0"]
                 [io.pedestal/pedestal.route "0.6.0"]
                 [io.pedestal/pedestal.service "0.6.0"]
                 [org.clojure/clojure "1.10.3"]
                 [org.slf4j/slf4j-simple "2.0.7"]
                 [org.clojure/tools.logging "1.2.4"]
                 [prismatic/schema "1.4.1"]
                 [org.testcontainers/testcontainers "1.18.0"]
                 [org.testcontainers/postgresql "1.18.0"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [org.postgresql/postgresql "42.2.10"]
                 [com.zaxxer/HikariCP "5.0.1"]
                 [hiccup/hiccup "2.0.0-RC1"]
                 [hikari-cp "3.0.1"]
                 [faker/faker "0.3.2"]]
  :main ^:skip-aot clojure-api.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})