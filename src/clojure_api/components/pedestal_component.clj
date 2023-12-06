(ns clojure-api.components.pedestal-component
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.http.content-negotiation :as content-negotiation]
            [io.pedestal.http.body-params :as body-params]
            [cheshire.core :as json]))


(defn response
  ([status]
   (response status nil))
  ([status body]
   (merge
     {:status  status
      :headers {"Content-type" "application/json"}}
     (when body {:body (json/encode body)}))))


(def ok (partial response 200))
(def not-found (partial response 404))
(def created (partial response 201))

(defn get-todo-by-id
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
                      (ok todo)
                      (not-found))]
       (assoc context :response response)))})


(defn save-todo
  [{:keys [in-memory-state-component]} todo]
  (reset! (:state-atom in-memory-state-component) [todo]))


(def post-todo-handler
  {:name :post-todo-handler
   :enter
   (fn [{:keys [dependencies] :as context}]
     (let [request (:request context)
           todo {:json-params request}]
       (save-todo dependencies todo)
       (assoc context :response (created todo))))})


(comment
  [{:id    (random-uuid)
    :name  "My todo list"
    :items [{:id     (random-uuid)
             :name   "Make a new youtube video"
             :status :created}]}
   {:id    (random-uuid)
    :name  "My todo list"
    :items []}])


(defn respond-hello
  [request]
  {:status 200
   :body   "Hello, world!\n"})


(def routes (route/expand-routes
              #{["/greet" :get respond-hello :route-name :greet]
                ["/todo/:todo-id" :get get-todo-handler :route-name :get-todo]
                ["/todo" :post [(body-params/body-params) post-todo-handler] :route-name :post-todo]}))


(def url-for (route/url-for-routes routes))


(defn inject-dependencies
  [dependencies]
  (interceptor/interceptor
    {:name  ::inject-dependencies
     :enter (fn [context]
              (assoc context :dependencies dependencies))}))


(def content-negotiation-interceptor
  (content-negotiation/negotiate-content ["application/json"]))


(defrecord PedestalComponent
  [config example-component in-memory-state-component]
  component/Lifecycle

  (start [component]
    (println "Starting PedestalComponent")
    (let [server (-> {::http/routes routes
                      ::http/type   :jetty
                      ::http/join?  false
                      ::http/port   (-> config
                                        :server
                                        :port)}
                     (http/default-interceptors)
                     (update ::http/interceptors concat
                             [(inject-dependencies component)
                              content-negotiation-interceptor])
                     (http/create-server)
                     (http/start))]
      (assoc component :server server)))

  (stop [component]
    (println "Stopping PedestalComponent")
    (when-let [server (:server component)]
      (http/stop server))
    (assoc component :server nil)))


(defn new-pedestal-component
  [config]
  (map->PedestalComponent {:config config}))