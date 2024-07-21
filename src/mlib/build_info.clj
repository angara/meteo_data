(ns mlib.build-info)


(defmacro get-build-info []
  {:appname   (System/getProperty "build_info.appname")
   :version   (System/getProperty "build_info.version")
   :branch    (System/getProperty "build_info.branch")
   :commit    (System/getProperty "build_info.commit")
   :timestamp (System/getProperty "build_info.timestamp")})


(def build-info
  (get-build-info))
