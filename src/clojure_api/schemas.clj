(ns clojure-api.schemas
  (:require [clojure.test :refer :all]
            [schema.core :as s]))


(s/defschema
  TodoItem
  {:id     s/Str
   :name   s/Str
   :status s/Str})

(s/defschema
  Todo
  {:id    s/Str
   :name  s/Str
   :items [TodoItem]})
