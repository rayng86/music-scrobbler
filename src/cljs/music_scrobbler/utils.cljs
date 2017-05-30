(ns music-scrobbler.utils
    (:require [re-frame.core :as re-frame]
              [clojure.string :as string]
              [cemerick.url :as url]))

(defn keyword-this [my-map]
 (into {}
 (for [[k v] my-map]
   [(keyword k) v])))
