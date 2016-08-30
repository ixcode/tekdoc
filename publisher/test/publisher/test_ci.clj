(ns publisher.test-ci
  (:use [clojure.test])
  (:require [clj-http.client :as client]))


(deftest trigger-ci
  (let [response (client/post "http://localhost:8087/trigger-ci" {:form-params {:foo "bar"}
                                                     :content-type :json})]
    (is (= 200 (:status response)))))
