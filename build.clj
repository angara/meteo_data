(ns build
  (:import
   [java.time LocalDateTime]
   [java.time.format DateTimeFormatter])
  (:require 
    [clojure.java.io :as io]
    [clojure.tools.build.api :as b]
  ))


(def APP_NAME   (System/getenv "APP_NAME"))
(def VER_MAJOR  (System/getenv "VER_MAJOR"))
(def VER_MINOR  (System/getenv "VER_MINOR"))
(def MAIN_CLASS (System/getenv "MAIN_CLASS"))


(def JAVA_SRC         "./java")
(def RESOURCES        "./resourses")
(def TARGET           "./target")
(def CLASS_DIR        "./target/classes")
(def TARGET_RESOURCES "./target/resources")

(def BUILD_INFO "build-info.edn")
(def JAR_NAME (str APP_NAME ".jar"))


(defn iso-now ^String []
  (.format (LocalDateTime/now) DateTimeFormatter/ISO_LOCAL_DATE_TIME))


(defn clean [_]
  (b/delete {:path TARGET}))


(defn version [_]
  (format "%s.%s.%s" VER_MAJOR VER_MINOR (b/git-count-revs nil)))


(defn build-info [_]
  {:appname APP_NAME
   :version (version nil)
   :branch (b/git-process {:git-args "branch --show-current"})
   :commit (b/git-process {:git-args "rev-parse --short HEAD"})
   :timestamp (iso-now)})


(defn write-build-info [build-info]
  (let [out-file (io/file CLASS_DIR BUILD_INFO)]
    (io/make-parents out-file)
    (spit out-file (pr-str build-info))))


;; https://clojure.org/guides/tools_build
;;
(defn javac [{basis :basis}]
  (b/javac {:src-dirs [JAVA_SRC]
            :class-dir CLASS_DIR
            :basis (or basis (b/create-basis {:project "deps.edn"}))
            :javac-opts ["-proc:none"  "--release" "21"]
           }))


(defn uberjar [_]
  (let [build-info (build-info nil)
        uber-file  (io/file TARGET JAR_NAME)
        basis      (b/create-basis {:project "deps.edn"})]

    (println "build:" build-info)
    (write-build-info build-info)

    (println "compile Java")
    (javac {:basis basis}) 

    (b/copy-dir {:src-dirs ["src" RESOURCES TARGET_RESOURCES]
                 :target-dir CLASS_DIR})

    (println "compile Clojure")
    (b/compile-clj {:basis basis
                    :src-dirs ["src"]
                    :class-dir CLASS_DIR
                    })
    
    (println "pack uberjar")
    (b/uber {:class-dir CLASS_DIR
             :uber-file (str uber-file)
             :basis basis
             :main MAIN_CLASS})
    
    (println "complete:" (str uber-file))
    ,))
