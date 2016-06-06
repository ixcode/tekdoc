(ns publisher.server
  (:use [compojure.core]
        [ring.util.response]
        [markdown.core]
        [clojure.java.io]
        [publisher.render])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [clj-yaml.core :as yaml]
            [clj-jade.core :as jade])
  (:gen-class))

(defn render-page [request]
  (let [uri (:uri request)
        page-id (page-id-from uri)
        render (select-template-render-fn-for page-id)
       ]
    (render page-id)))

(defroutes app-routes
  (GET "/" request (redirect "/index.html"))
  (route/resources "/")
  (GET "/*" request (response (render-page request))))


(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-response)
      ))


(defn -main [& args]
  (if (not (empty? args))
    (jetty/run-jetty app {:port (read-string (first args))})
    (jetty/run-jetty app {:port 8099}))
)



