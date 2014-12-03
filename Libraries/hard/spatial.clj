(ns hard.spatial
	(:use hard.core)
  (:require arcadia.core ))
   
(def ^:private UID (atom 0))
(defn- get-uid [] (swap! UID inc)) 
(def RECORDS (atom {}))

(defn forget [thing record]
  (let [members @(:membership record)
          data @(:data record)
          member-record (or (get members thing) false)]
      (when member-record 
        (do
          (when (get data member-record)
            (swap! (get data member-record) disj thing))
        (swap! (:membership record) #(dissoc % thing))))))



 (arcadia.core/defcomponent SpatialRecordEntry [^int uid]
  (OnDestroy [this]
    (forget (.gameObject this) (get @RECORDS uid))))

(defn bucket-hash [size]
  (let [uid (get-uid)
    res
  {:uid uid
   :size size
   :data (atom {})
   :membership (atom {})}] 
   (swap! RECORDS conj {uid res})
   res))
 
(defn gob->bucket [thing size]
	(when (gameobject? thing) (mapv int (-v / (vec3 thing) size))))

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
    (swap! (:membership H) conj {uid bucket} )
    ;else
    (when (not= member-record bucket)
      ;remove the member from old bucket"
      (swap! (get data member-record) disj uid)))

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


