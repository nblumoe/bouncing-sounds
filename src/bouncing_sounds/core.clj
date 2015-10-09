(ns bouncing-sounds.core
  (:require [quil
             [core :as q]
             [middleware :as m]]))

(defn reset []
  {:circles []})

(defn setup []
  (q/smooth)
  (q/frame-rate 10)
  (q/color-mode :hsb 100 100 100)
  (reset))

(defn update-state [state]
  (update state :circles (fn [circles] (map #(update % :radius inc) circles))))

(defn draw-state [{:keys [circles]}]
  (q/background 0)
  (q/no-fill)
  (doseq [{:keys [x y radius hue]} circles]
    (q/stroke hue 100 100)
    (q/ellipse x y radius radius)))

(defn mouse-clicked [state {:keys [x y]}]
  (update state :circles conj {:x x :y y :radius 20 :hue (rand-int 100)}))

(q/defsketch bouncing-sounds
  :title "Bouncing Sounds"
  :size [800 800]
  :setup setup
  :update update-state
  :draw draw-state
  :mouse-clicked mouse-clicked
  :features [:keep-on-top :resizable]
  :middleware [m/fun-mode])
