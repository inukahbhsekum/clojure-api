(ns unit.clojure-api.simple-test
  (:require [clojure.test :refer :all]
            [clojure-api.routes :refer [url-for]])
  (:import [java.util UUID]))


(deftest a-simple-passing-test
  (is (= 1 1)))


(deftest url-for-test
  (testing "greet endpoint url"
    (is (= "/greet" (url-for :greet))))
  (testing "get todo by id endpoint url"
    (let [todo-id (UUID/randomUUID)]
      (is (= (str "/todo/" todo-id)
             (url-for :get-todo {:path-params {:todo-id todo-id}}))))))