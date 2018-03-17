(ns tweet.db
  (:require [cljs-web3.core :as web3]))

(def default-db
  {:tweets         []
   :settings       {}
   :my-addresses   []
   :accounts       {}
   :new-tweet      {:text     ""
                    :name     ""
                    :address  nil
                    :sending? false}
   :web3           (or (aget js/window "web3")
                       (if goog.DEBUG
                         (web3/create-web3 "http://localhost:8545/")
                         (web3/create-web3 "https://morden.infura.io/metamask")))
   :provides-web3? (or (aget js/window "web3") goog.DEBUG)
   :contract       {:name     "SimpleTwitter"
                    :abi      nil
                    :bin      nil
                    :instance nil
                    :address  "0xF6Fa8B221C36C9C6c7c4360491D1829931b7cB2B"}})
