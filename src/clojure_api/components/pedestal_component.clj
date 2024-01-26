(ns clojure-api.components.pedestal-component
  (:require [clojure.tools.logging :as ctl]
            [clojure-api.routes :as routes]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.http.content-negotiation :as content-negotiation]))


(defn inject-dependencies
  [dependencies]
  (interceptor/interceptor
    {:name  ::inject-dependencies
     :enter (fn [context]
              (assoc context :dependencies dependencies))}))


(def content-negotiation-interceptor
  (content-negotiation/negotiate-content ["application/json"]))


(defrecord PedestalComponent
  [config example-component data-source in-memory-state-component]
  component/Lifecycle

  (start [component]
    (ctl/info "Starting PedestalComponent")
    (let [server (-> {::http/routes routes/routes
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
    (ctl/info "Stopping PedestalComponent")
    (when-let [server (:server component)]
      (http/stop server))
    (assoc component :server nil)))


(defn new-pedestal-component
  [config]
  (print "config: " config)
  (map->PedestalComponent {:config config}))