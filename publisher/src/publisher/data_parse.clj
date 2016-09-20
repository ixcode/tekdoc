(ns publisher.data-parse
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]))

;; Doesn't work if there are multiple spaces
(defn header->variable-name [input]
  (let [words (str/split input #"\P{L}+")
        w1 (str/lower-case (first words))
        r (map str/capitalize (rest words))
        processed (cons w1 r)]
    (keyword (apply str processed))))

;; eg (def d (read-tsv-file "./test/publisher/people.tsv"))
(defn read-tsv-file [filename]
   (with-open [in-file (io/reader filename)]
       (doall
        (csv/read-csv in-file :separator \tab))))

(defn process-data [data]
  (let [headers (map header->variable-name (first data))]
    (map (partial zipmap headers) (rest data))))

(defn load-data-from-tsv [tsv-filename]
  (process-data (read-tsv-file tsv-filename)))
