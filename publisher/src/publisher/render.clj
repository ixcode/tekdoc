(ns publisher.render
  (:use [compojure.core]
        [ring.util.response]
        [markdown.core]
        [clojure.java.io]
        [publisher.logging]
        [selmer.parser])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [clj-yaml.core :as yaml]
            [clj-jade.core :as jade]))

(jade/configure {:template-dir "doctek/public"
                 :pretty-print true
                 :cache? false})

(defn template-filename-for [page-id template-type]
  (format "doctek/public/%s.%s" page-id (name template-type)))

;;(def data (yaml/parse-string (slurp "doctek/public/_data.yml")))
(def data {})

(defn empty-map-for-nil [value]
  (if (nil? value)
    {}
    value))

(defn page-id-from [uri]
  "Turns e.g. '/foo/bar/index.html' into 'foo/bar/index'"
  (-> uri
      (clojure.string/split #"\.")
      (first)
      (clojure.string/replace-first "/" "")))

(defn render-jade-template-for-page-id [page-id]
  (let [template-name (format "%s.jade" page-id)        
        page-data (empty-map-for-nil ((keyword page-id) data))]
    (jade/render template-name page-data)))

(defn render-md-for-page-id [page-id]
  (let [html-to-insert (md-to-html-string (slurp (template-filename-for page-id :md)))]
    (jade/render "_layouts/md-article-page.jade" {:content html-to-insert})))

(defn render-html-for-page-id [page-id]
  (file-response (template-filename-for page-id :html)))

(def render-fns {:jade render-jade-template-for-page-id
                 :md render-md-for-page-id
                 :html render-html-for-page-id})

(defn template-exists [page-id template-type]
  (debug (format "looking for file [%s]" (.getAbsolutePath (clojure.java.io/as-file (template-filename-for page-id template-type)))))
  (.exists (clojure.java.io/as-file (template-filename-for page-id template-type))))

(defn select-template-render-fn-for [page-id]
  "Given a page-id, e.g. 'foo/bar/index' works out if it can find any templates for that id and if so picks one, searching .jade, .md (in that order). Throws an exception if it can't find any at all."
  (let [existing-templates (filter (fn [type] (template-exists page-id type)) [:jade :md])
        count-of-templates (count existing-templates)]

    (if (or (= 0 count-of-templates)
            (> 1 count-of-templates))
      (throw (RuntimeException. (format "Could not find any template [.jade or .md] for page-id [%s]." page-id))))
    ((first existing-templates) render-fns)))



