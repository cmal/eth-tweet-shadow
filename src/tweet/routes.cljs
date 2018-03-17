(ns tweet.routes
  (:require [secretary.core :as secretary]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(secretary/set-config! :prefix "#")

(defn mount-app-routes
  []
  (secretary/defroute
    app-path
    "/app"
    [query-params]
    (let [{:keys []} query-params]
      )))
