(ns import
  (:require
   [clojure.java.io :as io]
   [mlib.json :refer [parse-json]]
   ,))





(defn process-jsonl [fname handler]
  (with-open [rdr (clojure.java.io/reader fname)]
    (doseq [line (line-seq rdr)]
            (handler (parse-json line)))
    ))


(comment
  
  (process-jsonl "tmp/meteo_st.json" prn)
  
  ,)
