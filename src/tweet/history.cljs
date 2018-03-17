(ns tweet.history
  (:require
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   [secretary.core :as secretary])
  (:import goog.History))

(defonce history (History.))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto history
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token ^js event))))
    (.setEnabled true)))
