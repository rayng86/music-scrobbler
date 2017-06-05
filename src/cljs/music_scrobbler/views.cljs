(ns music-scrobbler.views
    (:require [re-frame.core :as re-frame]
              [reagent.cookies :as cookies]
              [clojure.string :as string]
              [cemerick.url :as url]
              [music-scrobbler.utils :refer [keyword-this]]
              [music-scrobbler.generic.forms :refer [input-text button]]))

(def callback-url "http://localhost:3669")

(defn generate-api-sig-fn [method artist track ts]
 (let [api-key (re-frame/subscribe [:api-key])
       secret-key (re-frame/subscribe [:secret-key])
       sk (cookies/get "session_key")
       api-sig (js/md5 (str "api_key" @api-key
                            "artist" artist
                            "method" method
                            "sk" sk
                            "timestamp" ts
                            "track" track
                            @secret-key))]
   (do #_(.log js/console api-sig)
       #_(.log js/console (str "api_key" @api-key
                             "artist" artist
                             "method" method
                             "sk" sk
                             "timestamp" ts
                             "track" track
                             @secret-key))
       (re-frame/dispatch
        [:handler-scrobble-track @api-key
                                 api-sig
                                 sk
                                 artist
                                 track
                                 (str ts)]))))

(defn recent-tracks-panel
  [coll track artist]
  [:div#recent-tracks-panel
   (doall
    (map-indexed
     (fn [idx v]
       ^{:key idx}
       [:div.track
        [:label "Artist: "]
        [:span (get-in (artist v) [:#text])]
        [:label "Track: "]
        [:span (track v)]
        [:hr]])
      coll))])

(defn recent-tracks-component []
 (let [user-recent-tracks-list (re-frame/subscribe [:user-recent-tracks-list])]
   (fn []
     [:div
      [:div
       [:h2 "Recent Scrobbled Tracks"]
       [recent-tracks-panel
        @user-recent-tracks-list :name :artist]]])))

(defn set-credentials []
  (let [username (re-frame/subscribe [:username])
        session-key (re-frame/subscribe [:session-key])]
   (cookies/set! "username" @username)
   (cookies/set! "session_key" @session-key)))


(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div "Hello from " @name])))
