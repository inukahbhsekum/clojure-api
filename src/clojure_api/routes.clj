(ns clojure-api.routes
  (:require [clojure-api.handlers :as handlers]
            [clojure.test :refer :all]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route :as route]))


(def routes (route/expand-routes
              #{["/greet" :get handlers/respond-hello :route-name :greet]
                ["/info" :get handlers/info-handler :route-name :info]
                ["/todo/:todo-id" :get handlers/get-todo-handler :route-name :get-todo]
                ["/todo" :post [(body-params/body-params) handlers/post-todo-handler] :route-name :post-todo]}))


(def url-for (route/url-for-routes routes))