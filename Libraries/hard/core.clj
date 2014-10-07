
(ns hard.core
  (:import
    [UnityEngine Physics Debug Resources PrimitiveType Application Color Input]))

(defn log [x] 
	(UnityEngine.Debug/Log x))

(defn resource [s] (UnityEngine.Resources/Load s))
 
;is a no arg predicate-style bad style?
(defn editor? []
	(. Application isEditor))


;needs to be more of these
(defn vector3? [x]
	(if (instance? UnityEngine.Vector3 x) true false))
(defn transform? [x]
	(if (instance? UnityEngine.Transform x) true false))
(defn gameobject? [x]
	(if (instance? UnityEngine.GameObject x) true false))


(def primitive-map {
		:cube UnityEngine.PrimitiveType/Cube
		:plane UnityEngine.PrimitiveType/Plane
		:sphere UnityEngine.PrimitiveType/Sphere
		:cylinder UnityEngine.PrimitiveType/Cylinder
		:quad UnityEngine.PrimitiveType/Quad
		:capsule UnityEngine.PrimitiveType/Capsule})

;should fns that alter the scene-graph use bang names?
;this should have a 2 arity for setting position 
(defn primitive! [kw]
	(GameObject/CreatePrimitive (kw primitive-map)))


;good fns for these things in unity.core now
(defn find-name [str] (. GameObject (Find str)))
 
;uh.. so this is not really saving much typing
(defn color 
	([col] (if (> (count col) 2) (apply color (take 4 col)) (color 0 0 0 0)))
	([r g b] (color r g b 1.0))
	([r g b a] (Color. r g b a)))




;this is a quick hack, Omar S. has much better stuff in works
(defn  -v [op & more]
	(apply mapv op 
		(map #(cond (number? %) [% % %]
					(vector3? %) [(.x %) (.y %) (.z %)]
					(= (type %) Color) [(.r %) (.g %) (.b %)]
					:else %) more)))

;the overloading in this is probably too specific to my workflow
(defn vec3 [o]
	(cond (vector3? o) o
		(sequential? o) (let [[x y z] o] (Vector3. (or x 0) (or y 0) (or z 0)))
		(transform? o)  (.position o)
		(gameobject? o) (.position (.transform o))))
  
;ugly - probably can add a protocol for vec to Vector3/2 types?
(defn unvec [o]
	[(.x o)(.y o)(.z o)])


(defn- clamp-v3 [v3 min max]
	(let [v (unvec (vec3 v3))
		  res (mapv #(Mathf/Clamp % min max) v)]
		  (vec3 res)))


;The following functions assume gameobject args, I'm big on that but i guess they should also
;accept transforms
 
(defn parent! [a b]
	(set! (.parent (.transform a)) (.transform b)))

(defn world-position [o]
	(cond (gameobject? o) (.TransformPoint (.transform o) (vec3 o))))

(defn position! [o pos]
	(cond (gameobject? o) (set! (.position (.transform o)) (vec3 pos))))

(defn local-direction [o v]
	(cond (gameobject? o)
		(let [[x y z] (if (vector3? v) (unvec v) v)]
	 		(.TransformDirection (.transform o) x y z))))

(defn local-scale [o]
	(cond (gameobject? o) (.localScale (.transform o) )))

(defn local-scale! [o v]
	(cond (gameobject? o) (set! (.localScale (.transform o)) (vec3 v))))

(defn rotate-around! [o point axis angle]
	(when (gameobject? o) 
	(. (.transform o) (RotateAround (vec3 point) (vec3 axis) angle))))

(defn rotation [o]
	(cond (gameobject? o) (.eulerAngles (.rotation (.transform o) ))))

(defn rotate! [o rot]
	(when-let [trans (cond 
		(gameobject? o) (.transform o)
		(transform? o) o)]
		(.Rotate trans (vec3 rot))))

(defn rotation! [o rot]
	(when (gameobject? o) 
		(set! (.eulerAngles (.transform o)) (clamp-v3 rot 0 360))))


;I think this is good
(defn destroy! [o]
	(cond (gameobject? o)
		(if (editor?)
			(. GameObject (DestroyImmediate o))
			(. GameObject (Destroy o)))
		(sequential? o)
		(mapv destroy! o)) nil)

;I don't understand/like the prefab concept hence the name
(defn clone!
	([ref] (clone! ref false))
	([ref pos]
		(let [source (if (string? ref) (find-name ref) ref)
			  gob (. GameObject (Instantiate source))]
			(when pos (position! gob pos)) 
			gob)))


(defn gravity [] (Physics/gravity))

(defn gravity! [v3] (set! (Physics/gravity) (vec3 v3)))


;eh.. 
(defn vertices [gob]
	(.vertices (.sharedMesh (.GetComponent gob "MeshFilter"))))

;sets vertex colors, which is awesome but probably not unity.core material
(defn color! [gob col]
	(when (gameobject? gob)
		(when-let [meshfilter (.GetComponent gob "MeshFilter")]
			(let [mesh (if (editor?) (.mesh meshfilter) (.mesh meshfilter))
				  verts (.vertices mesh)
				  colors (into-array (take (count verts) (repeat col)))]
				(set! (.colors mesh) colors) nil))))

;dito
(defn vertex-colors! [gob c]
	(when (gameobject? gob)
		(when-let [meshfilter (.GetComponent gob "MeshFilter")]
			(let [mesh (if (editor?) (.mesh meshfilter) (.mesh meshfilter))
				  verts (.vertices mesh)
				  fn (cond (fn? c) c
				  			:else (fn [_ _ _ _] (Color. 0 0 0 0)))
				  colors (into-array (doall 
				  			(for [idx (range (count verts))
				  				:let [v (.GetValue verts idx)
				  					  x (.x v)
				  					  y (.y v)
				  					  z (.z v)]]
				  				(fn x y z idx))))]
				(set! (.colors mesh) colors) 
				(count colors) ))))

;junk
(defn yfade [c1 c2]
	(fn [x y z i] (Color/Lerp c1 c2 y)))

;junk
(defn vec3*n [o n]
	(Vector3/Scale (vec3 o) (vec3 [n n n])))
;junk
(defn mud [n1 n2]
	(let [clean (fn [x] (inc (. UnityEngine.Mathf (Abs x))))]
		(mod (clean n1) (clean n2))))


 
 
(defn load-scene [sn]
	(log (str "loading scene " sn "..."))
	(Application/LoadLevel sn))

(defn quit [sn]
	(Application/Quit))
 
(defn ^:private appropiate-game-object [reference]
	(cond (nil? reference) nil
		(gameobject? reference) reference
		:else (try (.gameObject reference) (catch Exception e nil))))

(defn component [thing sym]
	(let [gob (appropiate-game-object thing)]
		(.GetComponent gob sym)))

(defn parent-component [thing sym]
	(when-not (string? sym)
		(let [gob (appropiate-game-object thing)]
			(.GetComponentInParent gob sym))))

(defn child-component [thing sym]
	(when-not (string? sym)
		(let [gob (appropiate-game-object thing)]
			(.GetComponentInChildren gob sym))))

(defn components [thing sym]
	(let [gob (appropiate-game-object thing)]
		(.GetComponents gob sym)))

(defn parent-components [thing sym]
	(when-not (string? sym)
		(let [gob (appropiate-game-object thing)]
			(.GetComponentsInParent gob sym))))

(defn child-components [thing sym]
	(when-not (string? sym)
		(let [gob (appropiate-game-object thing)]
			(.GetComponentsInChildren gob sym))))

(log "hard.core is here")
