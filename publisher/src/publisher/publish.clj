(ns publisher.publish
  (:use [markdown.core]
        [clojure.java.io])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]            
            [clj-jade.core :as jade]
            [publisher.config :as config]
            [selmer.parser :as selmer]))


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
(def content-files-pattern #"(?!.*README.md).*\.(md|jade|html|jpg|png)$")
;; (re-matches content-files-pattern "README.md")
;; (re-matches content-files-pattern "foo.md")
;; (re-matches content-files-pattern "foo/bar/README.md")


(defn extract-page-id [filepath]
  (:file-path-without-extension (parse-filepath filepath)))

(defn page-file [content-root file]
  (let [filepath (.getPath file)
        file-details (parse-filepath filepath)
        relative-path (clojure.string/replace (:filepath-without-extension file-details) content-root "")]
    {:filepath filepath
     :name (:name file-details)
     :filename (:filename file-details)
     :page-id relative-path
     :relative-filename (str relative-path "." (:extension file-details))
     :type (keyword (:extension file-details))}))

(defn list-page-files [content-root]
  (map (partial page-file content-root) (walk content-root content-files-pattern)))

(defn make-output-file [export-root page-file extension]
  (clojure.java.io/file  (str export-root (:page-id page-file) "." (name extension))))

(defmulti process-page (fn [export-root page-file] (:type page-file)))

(defmethod process-page :default [export-root page-file]
  (throw (IllegalArgumentException. (str "Could not process a file of type " (:type page-file)))))

(defmethod process-page :md [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)]
    (println (str "[markdown] " (:relative-filename page-file) " -> " output-file))))

(def page-context
  {:page {
          :publish {:timestamp (str (new java.util.Date))}
          :source "gitlab/foo/bar"}})

(defmethod process-page :html [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)]
    (.createNewFile output-file)
    (println (str "[selmer  ] " (:relative-filename page-file) " -> " output-file))
    (spit output-file (selmer/render-file  (:relative-filename page-file) page-context ))))


;;(def test-page {:type :html})

;;(let [output-file (str export-root (:page-id page-file) ".html")])
(defn process-page-files [export-root page-files]
  (dorun (map (partial process-page export-root) page-files)))


(defn export-site [export-root content-root]
  (.mkdirs (clojure.java.io/file export-root))
  (println "Going to be generating output in " export-root)
  (process-page-files export-root (list-page-files config/content-root)))

;; (export-site config/output-root config/content-root)
