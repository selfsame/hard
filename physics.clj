(ns hard.physics
  (:use arcadia.linear)
  (:import
    [UnityEngine Ray Physics RaycastHit Physics2D]))

(defn gob? [x] (instance? UnityEngine.GameObject x))

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

(defn hit [^Vector3 a ^Vector3 b]
  (if (> (hit* a b) 0) (aget hit-buff 0)))

(defn hits [^Vector3 a ^Vector3 b]
  (map #(aget hit-buff %) (range (hit* a b))))

(defn range-hits [^Vector3 a ^Vector3 b ^System.Double len]
  (map #(aget hit-buff %) (range (hit* a b len))))

#_(reduce v3+ (map #(.point %)  (hits (v3) (v3 0 -1 0))))

(defn gravity [] (Physics/gravity))

(defn gravity! [v3] (set! (Physics/gravity) v3))

(defn rigidbody? [o] (instance? UnityEngine.Rigidbody o))

(defn ->rigidbody [v]
  (if-let [o (.gameObject v)] (.GetComponent o UnityEngine.Rigidbody) nil))

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