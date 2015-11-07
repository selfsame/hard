(ns hard.mesh
  (:use [hard.core])
  (:import
    [UnityEngine Color]))

(defn vertices [gob] 
  (.vertices (.sharedMesh (.GetComponent gob "MeshFilter"))))

(defn color! [gob col]
  (when (gameobject? gob)
    (when-let [meshfilter (.GetComponent gob "MeshFilter")]
      (let [mesh (if (-editor?) (.mesh meshfilter) (.mesh meshfilter))
          verts (.vertices mesh)
          colors (into-array (take (count verts) (repeat col)))]
        (set! (.colors mesh) colors) nil))))

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