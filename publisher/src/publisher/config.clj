(ns publisher.config
  (:require [selmer.parser :as selmer]
            [clj-yaml.core :as yaml]
            [clj-jade.core :as jade]))


(def page-context {:page {:publish {:timestamp "2016-05-31"} :source "https://gitlab.some/source.html"}})

(defn expand-home [s]
  (if (.startsWith s "~")
    (clojure.string/replace-first s "~" (System/getProperty "user.home"))
    s))

(def tekdoc-config (first (yaml/parse-string (slurp (expand-home "~/.tekdoc.yml")))))
(def site-config (first (yaml/parse-string (slurp (:test-site tekdoc-config)))))


(defn resolve-relative-path [tekdoc-config-file relative-path]
  (let [root-path (.getParent (clojure.java.io/file tekdoc-config-file))]
    (.getCanonicalPath (clojure.java.io/file (str  root-path "/" relative-path)))))

(defn path-from-config [key]
  (resolve-relative-path (:test-site tekdoc-config) (key site-config)))

(def content-root (path-from-config :content))
(def static-root (path-from-config :static))
(def output-root (path-from-config :output))
(def layout-root (path-from-config :layouts))
(def publish-root (path-from-config :publish))


(defn initialise! [site-config-file]  
  (selmer/set-resource-path! content-root)

  ;; We never want caching on for now
  (selmer/cache-off!)

  (jade/configure {:template-dir content-root
                 :pretty-print true
                 :cache? false}))

