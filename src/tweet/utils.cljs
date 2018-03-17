(ns tweet.utils
  (:require [cljs-time.coerce :refer [to-date-time to-long to-local-date-time]]
            [cljs-time.core :refer [date-time to-default-time-zone]]
            [cljs-time.format :as tf]
            [cljs-web3.core :as web3]))

(defn truncate
  "Truncate a string with suffix (ellipsis by default) if it is
   longer than specified length."
  ([string length]
   (truncate string length "..."))
  ([string length suffix]
   (let [string-len (count string)
         suffix-len (count suffix)]
     (if (<= string-len length)
       string
       (str (subs string 0 (- length suffix-len)) suffix)))))

#_(defn evt-val [e]
    (aget e "target" "value"))

(defn evt-val [e]
  (-> e .-target .-value))

(defn big-number->date-time [big-num]
  (to-date-time (* (.toNumber big-num) 1000)))

(defn eth [big-num]
  (str (web3/from-wei big-num :ether) " ETH"))

(defn format-date [date]
  (tf/unparse-local (tf/formatters :rfc822) (to-default-time-zone (to-date-time date))))
