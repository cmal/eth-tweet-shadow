(ns tweet.views
  (:require
   [re-frame.core :refer [dispatch subscribe] :as rf]
   [reagent.core :as r]
   [tweet.utils :as u]))

(defn- accounts-selection []
  (let [addrs   (subscribe [:db/my-addresses])
        balance (subscribe [:new-tweet/selected-address-balance])]
    [:div
     "Choose your accounts: "
     [:select
      {:name "accounts"}
      (doall
       (for [[index addr] (u/indexed @addrs)]
         ^{:key index}
         [:option
          {:value addr}
          addr]))]
     [:div.balance "Balance: " (u/eth @balance)]]))

(defn- new-tweet []
  (let [settings     (subscribe [:db/settings])
        new-tweet    (subscribe [:db/new-tweet])
        my-addresses (subscribe [:db/my-addresses])
        balance      (subscribe [:new-tweet/selected-address-balance])]
    (fn []
      [:div
       {:style {:font-size   "20px"
                :font-weight "bold"
                :margin-top  "10px"}}
       "Add New Tweet:"
       [:div
        [:input
         {:id        "name"
          :label     "name"
          :type      "text"
          :value     (:name @new-tweet)
          :on-change #(rf/dispatch [:new-tweet/update :name (u/evt-val %)])}]]
       [:div
        [:input
         {:id        "text"
          :label     "text"
          :type      "text"
          :value     (:text @new-tweet)
          :on-change #(rf/dispatch [:new-tweet/update :text (u/evt-val %)])}]]
       [:button
        {:disabled (or (empty? (:text @new-tweet))
                       (empty? (:name @new-tweet))
                       (empty? (:address @new-tweet))
                       (:sending? @new-tweet))
         :on-click #(dispatch [:new-tweet/send])}
        "Tweet!"]])))

(defn- tweets []
  (let [tweets (subscribe [:db/tweets])]
    (fn []
      [:div.tweets
       [:div
        {:style {:margin-top    "20px"
                 :border-bottom "1px solid blue"
                 :font-size     "20px"}}
        "Tweets:"]
       (doall
        (for [{:keys [tweet-key name text date author-address]} @tweets]
          ^{:key tweet-key}
          [:div.tweet
           {:style {:border-bottom "1px solid #666"}}
           [:div
            [:span.name
             {:style {:color     "blue"
                      :font-size "22px"}}
             name]
            [:span.text
             {:style {:color       "gray"
                      :font-size   "18px"
                      :margin-left "20px"}}
             text]]
           [:div
            [:span.date
             {:style {:color     "#999"
                      :font-size "14px"}}
             date]
            [:span.addr
             {:style {:color       "#999"
                      :font-size   "14px"
                      :margin-left "10px"}}
             author-address]]]))])))


(defn app []
  [:div
   [accounts-selection]
   [new-tweet]
   [tweets]])
