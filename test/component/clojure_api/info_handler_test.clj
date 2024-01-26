(ns component.clojure-api.info-handler-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [clojure-api.routes :refer [url-for]]
            [clojure-api.core :as core]
            [com.stuartsierra.component :as component]
            [clj-http.client :as client])
  (:import (java.net ServerSocket)
           (org.testcontainers.containers PostgreSQLContainer)))


(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))


(defn sut->url
  [sut path]
  (str/join ["http://localhost:"
             (-> sut :pedestal-component :config :server :port)
             path]))


(defn get-free-port
  []
  (with-open [socket (ServerSocket. 0)]
    (.getLocalPort socket)))


(deftest info-handler-test
  (let [database-container (doto (PostgreSQLContainer. "postgres:15.4")
                             (.withDatabaseName "clojure-api-db")
                             (.withUsername "test")
                             (.withPassword "test"))]
    (try
      (.start database-container)
      (with-system
        [sut (core/clojure-api-system {:server  {:port (get-free-port)}
                                       :htmx    {:server {:port (get-free-port)}}
                                       :db-spec {:jdbcUrl  (.getJdbcUrl database-container)
                                                 :username (.getUsername database-container)
                                                 :password (.getPassword database-container)}})]
        (is (= {:body   "hello world"
                :status 200}
               (-> (sut->url sut (url-for :info))
                   (client/get {:accept :json})
                   (select-keys [:body :status])))))
      (finally
        (.stop database-container)))))