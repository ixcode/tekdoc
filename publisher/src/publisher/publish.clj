(ns publisher.publish
  (:use [compojure.core]
        [ring.util.response]
        [markdown.core]
        [clojure.java.io]
        [publisher.render]
        [publisher.config])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]            
            [clj-jade.core :as jade]))




(defn parse-filepath [filepath]
  (let [regex #"((.*\/)*)(.+)\.(.*)$"
        match (re-matches regex filepath)
        path (nth match 1)
        filename (nth match 3)
        extension (nth match 4)
        filepath-without-extension (format "%s%s" path filename)]
    {:path path
     :filename filename
     :extension extension
     :filepath-without-extension filepath-without-extension}))

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getPath %))
                 (file-seq (file dirpath)))))

(def match-all #".*\.(md|jade)$")
(def valid-content-files #"([a-z0-9\.\-]*\/)*([a-z0-9\.\-]+)\.(md|jade|html)$")

(defn extract-page-id [filepath]
  (:full-path-without-extension (parse-filepath filepath)))

(defn list-page-ids [root-dir]
  (map extract-page-id (walk root-dir valid-content-files)))

(defn export-site [root-dir, export-dir]
  )

;;(map #(println (.getPath %)) (walk "doctek/public" match-not-underscores))



;;(.getPath  (as-file "doctek/public/index.jade"))
