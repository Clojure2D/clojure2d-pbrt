(ns spectrum
  (:require [clojure2d.core :refer :all]
            [pbrt.spectrum :as s]
            [clojure2d.math :as m])
  (:import [pbrt.spectrum LineSegment]))

(def canvas (make-canvas 800 600))

(def window (show-window canvas "Spectrum visualization"))

(defn find-val-min-max
  "Find minimum and maximum values in samples"
  [samples]
  (reduce #(let [[mn mx] %1
                 s %2
                 mnv (min (:val1 s) (:val2 s) mn)
                 mxv (max (:val1 s) (:val2 s) mx)]
             [mnv mxv]) [Double/MAX_VALUE Double/MIN_VALUE] (:segments samples)))

(defn draw-spectrum
  "Draw spectrum data"
  ([canvas clr samples size [mnv mxv]]
   (set-color canvas clr 100)
   (dotimes [x (width canvas)]
     (let [v (s/interpolate-spectrum-samples samples (m/norm x 10 (- (width canvas) 10) (:x1 (:mn samples)) (:x2 (:mx samples))))]
       (ellipse canvas x (m/norm v mnv mxv (- (height canvas) 10) 10) size size)))
   canvas)
  ([canvas clr samples size] (draw-spectrum canvas clr samples size (find-val-min-max samples))))

;; draw xyz samples + resampled values

(let [cie-x-40 (s/resample-linear-spectrum s/cie-x s/cie-min-lambda s/cie-max-lambda 40)
      cie-y-40 (s/resample-linear-spectrum s/cie-y s/cie-min-lambda s/cie-max-lambda 40)
      cie-z-40 (s/resample-linear-spectrum s/cie-z s/cie-min-lambda s/cie-max-lambda 40)]

  ;; subsampling
  (with-canvas canvas
    (set-background 20 20 20)
    (draw-spectrum :white s/cie-x 5 [0.0 2.0])
    (draw-spectrum :gray s/cie-y 5 [0.0 2.0])
    (draw-spectrum :lightgrey s/cie-z 5 [0.0 2.0])
    (draw-spectrum :green (s/prepare-samples (:lambdas cie-x-40) (:values cie-x-40)) 3 [0.0 2.0])
    (draw-spectrum :lightblue (s/prepare-samples (:lambdas cie-y-40) (:values cie-y-40)) 3 [0.0 2.0])
    (draw-spectrum :red (s/prepare-samples (:lambdas cie-z-40) (:values cie-z-40)) 3 [0.0 2.0])))


;; oversampling and subsampling
(let [samples s/rgb-illum-to-spect-yellow
      oversamples (s/resample-linear-spectrum samples 380 720 99)
      subsamples (s/resample-linear-spectrum samples 380 720 19)]
  (with-canvas canvas
    (set-background 20 20 20)
    (draw-spectrum :white samples 8)
    (draw-spectrum :red (s/prepare-samples (:lambdas oversamples) (:values oversamples)) 3)
    (draw-spectrum :lightblue (s/prepare-samples (:lambdas subsamples) (:values subsamples)) 2)))

;; compare illum and refl

(with-canvas canvas
  (set-background 20 20 20)
  (draw-spectrum :blue s/rgb-refl-to-spect-blue 5)
  (draw-spectrum :lightblue s/rgb-illum-to-spect-blue 5))
