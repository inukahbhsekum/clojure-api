(ns component.clojure-api.todo-api-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [clojure-api.routes :refer [url-for]]
            [clojure-api.core :as core]
            [com.stuartsierra.component :as component]
            [clj-http.client :as client]
            [honey.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.net ServerSocket)
           (java.util UUID)
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


(deftest get-todo-test
  (let [database-container (PostgreSQLContainer. "postgres:15.4")]
    (try
      (.start database-container)
      (with-system
        [sut (core/clojure-api-system {:server  {:port (get-free-port)}
                                       :htmx    {:server {:port (get-free-port)}}
                                       :db-spec {:jdbcUrl  (.getJdbcUrl database-container)
                                                 :username (.getUsername database-container)
                                                 :password (.getPassword database-container)}})]
        (let [{:keys [data-source]} sut
              insert-query (sql/format {:insert-into [:todo]
                                        :columns     [:title]
                                        :values      [["My todo for test"]]
                                        :returning   :*})
              {:keys [todo-id title]} (jdbc/execute-one! (data-source)
                                                         insert-query
                                                         {:builder-fn rs/as-unqualified-kebab-maps})
              {:keys [status body]} (-> (sut->url sut (url-for :db-get-todo :path-params {:todo-id todo-id}))
                                        (client/get {:accept           :json
                                                     :as               :json
                                                     :throw-exceptions false})
                                        (select-keys [:body :status]))]
          (is (= 200 status))
          (is (= {:todo-id (str todo-id)
                  :title   title}
                 (select-keys body [:todo-id :title])))
          (testing "Empty body is returned for random todo id"
            (is (= {:body   ""
                    :status 404}
                   (-> (sut->url sut (url-for :db-get-todo :path-params {:todo-id (UUID/randomUUID)}))
                       (client/get {:throw-exceptions false})
                       (select-keys [:body :status]))))))))))