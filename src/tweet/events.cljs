(ns tweet.events
  (:require
   [clojure.string :as str]
   [tweet.db :as db]
   [day8.re-frame.http-fx]
   #_[com.smxemail.re-frame-cookie-fx]
   [ajax.core :as ajax]
   [re-frame.core :refer [reg-event-fx reg-event-db reg-sub path trim-v
                          after debug reg-fx console dispatch] :as r]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   [tweet.db :as db]
   [goog.string :as gstr]
   [goog.string.format]
   [district0x.re-frame.web3-fx]
   [tweet.utils :as u]))

(def interceptors [(when ^boolean js/goog.DEBUG debug)
                   trim-v])

(def tweet-gas-limit 1E6)

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :initialize-web3
 (fn [{:keys [db]} _]
   (console :log ":initialize")
   (merge
    {:db         db
     :http-xhrio {:method          :get
                  :uri             (gstr/format "./contracts/build/%s.abi"
                                                (get-in db [:contract :name]))
                  :timeout         6000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:contract/abi-loaded]
                  :on-failure      [:log-error]}}
    (when (:provides-web3? db)
      {:web3/call
       {:web3 (:web3 db)
        :fns  [{:fn         web3-eth/accounts
                :on-success [:blockchain/my-addresses-loaded]
                :on-error   [:log-error]}]}}))))

(reg-event-fx
 :blockchain/my-addresses-loaded
 interceptors
 (fn [{:keys [db]} [addresses]]
   (console :log ":my-addresses-loaded" addresses)
   {:db (-> db
            (assoc :my-addresses addresses)
            (assoc-in [:new-tweet :address] (first addresses)))
    :web3/get-balances
    {:web3      (:web3 db)
     :addresses (for [addr addresses]
                  {:address    addr
                   :id         (str "balance-" addr)
                   :watch?     true
                   :on-success [:blockchain/balance-loaded addr]
                   :on-error   [:log-error]})}}))

(reg-event-fx
 :contract/abi-loaded
 interceptors
 (fn [{:keys [db]} [abi]]
   (console :log ":abi-loaded")
   (let [web3              (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (:address (:contract db)))]
     (console :log contract-instance)
     {:db (assoc-in db [:contract :instance] contract-instance)

      :web3/watch-events
      {:events [{:instance          contract-instance
                 :id                "on-tweet-added"
                 :event             :on-tweet-added
                 :event-filter-opts {}
                 :block-filter-opts {:from-block 0}
                 :on-success        [:contract/on-tweet-loaded]
                 :on-error          [:log-error]}]}

      :web3/call
      {:web3 (:web3 db)
       :fns  [{:instance   contract-instance
               :fn         :get-settings
               :on-success [:contract/settings-loaded]
               :on-error   [:log-error]}]}})))

(reg-event-db
 :contract/on-tweet-loaded
 interceptors
 (fn [db [tweet]]
   (console :log ":on-tweet-loaded")
   (console :log (merge (select-keys tweet [:author-address :text :name])
                        {:date      (u/big-number->date-time (:date tweet))
                         :tweet-key (.toNumber (:tweet-key tweet))}))
   (update db :tweets conj (merge (select-keys tweet [:author-address :text :name])
                                  {:date      (u/format-date (u/big-number->date-time (:date tweet)))
                                   :tweet-key (.toNumber (:tweet-key tweet))}))))


(reg-event-db
 :contract/settings-loaded
 interceptors
 (fn [db [[max-name-length max-tweet-length]]]
   (console :log ":settings-loaded")
   (assoc db :settings {:max-name-length  (.toNumber max-name-length)
                        :max-tweet-length (.toNumber max-tweet-length)})))

(reg-event-db
 :blockchain/balance-loaded
 interceptors
 (fn [db [address balance]]
   (console :log ":balance-loaded" balance address)
   (assoc-in db [:accounts address :balance] balance)))

(reg-event-db
 :new-tweet/update
 interceptors
 (fn [db [key value]]
   (console :log ":update")
   (assoc-in db [:new-tweet key] value)))



(reg-event-fx
 :new-tweet/send
 interceptors
 (fn [{:keys [db]} []]
   (console :log ":send")
   (let [{:keys [name text address]} (:new-tweet db)]
     #_(prn
        (doseq [{:keys [:fn :instance :args :tx-opts :on-success :on-error :on-tx-hash :on-tx-hash-error
                        :on-tx-receipt :on-tx-error :on-tx-success]}
                (remove nil? [{:instance         (:instance (:contract db))
                               :fn               :add-tweet
                               :args             [name text]
                               :tx-opts          {:from address
                                                  :gas  tweet-gas-limit}
                               :on-tx-hash       [:new-tweet/confirmed]
                               :on-tx-hash-error [:log-error]
                               :on-tx-success    [:new-tweet/success]
                               :on-tx-error      [:log-error]
                               :on-tx-receipt    [:new-tweet/transaction-receipt-loaded]}])]
          (if instance
            (if tx-opts
              (;;apply web3-eth/contract-call
               prn "if if"
               (concat [instance fn]
                       args
                       [tx-opts]
                       [{:web3             (:web3 db)
                         :on-tx-hash       on-tx-hash
                         :on-tx-hash-error on-tx-hash-error
                         :on-tx-receipt    on-tx-receipt
                         :on-tx-success    on-tx-success
                         :on-tx-error      on-tx-error}]))
              (;;apply web3-eth/contract-call
               prn "inner if"
               #_(concat [instance fn]
                         args
                         [(dispach-fn on-success on-error)])))
            (;;apply fn
             prn "outter if"
             #_(concat [(:web3 db)] args [(dispach-fn on-success on-error)])))))
     {:web3/call
      {:web3 (:web3 db)
       :fns  [{:instance         (:instance (:contract db))
               :fn               :add-tweet
               :args             [name text]
               :tx-opts          {:from address
                                  :gas  tweet-gas-limit}
               :on-tx-hash       [:new-tweet/confirmed]
               :on-tx-hash-error [:log-error]
               :on-tx-success    [:new-tweet/success]
               :on-tx-error      [:log-error]
               :on-tx-receipt    [:new-tweet/transaction-receipt-loaded]}]}}
     #_{})))

(reg-event-db
 :new-tweet/confirmed
 interceptors
 (fn [db [transaction-hash]]
   (console :log ":confirmed" transaction-hash)
   (assoc-in db [:new-tweet :sending?] true)))

(reg-event-db
 :new-tweet/success
 interceptors
 (fn [db [params]]
   (console :log ":new-tweet/success" params)
   db))

(reg-event-db
 :new-tweet/transaction-receipt-loaded
 interceptors
 (fn [db [{:keys [gas-used] :as transaction-receipt}]]
   (console :log ":trasaction-receipt-loaded")
   (console :log transaction-receipt)
   (when (= gas-used tweet-gas-limit)
     (console :error "All gas used"))
   (assoc-in db [:new-tweet :sending?] false)))

(reg-event-fx
 :contract/fetch-compiled-code
 interceptors
 (fn [{:keys [db]} [on-success]]
   (console :log ":fetch-compiled-code")
   {:http-xhrio {:method          :get
                 :uri             (gstr/format "/contracts/build/%s.json"
                                               (get-in db [:contract :name]))
                 :timeout         6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      on-success
                 :on-failure      [:log-error]}}))

(reg-event-fx
 :contract/deploy-compiled-code
 interceptors
 (fn [{:keys [db]} [contracts]]
   (console :log ":deploy-compiled-code")
   (let [{:keys [abi bin]} (get-in contracts [:contracts (keyword (:name (:contract db)))])]
     {:web3/call
      {:web3 (:web3 db)
       :fns  [{:fn         web3-eth/contract-new
               :args       [(js/JSON.parse abi) {:gas  4500000
                                                 :data bin
                                                 :from (first (:my-addresses db))}]
               :on-success [:contract/deployed]
               :on-error   [:log-error]}]}})))

(reg-event-fx
 :blockchain/unlock-account
 interceptors
 (fn [{:keys [db]} [address password]]
   (console :log ":unlock-account")
   {:web3/call
    {:web3 (:web3 db)
     :fns  [{:fn         web3-personal/unlock-account
             :args       [address password 999999]
             :on-success [:blockchain/account-unlocked]
             :on-error   [:log-error]}]}}))

(reg-event-fx
 :blockchain/account-unlocked
 interceptors
 (fn [{:keys [db]}]
   (console :log ":account-unlocked")
   (console :log "Account was unlocked.")
   {}))

(reg-event-fx
 :contract/deployed
 interceptors
 (fn [_ [contract-instance]]
   (console :log ":deployed")
   (when-let [address (aget contract-instance "address")]
     (console :log "Contract deployed at" address))))

(reg-event-fx
 :log-error
 interceptors
 (fn [_ [err]]
   (console :log ":log-error")
   (console :error err)
   {}))


(reg-sub
 :db/my-addresses
 (fn [db]
   (:my-addresses db)))

(reg-sub
 :db/tweets
 (fn [db]
   (sort-by :date #(compare %2 %1) (:tweets db))))

(reg-sub
 :db/new-tweet
 (fn [db]
   (:new-tweet db)))

(reg-sub
 :db/settings
 (fn [db]
   (:settings db)))

(reg-sub
 :new-tweet/selected-address-balance
 (fn [db]
   (get-in db [:accounts (:address (:new-tweet db)) :balance])))
