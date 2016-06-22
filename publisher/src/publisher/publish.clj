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
            )
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


(defmulti process-page (fn [export-root page-file] (:type page-file)))

(defmethod process-page :default [export-root page-file]
  (throw (IllegalArgumentException. (str "Could not process a file of type " (:type page-file)))))

(defmethod process-page :md [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)]
    (println (str "[markdown] " (:relative-filename page-file) " -> " output-file))
    (fs/mkdirs (fs/parent output-file))
    (spit output-file (md/md-to-html-string (->> (slurp (:filepath page-file))
                                                 (swap-md-for-html))))))

(defmethod process-page :jade [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)
        rendered-content (jade/render (:relative-filename page-file) page-context)]
    (println (str "[jade] " (:relative-filename page-file) " -> " output-file))
    (fs/mkdirs (fs/parent output-file))
    (spit output-file rendered-content)))


(defmethod process-page :png [export-root page-file]
  (let [output-file (make-output-file export-root page-file :png)]
    (println (str "[png] " (:relative-filename page-file) " -> " output-file))
    (fs/copy (:filepath page-file) output-file)))


(defmethod process-page :html [export-root page-file]
  (let [output-file (make-output-file export-root page-file :html)]
    (.createNewFile output-file)
    (println (str "[selmer  ] " (:relative-filename page-file) " -> " output-file))
    (spit output-file (selmer/render-file  (:relative-filename page-file) page-context ))))


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

(defn export-site [export-root content-root]
  (.mkdirs (clojure.java.io/file export-root))
  (fs/delete-dir config/output-root)
  (fs/mkdirs config/output-root)
  (copy-static-site-files config/static-root config/output-root)
  (process-page-files export-root (list-page-files config/content-root)))

(defn -main [& args]
  (let [site-config-file (first args)]
    (config/initialise! site-config-file))
  (println "Going to publish the site...")
  (println "Content        : " config/content-root)
  (println "Static Content : " config/static-root)
  (println "Output         : " config/output-root)
  (export-site config/output-root config/content-root)
)
