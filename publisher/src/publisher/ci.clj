(ns publisher.ci
  (:use [compojure.core]
        [ring.util.response]
        [clojure.pprint])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn trigger-ci [request]
  (pprint (:body  request))
  (response { :is ["message"] :text "Thanks for the data!"}))

(defroutes app-routes
  (POST "/trigger-ci" request (trigger-ci request)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-response)
      (middleware/wrap-json-body {:keywords? true})
      ))

(defn -main [& args]
  (if (not (empty? args))
    (jetty/run-jetty app {:port (read-string (first args))})
    (jetty/run-jetty app {:port 8097}))
)
