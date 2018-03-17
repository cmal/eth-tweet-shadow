(ns tweet.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]
            [tweet.ajax :refer [load-interceptors!]]
            [tweet.events]
            [tweet.history :as hst]
            [tweet.routes :as routes]
            ))


(enable-console-print!)

(def pages
  {:app #'app})

(defn page []
  [:div
   [(pages @(rf/subscribe [:page]))]])

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize])
  (load-interceptors!)
  (routes/mount-app-routes)
  (hst/hook-browser-navigation!)
  (mount-components))
