(ns pbrt.spectrum-test
  (:require [pbrt.spectrum :refer :all]
            [expectations :refer :all]
            [clojure2d.math :as m]))

;; blackbody

(defn err
  "Relative error"
  [^double v ^double r]
  (/ (m/abs (- v r)) r))

(expect (approximately 0.0) (err (first (blackbody (double-array [483]) 6000)) 3.1849e13))
(expect (approximately 0.0) (err (first (blackbody (double-array [600]) 6000)) 2.86772e13))
(expect (approximately 0.0) (err (first (blackbody (double-array [500]) 3700)) 1.59845e12))
(expect (approximately 0.0) (err (first (blackbody (double-array [600]) 4500)) 7.46497e12))

(defn wiens-displacement-law
  ""
  [^double t]
  (let [lambda-max (* 1.0e9 (/ 2.8977721e-3 t))
        ^doubles res (blackbody (double-array [(* 0.999 lambda-max) lambda-max (* 1.001 lambda-max)]) t)]
    (and (< (aget res 0)
            (aget res 1))
         (> (aget res 1)
            (aget res 2)))))

(expect true (every? true? (map #(wiens-displacement-law %) (range 2700 6000 100))))

;; segment

(def s1 (make-segment 1 2 3 4))

;; only segment, consts on edges
(expect (approximately 4.0) (get-val s1 3.5))
(expect (approximately 3.0) (get-val s1 0.0))
(expect (approximately 3.5) (get-val s1 1.5))
;; line from segment
(expect (approximately 5.5) (get-val s1 3.5 true))
(expect (approximately 2.0) (get-val s1 0.0 true))

(expect nil? (intersect s1 0 0.9))
(expect 1.0 (:x1 (intersect s1 -1 2.5)))
(expect 2.0 (:x2 (intersect s1 -1 2.5)))
(expect 1.2 (:x1 (intersect s1 1.2 1.3)))
(expect 1.3 (:x2 (intersect s1 1.2 1.3)))

;; prepare samples - check

(expect 380.0 (:x1 (:mn rgb-refl-to-spect-white)))
(expect 720.0 (:x2 (:mx rgb-refl-to-spect-white)))
(expect 1.0618958571272863e+00 (:val1 (:mn rgb-refl-to-spect-white)))
(expect 1.0606565756823634e+00 (:val2 (:mx rgb-refl-to-spect-white)))
(expect 31 (count (:segments rgb-refl-to-spect-white)))

;; average spectrum samples

(def lambdas [1 2 3 4 5 6])
(def values [0.4 0.3 0.5 1.0 2.0 -1.0])
(def samples (prepare-samples lambdas values))

(expect (approximately 0.41) (average-spectrum-samples samples 2.5 2.6))
(expect (approximately 0.4) (average-spectrum-samples samples 2.0 3.0))
(expect (approximately 0.7) (average-spectrum-samples samples 1.0 6.0))
(expect (approximately -0.914141) (average-spectrum-samples samples 1.0 100.0))
(expect (approximately 0.4) (average-spectrum-samples samples -100.0 1.0))
(expect (approximately 1.0125) (average-spectrum-samples samples 3.9 4.1))

;; interpolate value from samples

(expect -1.0 (interpolate-spectrum-samples samples 10))
(expect 0.4 (interpolate-spectrum-samples samples -1))
(expect 1.5 (interpolate-spectrum-samples samples 4.5))
(expect 0.3 (interpolate-spectrum-samples samples 2))

;; resample spectrum

(expect (approximately 0.48) (last (:values (resample-linear-spectrum samples 2.5 2.9 5))))
(expect 100 (count (:values (resample-linear-spectrum samples 1 6 100))))

(expect [1.5 5.0 8.5] (:values (resample-linear-spectrum (prepare-samples [0 1 2 3 4] [1 3 5 7 9]) 0 4 3)))
(expect [2.0 4.0 6.0 8.0] (:values (resample-linear-spectrum (prepare-samples [0 1 2 3 4] [1 3 5 7 9]) 0.5 3.5 4)))

;; treat corner cases as segment extensions (not constants)
(expect [1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0] (:values (resample-linear-spectrum (prepare-samples [0 1 2 3 4] [1 3 5 7 9]) 0 4 9 true)))
