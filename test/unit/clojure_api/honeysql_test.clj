(ns unit.clojure-api.honeysql-test
  (:require [clojure-api.core :as core]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql])
  (:import (org.testcontainers.containers PostgreSQLContainer)))


(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))


(defn datasource-only-system
  [config]
  (component/system-map :data-source (core/datasource-component config)))


(defn create-database-container
  []
  (PostgreSQLContainer. "postgres:15.4"))


(deftest migrations-test
  (let [database-container (create-database-container)]
    (try
      (.start database-container)
      (with-system
        [sut (datasource-only-system {:db-spec {:jdbcUrl  (.getJdbcUrl database-container)
                                                :username (.getUsername database-container)
                                                :password (.getPassword database-container)}})]
        (let [{:keys [data-source]} sut
              select-query (sql/format {:select :*
                                        :from   :schema-version})
              [schema-version :as schema-versions]
              (jdbc/execute!
                (data-source)
                select-query
                {:builder-fn rs/as-unqualified-maps})]
          (is (= 1 (count schema-versions)))
          (is (= {:description "add todo tables"
                  :script      "V1__add_todo_tables.sql"
                  :success     true}
                 (select-keys schema-version [:description :script :success])))))
      (finally
        (.stop database-container)))))


(deftest todo-table-test
  (let [database-container (create-database-container)]
    (try
      (.start database-container)
      (with-system
        [sut (datasource-only-system {:db-spec {:jdbcUrl  (.getJdbcUrl database-container)
                                                :username (.getUsername database-container)
                                                :password (.getPassword database-container)}})]
        (let [{:keys [data-source]} sut
              insert-query (-> {:insert-into [:todo]
                                :columns     [:title]
                                :values      [["my todo list"]
                                              ["other todo list"]]
                                :returning   :*}
                               (sql/format))
              select-query (-> {:select :*
                                :from   :todo}
                               (sql/format))
              insert-results (jdbc/execute! (data-source)
                                            insert-query
                                            {:builder-fn rs/as-unqualified-maps})
              select-results (jdbc/execute! (data-source)
                                            select-query
                                            {:builder-fn rs/as-unqualified-maps})]
          (is (= 2 (count insert-results) (count select-results)))
          (is (= #{"my todo list" "other todo list"}
                 (->> insert-results (map :title) (into #{}))
                 (->> select-results (map :title) (into #{})))))))))