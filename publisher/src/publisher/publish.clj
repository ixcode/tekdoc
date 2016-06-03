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
        name (nth match 3)
        extension (nth match 4)
        filepath-without-extension (format "%s%s" path name)
        filename (format "%s.%s" name extension)]
    {:path path
     :name name
     :extension extension
     :filepath-without-extension filepath-without-extension
     :filename filename}))

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getPath %))
                 (file-seq (file dirpath)))))

;; hmtl files are treated as selmer (like jinja) templates)
(def content-files-pattern #".*\.(md|jade|html|jpg|png)$")


(defn extract-page-id [filepath]
  (:file-path-without-extension (parse-filepath filepath)))

(defn page-file [file]
  (let [filepath (.getPath file)
        file-details (parse-filepath filepath)]
    {:filepath filepath
     :filename (:filename file-details)
     :page-id (:filepath-without-extension file-details)
     :type (keyword (:extension file-details))}))

(def list-of-page-files (map #(page-file %) (walk config/content-root content-files-pattern)))


(defn export-site [root-dir, export-dir]
  )

