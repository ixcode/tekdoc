(defproject publisher "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.0.0"]
                 [clj-time "0.4.4"]
                 [midje "1.4.0"]
                 [clj-yaml "0.4.0"]                 
                 [clj-http "0.6.3"]
                 [compojure "1.1.1"]
                 [ring-mock "0.1.3"]
                 [markdown-clj "0.9.89"]
                 [ring/ring-json "0.1.2"]
                 [ring/ring-jetty-adapter "1.1.7" :exclusions [org.slf4j/slf4j-nop
                                                               org.slf4j/slf4j-log4j12]]
                 [dk.ative/docjure "1.6.0"]
                 [selmer "1.0.4"]
                 [clj-jade "0.1.7"]
                 [clj-time "0.11.0"]
                 [clj-http "3.1.0"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/data.csv "0.1.3"]]
  :ring {:handler publisher.server/app}
  :plugins [[lein-midje "2.0.1"]
            [lein-ring "0.7.3"]]
  :main publisher.server

  :aot [publisher.server publisher.publish publisher.ci]
  :profiles {:preview {:main publisher.server}
             :publish {:main publisher.publish}
             :ci {:main publisher.ci}}
                
  :aliases {"preview" ["with-profile" "preview" "run"]
            "publish" ["with-profile" "publish" "run"]
            "ci" ["with-profile" "ci" "run"]}
  )

