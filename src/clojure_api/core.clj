(ns clojure-api.core
  (:require [clojure-api.components.example-component :as example-component]
            [clojure-api.components.pedestal-component :as pedestal-component]
            [clojure-api.config :as config]
            [com.stuartsierra.component :as component]))


(defn clojure-api-system
  [config]
  (component/system-map
    :example-component (example-component/new-example-component config)
    :pedestal-component (component/using
                          (pedestal-component/new-pedestal-component config)
                          [:example-component])))


(defn -main
  []
  (let [system (-> (config/read-config)
                   (clojure-api-system)
                   (component/start-system))]
    (println "Starting clojure api service with config")
    (.addShutdownHook
      (Runtime/getRuntime)
      (new Thread #(component/stop-system system)))))