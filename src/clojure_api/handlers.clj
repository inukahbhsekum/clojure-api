(ns clojure-api.handlers
  (:require [clojure.test :refer :all]
            [clojure-api.schemas :as cs]
            [schema.core :as s]
            [utils.response-utils :as ur]))


(comment
  [{:id    (random-uuid)
    :name  "My todo list"
    :items [{:id     (random-uuid)
             :name   "Make a new youtube video"
             :status :created}]}
   {:id    (random-uuid)
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