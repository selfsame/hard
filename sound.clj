(ns hard.sound
  (:import [UnityEngine GameObject Application]))


(def audio-clips
  (if (. Application isPlaying)
    (into {} (mapv (juxt #(.name %) identity) 
    (UnityEngine.Resources/FindObjectsOfTypeAll UnityEngine.AudioClip)))))

(defn play-clip! 
  ([k] (play-clip!  k {})) 
  ([k opts] 
    (when-let [clip (get audio-clips k)]
      (let [idle-source 
        (or (first (remove #(.isPlaying %) 
              (UnityEngine.Resources/FindObjectsOfTypeAll UnityEngine.AudioSource)))
          (.AddComponent (GameObject. "sound") UnityEngine.AudioSource))]
        (assert idle-source)
        (when idle-source (set! (.clip idle-source) clip)
        (if (:volume opts) (set! (.volume idle-source) (float (:volume opts))))
        (.Play idle-source))))))

 