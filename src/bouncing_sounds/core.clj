(ns bouncing-sounds.core
  (:require [quil
             [core :as q]
             [middleware :as m]]))

(defn reset-state []
  {:circles []})

(defn setup []
  (q/smooth)
  (q/frame-rate 10)
  (q/no-fill)
  (q/color-mode :hsb 100 100 100)
  (reset-state))

(defn colliding? [{x1 :x y1 :y r1 :radius}
                  {x2 :x y2 :y r2 :radius}]
  (when-not (= 0 (- x1 x2) (- y1 y2))
    (< (q/dist x1 y1 x2 y2)
       (+ r1 r2))))

(defn collides-with-any? [circle other-circles]
  (some #{true} (map #(colliding? circle %) other-circles)))

(defn kill-small [circles]
  (remove #(< (:radius %) 5) circles))

(defn collide-circles [circles]
  (map #(if (collides-with-any? % circles)
          (assoc % :grow? false)
          %)
       circles))

(defn grow-circles [circles]
  (map #(update % :radius (if (:grow? %) inc dec)) circles))

(defn update-state [state]
  (-> state
      (update :circles kill-small)
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
  (update state :circles conj {:x x :y y :radius 20 :hue (rand-int 100) :grow? true}))

#_
(q/defsketch bouncing-sounds
  :title "Bouncing Sounds"
  :size [800 800]
  :setup setup
  :update update-state
  :draw draw-state
  :mouse-clicked mouse-clicked
  :features [:keep-on-top :resizable]
  :middleware [m/fun-mode])
