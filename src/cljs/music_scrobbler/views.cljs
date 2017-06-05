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

(defn authorize-user-panel []
  (let [api-key (re-frame/subscribe [:api-key])
        token (re-frame/subscribe [:token])
        api-sig (re-frame/subscribe [:api-sig])
        api-result (re-frame/subscribe [:api-result])
        auth-url (str "http://www.last.fm/api/auth/?"
                      "api_key=" @api-key
                      "&cb=" callback-url)]
 [:div.authentication-panel
  [:a.button.danger
   {:href auth-url} "Authorize"]
  #_[button "primary" "Login Now"
  #(do (.log js/console "button clicked")
       (re-frame/dispatch [:handler-get-session @api-sig @token]))]]))

(defn set-credentials []
  (let [username (re-frame/subscribe [:username])
        session-key (re-frame/subscribe [:session-key])]
   (cookies/set! "username" @username)
   (cookies/set! "session_key" @session-key)))

(defn user-authorization-dispatches []
 (let [api-key (re-frame/subscribe [:api-key])
       secret-key (re-frame/subscribe [:secret-key])
       fetched-token (get (keyword-this
                         (:query (url/url
                                  (-> js/window .-location .-href)))) :token)
       api-sig (js/md5 (str "api_key" @api-key
                            "method" "auth.getSession"
                            "token" fetched-token
                            @secret-key))]
  (when (empty? (cookies/get "username"))
   (when-not (empty? fetched-token)
    (do (re-frame/dispatch [:set-token fetched-token])
        (re-frame/dispatch [:set-api-sig api-sig])
        (re-frame/dispatch [:handler-get-session @api-key
                                                 api-sig
                                                 fetched-token]))))))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])
        api-key (re-frame/subscribe [:api-key])]
    (user-authorization-dispatches)
    (fn []
     (when (empty? (cookies/get "username")) (set-credentials))
     (when-not (empty? (cookies/get "username"))
      (re-frame/dispatch
       [:handler-get-recent-artists @api-key (cookies/get "username")]))
     [:div
      [:h1 @name]
      (if-not (empty? (cookies/get "username"))
       [:p "Logged in as: "
        [:a {:href (str "http://www.last.fm/user/"
                        (cookies/get "username")) } (cookies/get "username")]]
       [authorize-user-panel])
      (when-not (empty? (cookies/get "username")) [recent-tracks-component])])))
