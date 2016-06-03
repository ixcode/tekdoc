(ns publisher.publish
  (:use [markdown.core]
        [clojure.java.io]
        [publisher.render])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]            
            [clj-jade.core :as jade]
            [publisher.config :as config]))


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

;; hmtl files are treated as selmer (like jinja) templates)
(def content-files-pattern #".*\.(md|jade|html|jpg|png)$")


(defn extract-page-id [filepath]
  (:full-path-without-extension (parse-filepath filepath)))

(defn list-page-ids [root-dir]
  (map extract-page-id (walk root-dir valid-content-files)))

(defn export-site [root-dir, export-dir]
  )


(def list-of-content-files (map #(.getPath %) (walk config/content-root content-files-pattern)))



;;(.getPath  (as-file "doctek/public/index.jade"))
