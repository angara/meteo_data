(ns build
  (:require 
    [clojure.string :refer [trim-newline]]
    [clojure.java.io :as io]
    [clojure.tools.build.api :as b]
    [org.corfield.build :as bb]
  ))


(def APPLICATION 'angara/meteo_data)
(def VER_MAJOR 2)
(def VER_MINOR 0)
(def MAIN_CLASS 'meteo.main)


(def TARGET "./target")
(def RESOURCES "./resourses")
(def TARGET_RESOURCES "./target/resources")
(def VERSION_FILE "./VERSION")


(defn save-build-info [app-ver]
  (let [bi-file (io/file TARGET_RESOURCES "build-info")]
    (io/make-parents bi-file)  
    (spit bi-file app-ver)
  ))


(defn version [] 
  (format "%s.%s.%s" VER_MAJOR VER_MINOR (b/git-count-revs nil)))


(defn print-version [& _args]
  (println (version)))


(defn uberjar [& _args]
  (let [VERSION   (trim-newline (slurp VERSION_FILE))
        UBER_FILE (format "%s/%s.jar" TARGET (name APPLICATION))
        app-ver   (str (name APPLICATION) " v" VERSION)
        ]
    (save-build-info app-ver)
    (-> {:lib APPLICATION :version VERSION :main MAIN_CLASS :uber-file UBER_FILE 
         :resource-dirs [RESOURCES TARGET_RESOURCES]}
      (bb/uber))
  ))
