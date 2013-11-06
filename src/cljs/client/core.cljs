(ns client.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]])
  (:use-macros [dommy.macros :only [deftemplate sel sel1]]))

(deftemplate layout [content]
  [:div#inner-content
   content])

(def bug-states [:healthy :sick :dead])

(deftemplate bug [& [state]]
  (let [color (condp = state
                :healthy :green
                :sick :red
                :dead :black
                :green)]
    [:i.fa.fa-bug.fa-flip-vertical {:style {:color color}}]))

(deftemplate gameboard [h w]
  [:div#gameboard
   [:table #_ {:border "1px" :border-collapse true}
    (for [i (range h)]
      [:tr {:class (str i)}
       (for [j (range w)]
         [:td {:class (str j)
               :data-coords (format "[%s,%s]" i j)}])])]])

(defn change-state [[x y] val]
  (let [$cell ($ (format "[data-coords='[%s,%s]']" x y))]
    (j/html $cell val)))

(defn populate-board []
  (doseq [i (range 5)]
    (doseq [j (range 30)]
      (if (= (rand-nth bug-states) :healthy)
        (change-state [i j] (bug :healthy))))))

(defn run []
  (let [g (gameboard 20 30)
        l (layout g)]
    (-> ($ "#content")
        (j/html l))
    (populate-board)))


