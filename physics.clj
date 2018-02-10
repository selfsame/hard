(ns hard.physics
  (:use arcadia.linear)
  (:import
    [UnityEngine Mathf Vector3 Ray Physics RaycastHit Physics2D LayerMask]))

(defn gob? [x] (instance? UnityEngine.GameObject x))

(defn mask [n]
  (int (bit-shift-left 1 (if (string? n) (LayerMask/NameToLayer n) n))))

'(LayerMask/GetMask (into-array System.String ["player"]))

(defn set-mask! [o n]
  (let [m (int (if (string? n) (LayerMask/NameToLayer n) n))
        ^UnityEngine.Transform|[]| ts (.GetComponentsInChildren o UnityEngine.Transform)]
    (run! 
      #(set! (.layer (.gameObject %)) m) ts)))

(def ^:private hit-buff (make-array UnityEngine.RaycastHit 200))

(defn ^System.Single raycast-non-alloc [
  ^Vector3 origin 
  ^Vector3 direction 
  ^|UnityEngine.RaycastHit[]| results
  ^System.Double len]
  (Physics/RaycastNonAlloc origin direction results len))


(defn- hit*
  {:inline-arities #{2 3}
   :inline 
   (fn
     ([a b]   `(raycast-non-alloc ~a ~b hit-buff Mathf/Infinity))
     ([a b c] `(raycast-non-alloc ~a ~b hit-buff ~c)))}
  ([^Vector3 a ^Vector3 b]
    (raycast-non-alloc a b hit-buff Mathf/Infinity))
  ([^Vector3 a ^Vector3 b ^System.Double c]
    (raycast-non-alloc a b hit-buff c)))

(def zero-long (long 0))

(defn hit 
  ([^Vector3 a ^Vector3 b]
   (if (> (hit* a b) 0) (aget hit-buff 0)))
  ([^Vector3 a ^Vector3 b ^System.Double c]
   (if (> (hit* a b c) 0) (aget hit-buff 0)))
  ([^Vector3 a ^Vector3 b ^System.Double c d]
   (if (> (Physics/RaycastNonAlloc a b hit-buff c d) zero-long) (aget hit-buff zero-long))))

(defn hits 
  ([^Vector3 a ^Vector3 b]
    (map #(aget hit-buff %) (range (hit* a b))))
  ([^Vector3 a ^Vector3 b ^System.Double c]
    (map #(aget hit-buff %) (range (hit* a b c))))
  ([^Vector3 a ^Vector3 b ^System.Double c d]
    (map #(aget hit-buff %) (range (Physics/RaycastNonAlloc a b hit-buff c d)))))



(defn gravity [] (Physics/gravity))

(defn gravity! [v3] (set! (Physics/gravity) v3))

(defn rigidbody? [o] (instance? UnityEngine.Rigidbody o))

(defn ^UnityEngine.Rigidbody ->rigidbody [^UnityEngine.GameObject o]
  (.GetComponent o UnityEngine.Rigidbody))

(defn ->rigidbody2d [v]
  (if-let [o (.gameObject v)] (.GetComponent o UnityEngine.Rigidbody2D) nil))

(defn force! [body x y z] (.AddRelativeForce body x y z))
(defn global-force! [body x y z] (.AddForce body x y z))
(defn torque! [body x y z] (.AddRelativeTorque body x y z))
(defn global-torque! [body x y z] (.AddTorque body x y z))
(defn kinematic! [go v] (set! (.isKinematic (->rigidbody go)) v))

(defn force2d!         [body x y] (.AddRelativeForce body (v2 x y)))
(defn global-force2d!  [^UnityEngine.Rigidbody2D body x y] (.AddForce body (v2 x y)))
(defn torque2d!        [^UnityEngine.Rigidbody2D body r] (.AddTorque body (float r)) nil)

(defn ->velocity [o]
  (if-let [body (cond (rigidbody? o) o (gob? o) (->rigidbody o))]
    (.velocity body)))

(defn layer-mask [& s]
  (short (UnityEngine.LayerMask/GetMask (into-array s))))

(defn overlap-circle 
  ([p r] (Physics2D/OverlapCircle p (float r)))
  ([p r m] (Physics2D/OverlapCircle p (float r) m) ))

(defn overlap-sphere 
  ([p r] (Physics/OverlapSphere p (float r)))
  ([p r m] (Physics/OverlapSphere p (float r) m) ))