(ns publisher.config
  (:reqiure [clj-yaml.core :as yaml]))


(def page-context {:page {:publish {:timestamp "2016-05-31"} :source "https://gitlab.some/source.html"}})

(defn expand-home [s]
  (if (.startsWith s "~")
    (clojure.string/replace-first s "~" (System/getProperty "user.home"))
    s))

(def tekdoc-config (first (yaml/parse-string (slurp (expand-home "~/.tekdoc.yml")))))

(def publish-site-config (first (yaml/parse-string (slurp (:test-site tekdoc-config)))))
(selmer.parser/set-resource-path! (:content publish-site-config))


(selmer.parser/cache-off!)
