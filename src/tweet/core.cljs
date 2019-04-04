(ns tweet.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]
            [tweet.ajax :refer [load-interceptors!]]
            [tweet.events]
            [tweet.history :as hst]
            [tweet.routes :as routes]
            [tweet.views :as views]
            ))


(enable-console-print!)

#_(def pages
    {:app #'views/app})

(defn page []
  [:div
   #_[(pages @(rf/subscribe [:page]))]
   [#'views/app]])

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:initialize-web3])
  (load-interceptors!)
  #_(routes/mount-app-routes)
  (hst/hook-browser-navigation!)
  (mount-components))
