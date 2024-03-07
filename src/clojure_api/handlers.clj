(ns clojure-api.handlers
  (:require [clojure.test :refer :all]
            [clojure-api.models.info :as cmi]
            [clojure-api.schemas :as cs]
            [honey.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [schema.core :as s]
            [utils.response-utils :as ur])
  (:import [java.util UUID]))


(comment
  [{:id    (UUID/randomUUID)
    :name  "My todo list"
    :items [{:id     (UUID/randomUUID)
             :name   "Make a new youtube video"
             :status :created}]}
   {:id    (UUID/randomUUID)
    :name  "My todo list"
    :items []}])


(defn- get-todo-by-id
  [{:keys [in-memory-state-component]} todo-id]
  (->> @(:state-atom in-memory-state-component)
       (filter (fn [todo]
                 (= todo-id (:id todo))))
       (first)))


(def get-todo-handler
  {:name :get-todo-handler
   :enter
   (fn [{:keys [dependencies] :as context}]
     (let [request (:request context)
           todo (get-todo-by-id dependencies
                                (-> request
                                    :path-params
                                    :todo-id))
           response (if todo
                      (ur/ok todo)
                      (ur/not-found))]
       (assoc context :response response)))})


(def db-get-todo-handler
  {:name :db-get-todo-handler
   :enter
   (fn [{:keys [dependencies] :as context}]
     (let [{:keys [data-source]} dependencies
           todo-id (-> context
                       :request
                       :path-params
                       :todo-id
                       (UUID/fromString))
           query (sql/format {:select :*
                              :from   :todo
                              :where  [:= :todo-id todo-id]})
           todo (jdbc/execute-one! (data-source)
                                   query
                                   {:builder-fn rs/as-unqualified-kebab-maps})
           response (if todo
                      (ur/ok todo)
                      (ur/not-found))]
       (assoc context :response response)))})


(def info-handler
  {:name :info-handler
   :enter
   (fn [{:keys [dependencies] :as context}]
     (let [{:keys [data-source]} dependencies
           response (cmi/execute-query data-source "SHOW SERVER_VERSION")]
       (assoc context :response {:status 200
                                 :body   (str "Database server version: " (:server_version response))})))})


(defn- save-todo
  [{:keys [in-memory-state-component]} todo]
  (swap! (:state-atom in-memory-state-component) conj todo))


(def post-todo-handler
  {:name :post-todo-handler
   :enter
   (fn [{:keys [dependencies] :as context}]
     (let [request (:request context)
           todo (s/validate cs/Todo (get-in request [:json-params]))]
       (save-todo dependencies todo)
       (assoc context :response (ur/created todo))))})


(defn respond-hello
  [request]
  {:status 200
   :body   "Hello, world!\n"})