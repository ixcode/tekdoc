(defproject publisher "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.0.0"]
                 [clj-time "0.4.4"]
                 [midje "1.4.0"]
                 [clj-yaml "0.4.0"]                 
                 [clj-http "0.6.3"]
                 [compojure "1.1.1"]
                 [ring-mock "0.1.3"]
                 [markdown-clj "0.9.65"]
                 [ring/ring-json "0.1.2"]
                 [markdown-clj "0.9.66"]
                 [ring/ring-jetty-adapter "1.1.7" :exclusions [org.slf4j/slf4j-nop
                                                               org.slf4j/slf4j-log4j12]]
                 [endjinn "0.1.0-SNAPSHOT"]
                 [dk.ative/docjure "1.6.0"]
                 [selmer "1.0.4"]
                 [clj-jade "0.1.7"]]
  :ring {:handler publisher.server/app}
  :plugins [[lein-midje "2.0.1"]
            [lein-ring "0.7.3"]]
  :main publisher.server
)

