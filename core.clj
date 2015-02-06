
(ns hard.core
  (:require arcadia.core)
  (:import
    [UnityEngine Physics Debug Resources PrimitiveType Application Color Input Screen]))
 
(declare position!)

(defn log [x] 
  (UnityEngine.Debug/Log x))

(defn vector2? [x] (instance? UnityEngine.Vector2 x))
(defn vector3? [x] (instance? UnityEngine.Vector3 x))
(defn vector4? [x] (instance? UnityEngine.Vector4 x))
(defn transform? [x] (instance? UnityEngine.Transform x))
(defn quaternion? [x] (instance? UnityEngine.Quaternion x))
(defn color? [x] (instance? UnityEngine.Color x))
(defn gameobject? [x] (instance? UnityEngine.GameObject x))

(defn- get-or [col idx nf] (or (get col idx) nf))
    
(defn- -count [o]
  (cond (number? o) 1
        (sequential? o) (count o)
        (vector3? o) 3
        (vector2? o) 2
        (vector4? o) 4
        (color? o) 4)) 

(defn- -vec [o]
  (cond (number? o) [o]
    (vector2? o) [(.x o)(.y o)]
    (vector3? o) [(.x o)(.y o)(.z o)]
    (vector4? o) [(.x o)(.y o)(.z o)(.w o)]
    (color? o) [(.r o)(.g o)(.b o)(.a o)] 
  :else
  (try (vec o) (catch Exception e (str e)))))

(defn- operate [op -a -b]
  (let [c (max (-count -a)(-count -b))
        a (if (number? -a) (vec (take c (repeat -a)))
              (-vec -a))
        b (if (number? -b) (vec (take c (repeat -b)))
              (-vec -b))]
    (map #(op (get-or a % 0) (get-or b % 0)) (range c))))

(defn- reduce-operate [op col]
  (vec (reduce #(operate op %1 %2) col)))

(defn v+ [& more] (reduce-operate + more))
(defn v- [& more] (reduce-operate - more))
(defn v* [& more] (reduce-operate * more))
(defn vdiv [& more] (reduce-operate / more))
(defn -v [op & more] (reduce-operate op more)) 

(defn find-name [str] (. GameObject (Find str)))

(defn ->go [v]
  (cond (gameobject? v) v   
      (sequential? v) nil
      (string? v) (try (find-name v) (catch Exception e nil))
      :else (try (.gameObject v) (catch Exception e nil))))

(defn ->transform [v]
  (cond (transform? v) v
      :else (if-let [o (->go v)] (.transform o) nil)))

(defn ->v3 [o] 
  (cond (number? o) (Vector3. o o o)
    (sequential? o) (Vector3. (get-or o 0 0) (get-or o 1 0) (get-or o 2 0))
    (vector3? o) o
    (vector2? o) (Vector3. (.x o) (.y o) 0)
    (vector4? o) (Vector3. (.x o) (.y o) (.z o))
    (quaternion? o) (Vector3. (.x o)(.y o)(.z o))
    (color? o) (Vector3. (.r o)(.g o)(.b o))
    (transform? o) (.position o)
    (gameobject? o) (.position (.transform o))
    :else
    (try (.position (.transform (.gameObject o)))
        (catch Exception e (type o)))))

(defn ->vec [o]
  (cond 
    (vector3? o) [(.x o)(.y o)(.z o)]
    (vector2? o) [(.x o)(.y o)]
    (vector4? o) [(.x o)(.y o)(.z o)(.w o)]
    (quaternion? o) [(.x o)(.y o)(.z o)(.w o)]
    (color? o) [(.r o)(.g o)(.b o)(.a o)]
    :else (vec o)))

(defn vec3 [o]
  "depreciated" (->v3 o))

(defn unvec [o]
  "depreciated"
  [(.x o)(.y o)(.z o)])

(defn -editor? []
  (. Application isEditor)) 

(defn playing? []
  (. Application isPlaying)) 

(defn load-scene [sn]
  (log (str "loading scene " sn "..."))
  (Application/LoadLevel sn))

(defn quit [sn]
  (Application/Quit))

(defn screen-size []
  [(Screen/width)(Screen/height)])

(defn main-camera [] (UnityEngine.Camera/main))


(defn resource [s] (UnityEngine.Resources/Load s))

(defn null? [gob]
  (and (gameobject? gob) (Extras/nullObject gob)))

(defn destroy! [o]
  (if-let [o (->go o)]
    (if (gameobject? o)
      (if (-editor?)
        (. GameObject (DestroyImmediate o))
        (. GameObject (Destroy o)))
  (if (sequential? o)
    (mapv destroy! o)))))

(defn primitive! [kw]
  (GameObject/CreatePrimitive 
    {:cube UnityEngine.PrimitiveType/Cube
    :plane UnityEngine.PrimitiveType/Plane
    :sphere UnityEngine.PrimitiveType/Sphere
    :cylinder UnityEngine.PrimitiveType/Cylinder
    :quad UnityEngine.PrimitiveType/Quad
    :capsule UnityEngine.PrimitiveType/Capsule}))

(def CLONED (atom []))

(defn clone!
  ([ref] (clone! ref false))
  ([ref pos]
    (let [source (if (string? ref) (find-name ref) ref)
        gob (. GameObject (Instantiate source))]
      (swap! CLONED #(cons gob %))
      (when pos (position! gob pos)) 
      gob)))

(defn clear-cloned [] 
  (mapv destroy! @CLONED) 
  (reset! CLONED []))

(defn gravity [] (Physics/gravity))

(defn gravity! [v3] (set! (Physics/gravity) (vec3 v3)))
 
;uh.. so this is not really saving much typing
(defn color 
  ([col] (if (> (count col) 2) (apply color (take 4 col)) (color 0 0 0 0)))
  ([r g b] (color r g b 1.0))
  ([r g b a] (Color. r g b a)))
 


(defn- clamp-v3 [v3 min max]
  (let [v (unvec (vec3 v3))
      res (mapv #(Mathf/Clamp % min max) v)]
      (vec3 res)))


;The following functions assume gameobject args, I'm big on that but i guess they should also
;accept transforms
 
(defn parent! [a b]
  (set! (.parent (.transform a)) (.transform b)))

(defn world-position [o]
  (when-let [o (->go o)] (.TransformPoint (.transform o) (->v3 o))))

(defn position! [o pos]
  (when-let [o (->go o)] (set! (.position (.transform o)) (->v3 pos))))

(defn local-direction [o v]
  (when-let [o (->go o)]
    (let [[x y z] (if (vector3? v) (unvec v) v)]
      (.TransformDirection (.transform o) x y z))))

(defn transform-point [o v]
  (when-let [o (->go o)]
    (.TransformPoint (.transform o) (->v3 v))))

(defn inverse-transform-point [o v]
  (when-let [o (->go o)]
    (.InverseTransformPoint (.transform o) (->v3 v))))

(defn move-towards [v1 v2 step]
  (Vector3/MoveTowards v1 v2 step))

(defn lerp [v1 v2 ratio]
  (Vector3/Lerp (->v3 v1) (->v3 v2) ratio))

(defn local-scale [o]
  (when-let [o (->go o)] (.localScale (.transform o) )))

(defn local-scale! [o v]
  (when-let [o (->go o)] (set! (.localScale (.transform o)) (->v3 v))))

(defn rotate-around! [o point axis angle]
  (when-let [o (->go o)]
  (. (.transform o) (RotateAround (->v3 point) (->v3 axis) angle))))

(defn rotation [o]
  (when-let [o (->go o)] (.eulerAngles (.rotation (.transform o) ))))

(defn rotate! [o rot]
  (when-let [o (->go o)]
    (.Rotate (.transform o) (->v3 rot))))

(defn rotation! [o rot]
  (when-let [o (->go o)]
    (set! (.eulerAngles (.transform o)) (clamp-v3 rot 0 360))))



 
 
 
(defn ^:private appropiate-game-object [reference]
  (cond (nil? reference) nil
    (gameobject? reference) reference
    :else (try (.gameObject reference) (catch Exception e nil))))

(defn component [o sym]
  (when-let [o (->go o)]
    (.GetComponent o sym)))

(defn parent-component [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentInParent gob sym))))

(defn child-component [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentInChildren gob sym))))

(defn components [thing sym]
  (when-let [gob (->go thing)]
    (.GetComponents gob sym)))

(defn parent-components [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentsInParent gob sym))))

(defn child-components [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentsInChildren gob sym))))

(log "hard.core is here")
