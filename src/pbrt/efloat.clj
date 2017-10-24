(ns pbrt.efloat
  (:require [clojure2d.math :as m]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(m/use-primitive-operators)

(defn gamma
  "Gamma fn defined in pbrt."
  ^double [^double n]
  (let [v (* n m/MACHINE-EPSILON)]
    (/ v (- 1.0 v))))

(def ^:const ^double GAMMA3 (gamma 3.0))

(defprotocol EFloatProto
  (sub [e] [e e1])
  (add [e e1])
  (mult [e e1])
  (div [e e1])
  (absolute-error [e])
  (get-val [e])
  (abs [e])
  (applyf [e f]))

(deftype EFloat [^double v ^double low ^double high]
  Object
  (equals [_ e] (== v (.v ^EFloat e)))
  (toString [_] (str v " [" low ", " high "]"))
  EFloatProto
  (get-val [_] v)
  (absolute-error [_] (- high low))
  (add [_ e]
    (let [^EFloat e e]
      (EFloat. (+ v (.v e))
               (m/next-float-down (+ low (.low e)))
               (m/next-float-up (+ high (.high e))))))
  (sub [_ e]
    (let [^EFloat e e]
      (EFloat. (- v (.v e))
               (m/next-float-down (- low (.high e)))
               (m/next-float-up (- high (.low e))))))
  (sub [_]
    (EFloat. (- v) (- high) (- low)))
  (mult [_ e]
    (let [^EFloat e e
          p0 (* low (.low e))
          p1 (* high (.low e))
          p2 (* low (.high e))
          p3 (* high (.high e))]
      (EFloat. (* v (.v e))
               (m/next-float-down (min p0 p1 p2 p3))
               (m/next-float-up (max p0 p1 p2 p3)))))
  (div [_ e]
    (let [^EFloat e e
          p0 (/ low (.low e))
          p1 (/ high (.low e))
          p2 (/ low (.high e))
          p3 (/ high (.high e))]
      (EFloat. (/ v (.v e))
               (m/next-float-down (min p0 p1 p2 p3))
               (m/next-float-up (max p0 p1 p2 p3)))))
  (abs [e]
    (cond
      (>= low 0.0) e
      (<= high 0.0) (sub e)
      :else (EFloat. (m/abs v) 0.0 (max (- low) high))))
  (applyf [_ f]
    (EFloat. (f v) (m/next-float-down (f low)) (m/next-float-up (f high)))))

(defn efloat
  "Create efloat value"
  ([^double v ^double err]
   (if (zero? err)
     (EFloat. v v v)
     (EFloat. v (m/next-float-down (- v err)) (m/next-float-up (+ v err)))))
  ([v] (efloat v 0.0)))

(def efloat-neg-half (efloat -0.5))

(defn quadratic
  "Find quadratic discriminant"
  [^EFloat a ^EFloat b ^EFloat c]
  (let [discrim (- (* (.v b) (.v b)) (* 4.0 (.v a) (.v c)))]
    (if (neg? discrim)
      nil
      (let [root-discrim (m/sqrt discrim)
            efloat-root-discrim (efloat root-discrim (* m/MACHINE-EPSILON root-discrim))
            q (if (neg? (.v b))
                (mult efloat-neg-half (sub b efloat-root-discrim))
                (mult efloat-neg-half (add b efloat-root-discrim)))
            ^EFloat t0 (div q a)
            ^EFloat t1 (div c q)]
        (if (> (.v t0) (.v t1))
          [t1 t0]
          [t0 t1])))))
