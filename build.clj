(ns build
  (:import
   [java.time LocalDateTime]
   [java.time.format DateTimeFormatter])
  (:require 
    [clojure.tools.build.api :as b]
  ))


(def APP_NAME   (System/getenv "APP_NAME"))
(def VER_MAJOR  (System/getenv "VER_MAJOR"))
(def VER_MINOR  (System/getenv "VER_MINOR"))
(def MAIN_CLASS (System/getenv "MAIN_CLASS"))


(def JAVA_SRC         "./java")
(def TARGET           "./target")
(def CLASS_DIR        "./target/classes")
(def RESOURCES        "./resourses")
(def TARGET_RESOURCES "./target/resources")


(defn clean [_]
  (b/delete {:path TARGET}))


;; https://clojure.org/guides/tools_build
;;
(defn javac [{basis :basis}]
  (b/javac {:src-dirs [JAVA_SRC]
            :class-dir CLASS_DIR
            :basis (or basis (b/create-basis {:project "deps.edn"}))
            ; :javac-opts ["-source" "8" "-target" "8"]
           }))


(defn uberjar [_]
  (let [appname   APP_NAME
        version   (format "%s.%s.%s" VER_MAJOR VER_MINOR (b/git-count-revs nil))
        branch    (b/git-process {:git-args "branch --show-current"})
        commit    (b/git-process {:git-args "rev-parse --short HEAD"})
        timestamp (.format (LocalDateTime/now) DateTimeFormatter/ISO_LOCAL_DATE_TIME)
        UBER_FILE (format "%s/%s.jar" TARGET APP_NAME)
        basis     (b/create-basis {:project "deps.edn"})]

    (println "building:" appname version branch commit)

    (javac {:basis basis})

    (b/copy-dir {:src-dirs ["src" RESOURCES TARGET_RESOURCES]
                 :target-dir CLASS_DIR})

    (b/compile-clj {:basis basis
                    :src-dirs ["src"]
                    :class-dir CLASS_DIR
                    :java-opts [(str "-Dbuild_info.appname="   appname)
                                (str "-Dbuild_info.version="   version)
                                (str "-Dbuild_info.branch="    branch)
                                (str "-Dbuild_info.commit="    commit)
                                (str "-Dbuild_info.timestamp=" timestamp)]
                    })

    (b/uber {:class-dir CLASS_DIR
             :uber-file UBER_FILE
             :basis basis
             :main MAIN_CLASS})))
;;
