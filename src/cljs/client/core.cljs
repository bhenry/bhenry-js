(ns client.core
  (:require [dommy.core :as dommy]
            [jayq.core
 :as j :refer [$]])
  (:use-macros [dommy.macros :only [deftemplate sel sel1]]))

(deftemplate layout [content]
  [:div#inner-content
   content])

(defn run []
  (let [l (layout [:div#soon [:p "more"] [:p "coming"] [:p "soon..."]])]
    (-> ($ "#content")
        (j/html l))))


nil
