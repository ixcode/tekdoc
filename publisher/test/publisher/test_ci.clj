(ns publisher.test-ci
  (:use [clojure.test])
  (:require [clj-http.client :as client]))


(deftest addition
  (let [response (client/post "http://localhost:8097/trigger-ci" {:form-params {:foo "bar"}
                                                     :content-type :json})]
    (is (= 200 (:status response)))))
