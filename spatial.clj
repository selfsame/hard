(ns hard.spatial
	(:use hard.core)
  (:require arcadia.core )
  (:import [Mathf]))
   
(def ^:private UID (atom 0))
(defn- get-uid [] (swap! UID inc)) 
(def RECORDS (atom {}))

(defn forget [thing record]
  (when record
  (let [members @(:membership record)
          data @(:data record)
          member-record (or (get members thing) false)]
      (when member-record 
        (do
          (when (get data member-record)
            (swap! (get data member-record) disj thing))
        (swap! (:membership record) #(dissoc % thing)))))))



 (arcadia.core/defcomponent SpatialRecordEntry [^int uid]
  (OnDestroy [this]
    (forget (->go this) (get @RECORDS uid))))

; (arcadia.core/defcomponent Gozmo [] 
;   (OnDrawGizmos [this] 
;     (gizmo-color (color [0.2 0.2 0.6]))
;     (Gizmos/DrawSphere (->v3 this) 0.5)
;     (hash-vis this)
;     (mapv block-vis (sel))))


(defn bucket-hash [size]
  (let [uid (get-uid)
    res
  {:uid uid
   :size size
   :data (atom {})
   :membership (atom {})}] 
   (swap! RECORDS conj {uid res})
   res))

(defn inty [n]
  (if (neg? n) (dec (int n)) (int n)))

(defn gob->bucket [thing size]
	(when (gameobject? thing) (mapv inty (vdiv (->v3 thing) size))))

(defn store [thing H]
  (when (gameobject? thing)
    (when-not (component thing SpatialRecordEntry)
      (set! (.uid (.AddComponent thing SpatialRecordEntry)) (int (:uid H)))))
  (let [members @(:membership H)
        data @(:data H)
        size (:size H)
        client thing
        uid thing
        [x y z] (gob->bucket thing size)
        bucket [x y z]
        member-record (or (get members uid) false)]

  (if-not member-record 
    ;register
    (swap! (:membership H) conj {uid bucket}))
    
  (when (not= member-record bucket)
      (when-let [from (get data member-record)]
        (if (= 1 (count @from))
          (swap! (:data H) dissoc member-record)
          (swap! from disj uid))
      (swap! (:membership H) conj {uid bucket})))

  (if (nil? (get data bucket))
    (swap! (:data H) conj {bucket (atom #{uid})})
    ;otherwise just add to existant atomic bucket
    (swap! (get data bucket) conj uid))))


(defn bucket-others [thing bucket]
	(let [[x y z] (gob->bucket thing (:size bucket))
		  slots (into #{}
		  	(reduce concat
			  	(for [bx [(dec x) x (inc x)]
			  			  by [(dec y) y (inc y)]
			  			  bz [(dec z) z (inc z)]
			  			  :let [stash (get @(:data bucket) [bx by bz])]
			  			  :when stash]
			  			@stash)))]
		  (disj slots thing)))


