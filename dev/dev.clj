(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [clojure-api.config :as config]
            [clojure-api.core :as core]))


(component-repl/set-init
  (fn [_old-system]
    (core/clojure-api-system (config/read-config))))