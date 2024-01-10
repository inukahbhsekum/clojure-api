(ns unit.clojure-api.simple-test
  (:require [clojure.test :refer :all]
            [clojure-api.routes :refer [url-for]]
            [next.jdbc :as jdbc])
  (:import [java.util UUID]
           [org.testcontainers.containers PostgreSQLContainer]))


(deftest a-simple-passing-test
  (is (= 1 1)))


(deftest a-simple-persistence-test
  (let [database-container (doto (PostgreSQLContainer. "postgres:15.4")
                             (.withDatabaseName "clojure-api-db")
                             (.withUsername "test")
                             (.withPassword "test"))]
    (try
      (.start database-container)
      (let [ds (jdbc/get-datasource {:jdbcUrl  (.getJdbcUrl database-container)
                                     :user     (.getUsername database-container)
                                     :password (.getPassword database-container)})]
        (is (= {:r 1} (first (jdbc/execute! ds ["select 1 as r;"])))))
      (finally
        (.stop database-container)))))


(deftest url-for-test
  (testing "greet endpoint url"
    (is (= "/greet" (url-for :greet))))
  (testing "get todo by id endpoint url"
    (let [todo-id (UUID/randomUUID)]
      (is (= (str "/todo/" todo-id)
             (url-for :get-todo {:path-params {:todo-id todo-id}}))))))