(ns clojure-api.models.info
  (:require [next.jdbc :as jdbc]))


(defn execute-query
  [datasource query]
  (let [db-response (first (jdbc/execute! (:data-source datasource) [query]))]
    db-response))
