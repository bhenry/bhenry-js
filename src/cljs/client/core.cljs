(ns client.core
  (:require [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [deftemplate sel sel1]]))

(deftemplate layout [content]
  [:div#inner-content
   content])

(defn run []
  (let [l (layout [:div.soon "more <br/> coming <br/> soon..."])]
    (-> (sel1 "#content")
        (dommy/append l))))


nil
