(ns clojure-api.core
  (:require [clojure.string :as str]
            [clojure-api.components.example-component :as example-component]
            [clojure-api.components.in-memory-state-component :as in-memory-state-component]
            [clojure-api.components.pedestal-component :as pedestal-component]
            [clojure-api.config :as config]
            [com.stuartsierra.component :as component]
            [next.jdbc.connection :as connection]
            [hikari-cp.core :as cp])
  (:import (com.zaxxer.hikari HikariDataSource)))


(defn db-info-from-url [db-url]
  (let [db-uri (java.net.URI. db-url)]
    {:username      "cad"
     :password      "cad"
     :port-number   (.getPort db-uri)
     :database-name (str/replace-first (.getPath db-uri) "/" "")
     :server-name   (.getHost db-uri)}))


(def datasource-options
  (merge (db-info-from-url "postgresql://localhost:5432/clojure-api-db")
         {:auto-commit        true
          :read-only          false
          :adapter            "postgresql"
          :connection-timeout 30000
          :validation-timeout 5000
          :idle-timeout       600000
          :max-lifetime       1800000
          :minimum-idle       10
          :maximum-pool-size  20
          :pool-name          "db-pool"
          :register-mbeans    false}))


(defonce datasource
         (delay (cp/make-datasource datasource-options)))


(def database-connection {:datasource @datasource})


(defn clojure-api-system
  [config]
  (component/system-map
    :example-component (example-component/new-example-component config)
    :in-memory-state-component (in-memory-state-component/new-in-memory-state-component config)
    :data-source database-connection
    :pedestal-component (component/using
                          (pedestal-component/new-pedestal-component config)
                          [:example-component
                           :data-source
                           :in-memory-state-component])))


(defn -main
  []
  (let [system (-> (config/read-config)
                   (clojure-api-system)
                   (component/start-system))]
    (println "Starting clojure api service with config")
    (.addShutdownHook
      (Runtime/getRuntime)
      (new Thread #(component/stop-system system)))))