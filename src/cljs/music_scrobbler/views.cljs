(ns music-scrobbler.views
    (:require [re-frame.core :as re-frame]
              [reagent.core :as r]
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

(defn clear-form []
  (re-frame/dispatch [:set-artist ""])
  (re-frame/dispatch [:set-track ""]))

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
        (when (true? (boolean (get-in ((keyword "@attr") v) [:nowplaying])))
         [:span.now-playing "Listening now..."])])
      coll))])

(defn recent-tracks-component []
 (let [user-recent-tracks-list (re-frame/subscribe [:user-recent-tracks-list])]
   (fn []
     [:div
      [:div
       [:h2 "Recent Scrobbled Tracks"]
       [recent-tracks-panel
        @user-recent-tracks-list :name :artist]]])))

(defn scrobbled-success [dismissed? dismiss-action]
 (let [scrobble-accepted? (re-frame/subscribe [:scrobble-accepted?])
       dismiss-btn [button "dismiss" "Dismiss" #(reset! dismiss-action)]
       artist (re-frame/subscribe [:scrobble-artist])
       track (re-frame/subscribe [:scrobble-track])]
    (cond
     (and (= @scrobble-accepted? 1) (= dismissed? false))
      [:div.scrobble-msg.success
      "Scrobbled " [:strong @artist " - " @track] " successfully."
      dismiss-btn]
     (and (= @scrobble-accepted? 0) (= dismissed? false))
      [:div.scrobble-msg.fail
      "Scrobble failed. Please try again."
      dismiss-btn]
     :else [:div ""])))

(defn manual-track-scrobble-panel []
  (let [artist-label "Artist name"
        track-label "Track name"
        msg-dismissed? (r/atom false)
        artist (re-frame/subscribe [:artist])
        track (re-frame/subscribe [:track])
        get-current-ts (Math/floor (/ (.now js/Date.) 1000))
        api-key (re-frame/subscribe [:api-key])]
 (fn []
  [:div#track-scrobble-panel
   [input-text artist-label artist artist-label
    #(do (re-frame/dispatch
          [:set-artist (-> % .-target .-value)]))]
   [input-text track-label track track-label
    #(do (re-frame/dispatch
          [:set-track (-> % .-target .-value)]))]
   [button "primary" "Manual Scrobble"
   #(do (.log js/console "button clicked")
        (generate-api-sig-fn "track.scrobble"
                             @artist
                             @track
                             get-current-ts)
        (reset! msg-dismissed? false))]
   [button "primary" "Refresh Scrobbles"
    #(do (re-frame/dispatch
          [:handler-get-recent-artists @api-key (cookies/get "username")]))]
   [button "primary" "Clear" #(clear-form)]
   [scrobbled-success @msg-dismissed? msg-dismissed?]])))

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
        session-key (re-frame/subscribe [:session-key])
        is-authenticated? (re-frame/subscribe [:is-authenticated?])]
   (when-not (and (empty? @username)
                  (empty? @session-key))
    (when (and (empty? (cookies/get "username"))
               (not=  @is-authenticated? false))
     (cookies/set! "username" @username)
     (cookies/set! "session_key" @session-key)))))

(defn clear-credentials []
  (let [username (re-frame/subscribe [:username])
        session-key (re-frame/subscribe [:session-key])]
     (cookies/remove! "username")
     (cookies/remove! "session_key")))

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

(defn logged-in-only [component]
 (let [is-authenticated? (re-frame/subscribe [:is-authenticated?])]
   (when-not (false? @is-authenticated?)
    (when-not (empty? (cookies/get "username")) component ))))

(defn login-panel []
 (if-not (empty? (cookies/get "username"))
  [:div#login-panel
   [:p.login-user "Logged in as: "
    [:a {:target "_blank"
         :href (str "http://www.last.fm/user/"
                    (cookies/get "username")) } (cookies/get "username")]]
   [button "log-out-btn" "Log Out"
                    #(do (.log js/console "Logged out")
                         (clear-credentials)
                         (re-frame/dispatch [:handler-is-authenticated? false]))]]
  ;;(get-in @get-session-result [:session :name])
  [authorize-user-panel]))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])
        api-key (re-frame/subscribe [:api-key])]
    (user-authorization-dispatches)
    (fn []
     (set-credentials)
     (logged-in-only
      (re-frame/dispatch
       [:handler-get-recent-artists @api-key
                                    (cookies/get "username")]))
     [:div
      [:h1 @name]
      (login-panel)
      (logged-in-only [manual-track-scrobble-panel])
      (logged-in-only [recent-tracks-component])])))
