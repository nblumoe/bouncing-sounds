(ns bouncing-sounds.core
  (:require [quil
             [core :as q]
             [middleware :as m]]))

;; for whatever reason, we need to create a first sketch before
;; starting overtone, otherwise an exception is thrown from time to
;; time
(q/defsketch foo :size [10 10])
(quil.applet/applet-close foo)
(use 'overtone.live)

(defn reset-state []
  {:circles []})

(defn setup []
  (q/smooth)
  (q/frame-rate 10)
  (q/no-fill)
  (q/color-mode :hsb 100 100 100)
  (reset-state))

;; TODO handle circles inside each other
(defn colliding? [{x1 :x y1 :y r1 :radius}
                  {x2 :x y2 :y r2 :radius}]
  (when-not (= 0 (- x1 x2) (- y1 y2))
    (< (q/dist x1 y1 x2 y2)
       (+ r1 r2))))

(defn collides-with-any? [circle other-circles]
  (some #{true} (map #(colliding? circle %) other-circles)))

(defn small-circle? [circle]
  (< (:radius circle) 5))

(defn kill-small [circles]
  (remove small-circle? circles))

(defn reverse-growth [circle]
  (update circle :growth -))

(defn reverse-small [circles]
  (map #(if (small-circle? %)
          (reverse-growth %)
          %) circles))

(definst bounce-inst [freq 220 release 0.3]
  (let [env (env-gen (perc 0.1 release) :action FREE)]
    (* env (saw freq))))

(defn circle->freq [{:keys [radius]}]
  (let [min-freq 50]
    (+ min-freq (* min-freq (Math/floor (/ radius (* 100 (/ (inc (q/mouse-x)) (q/width)))))))))

(defn collide-circles [circles]
  (map #(if (collides-with-any? % circles)
          (do
            (bounce-inst (circle->freq %) (* 1 (/ (:hue %) 100)))
            (reverse-growth %))
          %) circles))

(defn grow-circles [circles]
  (map #(update % :radius (partial + (* 4 (/ (q/mouse-y) (q/height))  (:growth %)))) circles))

(defn update-state [state]
  (-> state
      (update :circles reverse-small)
      (update :circles collide-circles)
      (update :circles grow-circles))
  #_
  (reset-state))

(defn draw-state [{:keys [circles]}]
  (q/background 0)

  (doseq [{:keys [x y radius hue]} circles]
    (q/stroke hue 100 100)
    (q/ellipse x y (* 2 radius) (* 2 radius))))

(defn mouse-clicked [state {:keys [x y]}]
  (update state :circles conj {:x x :y y :radius 20 :hue (rand-int 100) :growth (inc (rand-int 3))}))

(defn start []
  (q/defsketch bouncing-sounds
    :title "Bouncing Sounds"
    :size [800 800]
    :setup setup
    :update update-state
    :draw draw-state
    :mouse-clicked mouse-clicked
    :features [:keep-on-top :resizable]
    :middleware [m/fun-mode]))
