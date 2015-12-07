(ns hard.mesh
  (:use [hard.core])
  (:import
    [UnityEngine Color]))

(defn vertices [gob] 
  (.vertices (.sharedMesh (.GetComponent gob "MeshFilter"))))

(defn vertex-color! [gob col]
  (when (gameobject? gob)
    (when-let [meshfilter (.GetComponent gob "MeshFilter")]
      (let [mesh (if (-editor?) (.sharedMesh meshfilter) (.sharedMesh meshfilter))
          verts (.vertices mesh)
          colors (into-array (take (count verts) (repeat col)))]
        (set! (.colors mesh) colors) nil))))

(defn material-color! [o c]
  (set! (.color (.material (.GetComponent o UnityEngine.Renderer))) c))

(defn vertex-colors! [gob c]
  (when (gameobject? gob)
    (when-let [meshfilter (.GetComponent gob "MeshFilter")]
      (let [mesh (if (-editor?) (.mesh meshfilter) (.mesh meshfilter))
          verts (.vertices mesh)
          fn (cond (fn? c) c
                :else (fn [_ _ _ _] c))
          colors (into-array (doall 
                (for [idx (range (count verts))
                  :let [v (.GetValue verts idx)
                      x (.x v)
                      y (.y v)
                      z (.z v)]]
                  (fn x y z idx))))]
        (set! (.colors mesh) colors) 
        (count colors) ))))

(defn yfade [c1 c2]
  (fn [x y z i] (Color/Lerp c1 c2 y)))

(defn map-mesh-set! [o f]
  (let [mf (component o UnityEngine.MeshFilter)
        vs (.. mf mesh vertices)]
    (set! (.vertices (.mesh mf)) 
     (into-array (vec (for [i (range (count vs))] (f i (get (.vertices (.mesh mf)) i))))))
    (.RecalculateNormals (.mesh mf))
  true))

(defn hill-color [o f]
  (let [mf (component o UnityEngine.MeshFilter)]
    (set! (.colors (.mesh mf)) 
     (into-array (vec (for [i (range (count (.colors (.mesh mf))))] (f i (get (.normals (.mesh mf)) i))))))
  true))