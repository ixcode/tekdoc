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

(defn exec-shell [& args]
  (let [cmd-output (apply sh args)
        out (:out cmd-output)]
    (println out)
    out))

;; TODO - should probably use git rev-parse and remember the last revision for more scientific comparison
(defn pull-from-git [content-root]
  (println "Checking and updating repo @ " )
  (let [out (exec-shell "git" "pull" "--rebase" :dir content-root)]
    (if (str/starts-with? (:out out) "Current branch master is up to date.")
      { :up-to-date? true }
      { :up-to-date? false})))


(defn publish-site [output-root publish-root]
  (println "Copying site accross from " output-root " to " publish-root)
  (exec-shell "rm" "-r" (format "%s/*"))
  (exec-shell "cp" "-R" (format "%s/*" output-root) publish-root))

(defn trigger-ci [request]
  (pprint (:body  request))
  (pull-from-git config/content-root)
  (publish/export-site config/output-root config/content-root)
  (publish-site config/output-root config/publish-root)
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
