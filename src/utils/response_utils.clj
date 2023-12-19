(ns utils.response-utils
  (:require [clojure.test :refer :all]
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