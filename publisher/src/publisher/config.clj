(ns publisher.config
  (:require [selmer.parser :as selmer]
            [clj-yaml.core :as yaml]
            [clj-jade.core :as jade]))


(def page-context {:page {:publish {:timestamp "2016-05-31"} :source "https://gitlab.some/source.html"}})

(defn expand-home [s]
  (if (.startsWith s "~")
    (clojure.string/replace-first s "~" (System/getProperty "user.home"))
    s))


(defn resolve-relative-path [tekdoc-config-file relative-path]
  (let [root-path (.getParent (clojure.java.io/file tekdoc-config-file))]
    (.getCanonicalPath (clojure.java.io/file (str  root-path "/" relative-path)))))

(def -config (atom {}))
(defn get-config [] @-config)


(defn init-config! [site-config-file]
  (println "[init-config!] site-config-file : " site-config-file)
  (let [site-config (first  (yaml/parse-string (slurp (expand-home site-config-file))))
        new-config {:config-source site-config-file
                    :scm-root (:scm site-config)
                    :content-root (resolve-relative-path site-config-file (:content site-config))
                    :static-root (resolve-relative-path site-config-file (:static site-config))
                    :output-root (resolve-relative-path site-config-file (:output site-config))
                    :layouts-root (resolve-relative-path site-config-file (:layouts site-config))
                    :publish-root (:publish site-config)}]
    (swap! -config merge new-config)))

(defn debug-config []
  (let [{:keys [:config-source :scm-root :content-root
                :static-root :output-root :publish-root]} (get-config)]
    (str  "---------------------------------------------------------------\n"
          "Site config    : " config-source "\n"
          "---------------------------------------------------------------\n"    
          "SCM            : " scm-root "\n"
          "Content        : " content-root "\n"
          "Static Content : " static-root "\n"
          "Output         : " output-root "\n"
          "Publish to     : " publish-root "\n"
          "---------------------------------------------------------------\n")))

(defn initialise! [site-config-file]
  (init-config! site-config-file)
  (println (debug-config))
  (let [{:keys [:content-root]} (get-config)]
    (selmer/set-resource-path! content-root)

    (println "Initialised selmer to : " content-root)

    ;; We never want caching on for now
    (selmer/cache-off!)

    (jade/configure {:template-dir content-root
                     :pretty-print true
                     :cache? false})
    (println "Initialised jade to   : " content-root)))

