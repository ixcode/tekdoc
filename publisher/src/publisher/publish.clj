(ns publisher.publish
  (:require [clojure.java.io :as io]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]            
            [clj-jade.core :as jade]
            [publisher.config :as config]
            [selmer.parser :as selmer]
            [me.raynes.fs :as fs]
            [markdown.core :as md]
            [clj-yaml.core :as yaml]
            [publisher.data-parse :as dp])
  (:gen-class))

(defn swap-md-for-html [input-string]
  (-> (clojure.string/replace input-string ".md)" ".html)")
      (clojure.string/replace ".md#" ".html#")))
    

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
                 (file-seq (io/file dirpath)))))

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
     :dir (.getParent file)
     :name (:name file-details)
     :filename (:filename file-details)
     :page-id relative-path
     :relative-filename (str relative-path "." (:extension file-details))
     :type (keyword (:extension file-details))}))

(defn list-page-files [content-root]
  (map (partial page-file content-root) (walk content-root content-files-pattern)))

(defn make-output-file [export-root page-file extension]
  (clojure.java.io/file  (str export-root (:page-id page-file) "." (name extension))))


(def page-context
  {:page {
          :publish {:timestamp (str (new java.util.Date))}
          :source "gitlab/foo/bar"}})

;; To test:
;; (def input-file (fs/file "some/path/to/a/source/file"))
;; (def page (page-file config/content-root input-file))
;; (process-page (fs/file config/output-root) page)
(defmulti process-page (fn [export-root page-file] (:type page-file)))

(defmethod process-page :default [export-root page-file]
  (println (str "Could not process a file of type " (:type page-file) ", ignoring.")))

(defmethod process-page :md [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)
        rendered-md (md/md-to-html-string-with-meta (->> (slurp (:filepath page-file))
                                                         (swap-md-for-html))
                                                    :heading-anchors true)
        html (:html rendered-md)
        template-meta (:template (:metadata rendered-md))
        template-name (if (nil? template-meta) "md-page-default" (first template-meta))
        template-file (format "_layouts/%s.jade" template-name)
        rendered-page (jade/render template-file (merge page-context {:content html}))]
    (println (str "[markdown] " (:relative-filename page-file) " -> " output-file " : " template-file))    
    (fs/mkdirs (fs/parent output-file))
    (spit output-file rendered-page)))

(defmethod process-page :jade [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)
        rendered-page (jade/render (:relative-filename page-file) page-context)]
    (println (str "[jade] " (:relative-filename page-file) " -> " output-file))
    (fs/mkdirs (fs/parent output-file))
    (spit output-file rendered-page)))


(defmethod process-page :png [export-root page-file]
  (let [output-file (make-output-file export-root page-file :png)]
    (println (str "[png] " (:relative-filename page-file) " -> " output-file))
    (fs/mkdirs (fs/parent output-file))
    (fs/copy (:filepath page-file) output-file)))


(defmethod process-page :html [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)]
    (.createNewFile output-file)
    (println (str "[selmer  ] " (:relative-filename page-file) " -> " output-file))
    (spit output-file (selmer/render-file  (:relative-filename page-file) page-context ))))

;;(def test-root ".../tekdoc/publisher/test/publisher")
;;(def test-file (fs/file test-root "list-of-people.yaml"))
;;(def p (page-file test-root test-file))
(defmethod process-page :yaml [export-root page-file]
  (let [{:keys [template data-spec]} (yaml/parse-string (slurp (:filepath page-file)))
        {:keys [source join]} data-spec
        insert-data (dp/load-data-from-tsv (str (:dir page-file) "/" source))
        template-data (conj page-context {:data insert-data})
        output-file (make-output-file export-root page-file :html)
        rendered-page (jade/render (format "_layouts/%s.jade" template) template-data)]
    (println (str "[yaml    ] " (:relative-filename page-file) " -> " output-file))
    (fs/mkdirs (fs/parent output-file))
    (spit output-file rendered-page)))

;;(def test-page {:type :html})

;;(let [output-file (str export-root (:page-id page-file) ".html")])
(defn process-page-files [export-root page-files]
  (dorun (map (partial process-page export-root) page-files)))

(defn valid-file? [input-file]
  (not (fs/hidden? input-file)))

(defn copy-static [output-root input-file]
  (println "processing-static-file: " (.getAbsolutePath input-file))
  (if (valid-file? input-file) 
    (if (fs/directory? input-file)
      (fs/copy-dir input-file output-root)
      (fs/copy input-file (fs/file output-root (.getName input-file))))))

(defn copy-static-site-files [static-root output-root]
  (let [list-of-files (fs/list-dir static-root)]
    (dorun (map (partial copy-static output-root) list-of-files))))


(defn export-site [output-root content-root static-root]  
  (fs/delete-dir output-root)
  (fs/mkdirs output-root)
  (copy-static-site-files static-root output-root)
  (process-page-files output-root (list-page-files content-root)))



(defn -main [& args]
  (let [site-config-file (first args)]
    (config/initialise! site-config-file))
  (let [{:keys [:output-root :content-root :static-root]} (config/get-config)]
    (export-site output-root content-root static-root)))
