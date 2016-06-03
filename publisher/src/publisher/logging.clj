(ns publisher.logging
  (:use [compojure.core]
        [ring.util.response]
        [markdown.core]
        [clojure.java.io])
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [clj-yaml.core :as yaml]
            [clj-jade.core :as jade]))

(defn debug [msg]
  (println "[DEBUG] " msg))
