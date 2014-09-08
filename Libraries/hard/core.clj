
(ns hard.core
  (:import
    [UnityEngine Debug Resources PrimitiveType Application Color]))

(defn log [x] 
	(UnityEngine.Debug/Log x))

;(defn math [f & more]
;	(. UnityEngine.Mathf (f more)))

(defn resource [s] (UnityEngine.Resources/Load s))
 
(defn editor? []
	(. Application isEditor))

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


(defn primitive [kw]
	(GameObject/CreatePrimitive (kw primitive-map)))

(defn find-name [str] (. GameObject (Find str)))

 

(defn vec3 [o]
	(cond (vector3? o) o
		(sequential? o) (let [[x y z] o] (Vector3. (or x 0) (or y 0) (or z 0)))
		(transform? o)  (.position o)
		(gameobject? o) (.position (.transform o))))

(defn unvec [o]
	[(.x o)(.y o)(.z o)])

(defn position! [o pos]
	(cond (gameobject? o) (set! (.position (.transform o)) (vec3 pos))))

(defn local-scale [o]
	(cond (gameobject? o) (.localScale (.transform o) )))

(defn local-scale! [o v]
	(cond (gameobject? o) (set! (.localScale (.transform o)) (vec3 v))))

(defn rotate-around! [o point axis angle]
	(when (gameobject? o) 
	(. (.transform o) (RotateAround (vec3 point) (vec3 axis) angle))))

(defn destroy! [o]
	(cond (gameobject? o)
		(if (editor?)
			(. GameObject (DestroyImmediate o))
			(. GameObject (Destroy o)))
		(sequential? o)
		(mapv destroy! o)) nil)

(defn clone 
	([ref] (clone ref false))
	([ref pos]
		(let [source (if (string? ref) (find-name ref) ref)
			  gob (. GameObject (Instantiate source))]
			(when pos (position! gob pos)) 
			gob)))




(defn vec3*n [o n]
	(Vector3/Scale (vec3 o) (vec3 [n n n])))


;(set! (.color (.material (.renderer cube))) (Vector4. 1.0 0.2 0.5 1.0))
;(into-array Object [1 2 3])


(defn vertices [gob]
	(.vertices (.sharedMesh (.GetComponent gob "MeshFilter"))))

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


(defn color! [gob col]
	(when (gameobject? gob)
		(when-let [meshfilter (.GetComponent gob "MeshFilter")]
			(let [mesh (if (editor?) (.mesh meshfilter) (.mesh meshfilter))
				  verts (.vertices mesh)
				  colors (into-array (take (count verts) (repeat col)))]
				(set! (.colors mesh) colors) nil))))


(defn yfade [c1 c2]
	(fn [x y z i] (Color/Lerp c1 c2 y)))


(defn mud [n1 n2]
	(let [clean (fn [x] (inc (. UnityEngine.Mathf (Abs x))))]
		(mod (clean n1) (clean n2))))


(log "hard.core is here")
