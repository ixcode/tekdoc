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
            [ring.adapter.jetty :as jetty]
            [me.raynes.fs :as fs])
  (:gen-class))

(defn exec-shell [& args]
  (let [cmd-output (apply sh args)
        out (:out cmd-output)
        err (:err cmd-output)
        exit (:exit cmd-output)]    
    (println "executed " args)
    (println "exit status: " exit)
    (println "----------------------------------------------------------")
    (println "stdout")   
    (println out)    
    (println "----------------------------------------------------------")
    (println "stderr")
    (println err)    
    (println "----------------------------------------------------------")

    
    out))

;; TODO - should probably use git rev-parse and remember the last revision for more scientific comparison
(defn pull-from-git [content-root]
  (println "Checking and updating repo @ " )
  (let [out (exec-shell "git" "pull" "--rebase" :dir content-root)]
    (if (str/starts-with? out "Current branch master is up to date.")
      { :up-to-date? true }
      { :up-to-date? false})))


(defn publish-site [output-root publish-root]
  (let [publish-pattern (format "%s/*" publish-root)
        output-pattern (format "%s/" output-root)]
    
    (println "Removing files in [" publish-pattern "]")    
    (exec-shell "sh" "-c" (format  "rm -rv %s" publish-pattern))
    
    (println "Copying site accross from [" output-pattern "] to [" publish-root "]")   
    (exec-shell "sh" "-c" (format  "cp -Rv %s %s" output-pattern publish-root))))

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
