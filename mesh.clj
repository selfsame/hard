(ns hard.mesh
  (:import
    [UnityEngine Color]))

(defn gobj? [x] (instance? UnityEngine.GameObject x))

(defn vertices [^UnityEngine.GameObject gob] 
  (.vertices (.mesh (.GetComponent gob UnityEngine.MeshFilter))))

(defn vertices! [^UnityEngine.GameObject gob ^UnityEngine.Vector3|[]| ar] 
  (set! (.vertices (.mesh (.GetComponent gob UnityEngine.MeshFilter))) ar))

(defn vertex-color! [gob col]
  (when (gobj? gob)
    (when-let [meshfilter (.GetComponent gob UnityEngine.MeshFilter)]
      (let [mesh (.sharedMesh meshfilter) 
          verts (.vertices mesh)
          colors (into-array (take (count verts) (repeat col)))]
        (set! (.colors mesh) colors) nil))))


(defn vertex-colors! [gob c]
  (when (gobj? gob)
    (when-let [meshfilter (.GetComponent gob UnityEngine.MeshFilter)]
      (let [mesh (.mesh meshfilter)
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

(defn map-mesh-set! [o f]
  (let [mf (.GetComponent o UnityEngine.MeshFilter)
        vs (.. mf mesh vertices)]
    (set! (.vertices (.mesh mf)) 
     (into-array (vec (for [i (range (count vs))] (f i (get (.vertices (.mesh mf)) i))))))
    (.RecalculateNormals (.mesh mf))
    ;TODO recalc bounds
  true))

(defn vcol-fn-normals [o f]
  (let [mf (.GetComponent o UnityEngine.MeshFilter)]
    (set! (.colors (.mesh mf)) 
     (into-array (vec (for [i (range (count (.colors (.mesh mf))))] (f i (get (.normals (.mesh mf)) i))))))
  true))