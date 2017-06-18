(ns hard.math
  (:refer-clojure :exclude [update])
  (:use arcadia.linear)
  (:import [UnityEngine Debug Gizmos Vector3 Color Time Mathf]))


;;catmull rom by nasser
(defn spline
  ([t ps]
   (let [i (int t)
         t (- t i)]
     (spline t
             (nth ps i)
             (nth ps (+ i 1))
             (nth ps (+ i 2))
             (nth ps (+ i 3)))))
  ([t p0 p1 p2 p3]
   (let [a (v3* (v3* p1 2) 0.5)
         b (v3* (v3- p2 p0) 0.5)
         c (v3* (v3+ (v3* p0 2)
                     (v3* p1 -5)
                     (v3* p2 4)
                     (v3* p3 -1))
                0.5)
         d (v3* (v3+ (v3* p0 -1)
                     (v3* p1 3)
                     (v3* p2 -3)
                     p3)
                0.5)]
     (v3+ a
          (v3* b t)
          (v3* c (Mathf/Pow t 2))
          (v3* d (Mathf/Pow t 3))))))