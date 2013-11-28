(ns client.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [cljs.reader :refer [read-string]])
  (:use-macros [dommy.macros :only [deftemplate
                                    sel sel1]]))

(def game-starting-position [5 5])
(def game-bug-count 30)
(def game-board-width 11)
(def game-board-height 11)

(deftemplate layout [content]
  [:div#inner-content
   content])

(def bug-states [:healthy :sick :dead])

(defn bug-direction [dir]
  (condp = dir
    :east :fa-rotate-90
    :south :fa-rotate-180
    :west :fa-rotate-270
    :normal))

(deftemplate bug [& [state dir]]
  (let [color (condp = state
                :healthy :green
                :sick :red
                :dead :black
                :green)
        dir (or dir (rand-nth
                     [:north :east :south :west]))]
    [:i.fa.fa-bug {:style {:color color}
                   :class (bug-direction dir)}]))

(deftemplate man [& [color]]
  [:i.fa.fa-male
   {:style {:color (or color :black)}}])

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

(defn random-coords []
  [(rand-nth (range game-board-width))
   (rand-nth (range game-board-height))])

(defn board-coords []
  (for [x (range game-board-width)
        y (range game-board-height)]
    [x y]))

(defn grab [[x y]]
  ($ (format "[data-coords='[%s,%s]']" x y)))

(defn put [xy val]
  (j/html (grab xy) val))

(defn find-man []
  (-> ($ ".fa-male") (j/closest "td")))

(defn coords [$cell]
  [(first (j/data $cell :coords))
   (last (j/data $cell :coords))])

(defn calc-buffer [start]
  (let [ide identity]
    (for [pair [[dec dec][ide dec][inc dec]
                [dec ide][ide ide][inc ide]
                [dec inc][ide inc][inc inc]]]
      [((first pair) (first start))
       ((last pair) (last start))])))

(defn calc-available [excludes]
  (remove (set excludes) (board-coords)))

(defn bug-map [start]
  (let [buffer-zone (calc-buffer start)]
    (loop [bugs (range game-bug-count)
           open (calc-available buffer-zone)
           used []]
      (let [position (rand-nth open)]
        (if (next bugs)
          (recur (rest bugs)
                 (remove #{position} open)
                 (conj used position))
          (conj used position))))))

(defn populate-board [start]
  (put start (man))
  (doseq [b (bug-map start)]
    (put b (bug))))

(defn validate-move [[x y] dir dist]
  (condp = dir
    :north (not (neg? (- y dist)))
    :south (> game-board-height (+ y dist))
    :west  (not (neg? (- x dist)))
    :east  (> game-board-width (+ x dist))))

(defn move [from to]
  (let [dest (grab to)
        curr (j/html (grab from))]
    (put from (blank))
    (put to curr)))

(defn make-move [[x y] dir dist]
  (when (validate-move [x y] dir dist)
    (condp = dir
      :north (move [x y] [x (- y dist)])
      :south (move [x y] [x (+ y dist)])
      :west  (move [x y] [(- x dist) y])
      :east  (move [x y] [(+ x dist) y]))))

(defn move-toward [[x1 y1] [x2 y2]]
  (let [new-x (cond (< x1 x2) (inc x1)
                    (> x1 x2) (dec x1)
                    :equal x1)
        new-y (cond (< y1 y2) (inc y1)
                    (> y1 y2) (dec y1)
                    :equal y1)]
    (move [x1 y1] [new-x new-y])))

(defn read-key-input [e]
  (let [k (.-which e)]
    (condp = k
      38 :north
      40 :south
      37 :west
      39 :east
      :sit)))

(defn wire-up-keyboard-controls []
  (.keydown
   ($ "body")
   (fn [e]
     (let [dir (read-key-input e)]
       (when-not (= dir :sit)
         (j/prevent e)
         (make-move (coords (find-man)) dir 1))))))

(defn wire-up-mouse-controls []
  (.click
   ($ "#gameboard table")
   (fn [e]
     (let [target (-> e (.-target) $ (j/closest "td"))
           final (coords target)
           current (coords (find-man))]
       (move-toward current final)))))

(defn run []
  (let [g (gameboard game-board-height
                     game-board-width)
        l (layout g)]
    (-> ($ "#content") (j/html l))
    (populate-board game-starting-position)
    (wire-up-keyboard-controls)
    (wire-up-mouse-controls)))


