(ns publisher.ci
  (:use [compojure.core]
        [ring.util.response]
        [clojure.pprint]
        [clojure.java.shell :only [sh]])
  (:require [publisher.publish :as publish]
            [publisher.config :as config]
            [compojure.handler :as handler]
            [clojure.string :as str]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

;; TODO - should probably use git rev-parse and remember the last revision for more scientific comparison
(defn pull-from-git [repository-url]
  (let [cmd-output (sh "git" "pull" "--rebase" :dir repository-url)]
    (if (str/starts-with? (:out cmd-output) "Current branch master is up to date.")
      { :up-to-date? true }
      { :up-to-date? false})))

(defn trigger-ci [request]
  (pprint (:body  request))
  (publish/export-site config/output-root config/content-root)
  (response { :is ["message"] :text "Just published the website!"}))

(defroutes app-routes
  (POST "/trigger-ci" request (trigger-ci request)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-response)
      (middleware/wrap-json-body {:keywords? true})
      ))

(defn -main [& args]
  (if (not (empty? args))
    (let [site-config-file (first args)]
    (config/initialise! site-config-file)))
    
  (jetty/run-jetty app {:port 8087})
)
