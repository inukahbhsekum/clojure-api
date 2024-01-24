(ns clojure-api.components.db-component
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [hikari-cp.core :as cp])
  (:import (java.net URI)))


(defn- db-info-from-url [config]
  (let [db-spec (-> config
                    :db-spec)
        db-uri (URI. (:jdbcUrl db-spec))]
    {:username      (:username db-spec)
     :password      (:password db-spec)
     :port-number   (.getPort db-uri)
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


(defrecord DbComponent
  [config]
  component/Lifecycle

  (start [component]
    (println "Starting DB component")
    (let [datasource (delay (-> config
                                datasource-options
                                cp/make-datasource))]
      (assoc component :data-source @datasource)))

  (stop [component]
    (println "Stopping DB component")
    (let [datasource (:data-source component)]
      (cp/close-datasource @datasource))))


(defn new-db-component
  [config]
  (map->DbComponent {:config config}))
