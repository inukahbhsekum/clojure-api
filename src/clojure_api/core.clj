(ns clojure-api.core
  (:require [clojure-api.components.example-component :as example-component]
            [clojure-api.components.in-memory-state-component :as in-memory-state-component]
            [clojure-api.components.pedestal-component :as pedestal-component]
            [clojure-api.config :as config]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [hikari-cp.core :as cp])
  (:import (java.net URI)))


(defn- db-info-from-url
  [config]
  (let [db-spec (:db-spec config)
        db-uri (URI. (:jdbcUrl db-spec))]
    (println "db-url: " db-uri)
    {:username      (:username db-spec)
     :password      (:password db-spec)
     :port-number   "5432"
     :database-name (str/replace-first (.getPath db-uri) "/" "")
     :server-name   (.getHost db-uri)}))


(defn- datasource-options
  [config]
  (merge (db-info-from-url config)
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


(defn clojure-api-system
  [config]
  (let [data-source (delay (-> config
                               datasource-options
                               cp/make-datasource))]
    (component/system-map
      :example-component (example-component/new-example-component config)
      :in-memory-state-component (in-memory-state-component/new-in-memory-state-component config)
      :data-source data-source
      :pedestal-component (component/using
                            (pedestal-component/new-pedestal-component config)
                            [:example-component
                             :data-source
                             :in-memory-state-component]))))


(defn -main
  []
  (let [system (-> (config/read-config)
                   (clojure-api-system)
                   (component/start-system))]
    (println "Starting clojure api service with config")
    (.addShutdownHook
      (Runtime/getRuntime)
      (new Thread #(component/stop-system system)))))