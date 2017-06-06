(ns music-scrobbler.events
    (:require [re-frame.core :as re-frame]
              [clojure.string :as string]
              [music-scrobbler.db :as db]
              [ajax.core :as ajax :refer [GET POST OPTIONS]]
              [day8.re-frame.http-fx]))

(def base "http://ws.audioscrobbler.com/2.0/")

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-name
 (fn [db [_ name]]
   (assoc db :name name)))

;; For User Authentication
(re-frame/reg-event-db
 :set-token
 (fn [db [_ token]]
   (assoc db :token token)))

(re-frame/reg-event-db
 :set-api-sig
 (fn [db [_ api-sig]]
   (assoc db :api-sig api-sig)))

;; Get Session Request - Returns User and Session Key
(re-frame/reg-event-fx
 :handler-get-session
 (fn [{:keys [db]} [_ api-key api-sig token]]
   { :http-xhrio {:method         :get
                  :uri             (str base "?method="
                                        "auth.getSession"
                                        "&api_key=" api-key
                                        "&api_sig=" api-sig
                                        "&token=" token
                                        "&format=json")
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:good-get-session-result]
                  :on-failure      [:bad-get-session-result]}}))

;; Post Request for Track Scrobbles
(re-frame/reg-event-fx
 :handler-scrobble-track
 (fn [{:keys [db]} [_ api-key api-sig sk artist track ts]]
   { :http-xhrio {:method         :post
                 :uri             (str base)
                 :params          (str "format=json&"
                                       "api_key=" api-key "&"
                                       "api_sig=" api-sig "&"
                                       "artist=" (string/replace artist #" " "+") "&"
                                       "method=" "track.scrobble" "&"
                                       "sk=" sk "&"
                                       "timestamp=" ts "&"
                                       "track=" (string/replace track #" " "+"))
                 :timeout         8000
                 :format          (ajax/text-request-format)
                 :response-format (ajax/json-response-format)
                 :on-success      [:good-scrobbled-track]
                 :on-failure      [:bad-scrobbled-track]}}))

;; Get Request for Recent Tracks
(re-frame/reg-event-fx
 :handler-get-recent-artists
 (fn [{:keys [db]} [_ api-key username]]
   { :http-xhrio {:method         :get
                 :uri             (str base "?method="
                                       "user.getrecenttracks&user=" username
                                       "&api_key=" api-key
                                       "&format=json")
                 :timeout         8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:good-http-result]
                 :on-failure      [:bad-http-result]}}))

;; Set Artist and Track Names
(re-frame/reg-event-db
 :set-artist
 (fn [db [_ artist]]
   (assoc db :artist artist)))

(re-frame/reg-event-db
 :set-track
 (fn [db [_ track]]
   (assoc db :track track)))

;; On Success/Fail Response, Get Result
(re-frame/reg-event-db
  :good-get-session-result
  (fn [db [_ result]]
    (assoc db :get-session-result result)))

(re-frame/reg-event-db
  :bad-get-session-result
  (fn [db [_ result]]
    (assoc db :get-session-result result)))

(re-frame/reg-event-db
  :good-scrobbled-track
  (fn [db [_ result]]
    (assoc db :recent-scrobbled-track result)))

(re-frame/reg-event-db
  :bad-scrobbled-track
  (fn [db [_ result]]
    (assoc db :recent-scrobbled-track result)))

(re-frame/reg-event-db
  :good-http-result
  (fn [db [_ result]]
    (assoc db :api-result result)))

(re-frame/reg-event-db
  :bad-http-result
  (fn [db [_ result]]
    (assoc db :api-result result)))
