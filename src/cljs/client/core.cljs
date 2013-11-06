(ns client.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [cljs.reader :refer [read-string]]
            )
  (:use-macros [dommy.macros :only [deftemplate sel sel1]]))

(def game-bug-count 30)
(def game-board-width 20)
(def game-board-height 10)

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

(deftemplate man [& [color]]
  [:i.fa.fa-male {:style {:color (or color :black)}}])

(deftemplate blank []
  [:div.square])

(deftemplate gameboard [h w]
  [:div#gameboard
   [:table {:border "1px" :border-collapse true}
    (for [i (range h)]
      [:tr {:class (str i)}
       (for [j (range w)]
         [:td {:class (str j)
               :data-coords (format "[%s,%s]" j i)}
          (blank)])])]])

(defn grab [[x y]]
  ($ (format "[data-coords='[%s,%s]']" x y)))

(defn put [xy val]
  (j/html (grab xy) val))

(defn find-man []
  (-> ($ ".fa-male") (j/closest "td")))

(defn coords [$cell]
  [(first (j/data $cell :coords))
   (last (j/data $cell :coords))])

(defn populate-board []
  (put [0 5] (man))
  (doseq [b (range game-bug-count)]))

(defn validate-move [[x y] dir dist]
  (condp = dir
    :north (not (neg? (- y dist)))
    :south (> game-board-height (+ y dist))
    :west (not (neg? (- x dist)))
    :east (> game-board-width (+ x dist))))

(defn move [from to]
  (let [dest (grab to)
        curr (j/html (grab from))]
    (put to curr)
    (put from (blank))))

(defn make-move [[x y] dir dist]
  (when (validate-move [x y] dir dist)
    (condp = dir
      :north (move [x y] [x (- y dist)])
      :south (move [x y] [x (+ y dist)])
      :west (move [x y] [(- x dist) y])
      :east (move [x y] [(+ x dist) y]))))

(defn read-key-input [e]
  (let [k (.-which e)]
    (condp = k
      38 :north
      40 :south
      37 :west
      39 :east
      :sit)))

(defn wire-up-keyboard-controls []
  (.keydown ($ "body")
            (fn [e]
              (let [dir (read-key-input e)]
                (when-not (= dir :sit)
                  (make-move (coords (find-man)) dir 1))))))

(defn run []
  (let [g (gameboard game-board-height game-board-width)
        l (layout g)]
    (-> ($ "#content")
        (j/html l))
    (populate-board)
    (wire-up-keyboard-controls)))


