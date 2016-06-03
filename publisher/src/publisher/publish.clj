(ns publisher.publish
  (:use [markdown.core]
        [clojure.java.io])
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
     :name (:name file-details)
     :filename (:filename file-details)
     :page-id (:filepath-without-extension file-details)
     :type (keyword (:extension file-details))}))

(defn list-page-files [root-dir]
  (map #(page-file %) (walk root-dir content-files-pattern)))

(defmulti process-page (fn [export-root page-file] (:type page-file)))

(defmethod process-page :md [export-root page-file]
  (println (str "Writing markdown file " page-file " to  " export-root)))

(defmethod process-page :html [export-root page-file]
  (println (str "Writing a selmer template " page-file " to " export-root)))

(defmethod process-page :default [export-root page-file]
  (throw (IllegalArgumentException. (str "Could not process a file of type " (:type page-file)))))

;;(def test-page {:type :html})

;;(let [output-file (str export-root (:page-id page-file) ".html")])
(defn process-page-files [export-root page-files]
  (dorun (map (partial process-page export-root) page-files)))


(defn export-site [export-root content-root]
  (process-page-files export-root (list-page-files config/content-root)))

