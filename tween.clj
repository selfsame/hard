(ns hard.tween
  (:use hard.core hard.input hard.boiled hard.protocols)
  (:require arcadia.core 
  [clojure.string :as string])
  (:import [UnityEngine Color Vector3]))
 
(declare -update transition tweak)
 
(comment TODO

  ; [\] relative tweens
  ;   [ ] additive tweens (will tween to a relative value even concurrent with other same channel effects)

  ; cloning and chaning targets on tweens

  ; better builtin property accessors

  ; way to stop tweens

  ; easing functions
  )



(def ^:private UID (atom 0))

(defn- get-uid [] (swap! UID inc))

(def ^:private tweens (atom {}))
(def ^:private new-tweens (atom {}))

(defn -update [delta]
  (let [now (UnityEngine.Time/time)
        res (into {} (map (fn [t] (tweak t now)) @tweens))]
    (reset! tweens (conj @new-tweens res))
    (reset! new-tweens {})))

(defn clean-tweens []
  (reset! tweens {})
  (reset! new-tweens {}))

(arcadia.core/defcomponent Tween []
  (Update [this] (-update (UnityEngine.Time/deltaTime))))


(def ^:private handlers (atom 
  {[:position] {:get #(->v3 %1)
                :set #(kset! (->v3 %2) %1 :transform :position )}
  [:local-scale] {:get #(vec3 (local-scale %1))
                  :set #(local-scale! %1 %2)}
  [:euler] {:get #(rotation %1)
            :set #(rotation! %1 %2)}
  [:camera :field-of-view]
    {:get #(.fieldOfView (component %1 "Camera")) 
     :set #(set! (.fieldOfView (component %1 "Camera")) %2)}
  [:camera :background]
    {:get #(.backgroundColor (component %1 "Camera")) 
     :set #(set! (.backgroundColor (component %1 "Camera")) %2)}
  [:text-mesh :color] 
    {:get #(.color (component %1 "TextMesh")) 
     :set #(set! (.color (component %1 "TextMesh")) %2)}
     }))


(defn register [path getter setter]
  (swap! handlers #(conj % {path {:get getter :set setter}})))

(register [:blend-shape 0] 
  #(.GetBlendShapeWeight (component % "SkinnedMeshRenderer") 0)
  #(.SetBlendShapeWeight (component %1 "SkinnedMeshRenderer") 0 %2))

(register [:blend-shape 1] 
  #(.GetBlendShapeWeight (component % "SkinnedMeshRenderer") 1)
  #(.SetBlendShapeWeight (component %1 "SkinnedMeshRenderer") 1 %2))

(register [:blend-shape 2] 
  #(.GetBlendShapeWeight (component % "SkinnedMeshRenderer") 2)
  #(.SetBlendShapeWeight (component %1 "SkinnedMeshRenderer") 2 %2))


(def ^:private easing 
  {:pow2 #(* % %)
    :pow3 #(* % % %)
    :pow4 #(* % % % %)
    :pow5 #(* % % % % %)
  :in #(%2 %1)
  :out #(-1 (%2 (- 1 %1)))
  :inout #(cond (< %1 0.5) (/ (%2 (* 2 %1)) 2)
                  :else (- 1 (/ (%3 (* 2 (- 1 %1))) 2)))})




(defn- cast-as [v t]
  (if (= t (type v)) v
    (case t
      System.Int64 (int v)
      System.Double (float v)
      UnityEngine.Vector3 (vec3 v)
      UnityEngine.Color (color v)
      (if (sequential? v) (vec3 v) v))))


(defn pkv [px col]
  (mapcat (fn [[k v]] 
    (if (map? v)
      (pkv (conj px k) v)
      [[(conj px k) v]])) col))

(defn parse-tween [data]
  (mapv (fn [[p v]] 
    {:target (first p)
      :path (vec (rest p))
      :value v}) (pkv [] data)))

(defn- wake! []
  (when-not (find-name "__tweener__")
    (let [c (GameObject. "__tweener__")]
      (.AddComponent c "Tween"))))

(defn run [t]
  (when (valid? t)
    (wake!)
    (swap! new-tweens conj {t (UnityEngine.Time/time)})))

(defn- tweak [[t began] now]
  (--update t began now))
  


(defn delete [uid]
  (swap! tweens #(dissoc % uid)))




(defn -walklink [g visited]
  (mapcat #(list % 
    (if (visited %) 'LOOP
      (-walklink % (conj visited %)))) (linked g)))

(defn walklink [node]
  (list node
    (-walklink node #{node})))

(defn link [& more]
  (when-not (empty? more)
    (reduce -link more)))


(deftype T [^String name ^float duration 
            opts links effects]
  clojure.lang.IMeta
  (meta [this] (meta links))
  clojure.lang.IObj
  (withMeta [this m] this)
  (ToString [this] 
    (let [minutes (int (/ duration 60))
          seconds (int (mod duration 60))]
    (str name " " (when (pos? minutes) (str minutes "m")) seconds "s ")))
  Linkable
    (-link [this oth]
      (if-let [other (cond (number? oth) 
                        (transition oth "delay")
                        (instance? T oth) oth
                        :else nil)]
      (do (swap! links conj other) other)
      this))
    (linked [this] @links)
  clojure.lang.ILookup
    (valAt [_ k]
      (case k
        :targets (vec (set (map #((.t %)) @effects)))
        :name name
        (get opts k)))
    (valAt [_ k nf]
      (case k
        :targets (vec (set (map #((.t %)) @effects)))
        :name name
        (get opts k nf)))
  Validatable
    (valid? [this]
      (if ((set (mapv valid? @effects)) false)
          false true))
  Temporal
    (--update [me began now]
      (try             
        (let [time-alive (- now began)
              ratio (Mathf/Clamp (/  time-alive duration) 0.0 1.0)
              
              easefn (first (filter (:flags opts) (keys easing)))
              in (get easing (or (:in opts) easefn))
              out (get easing (or (:out opts) easefn))

              eased (cond (and in out) ((:inout easing) (float ratio) in out)
                        in ((:in easing) (float ratio) in)
                        out ((:out easing) (float ratio) out)
                        :else ratio)
              time-remaining (- time-alive duration)]
          (mapv #(show-ratio % eased) @effects)
          

          (if (pos? time-remaining)
              (do
                (mapv #(set! (.start (.g %)) nil) @effects)
                ;will try 1 arity, catch try 0 arity
                (when-let [cback (:callback opts)]
                  (try (cback me) 
                    (catch Exception e 
                      (try (cback) (catch Exception e (log (str e)))))))
                (into {} (mapv (fn [n] {n (- now time-remaining)}) @links)))
              {me began}))
          (catch Exception e 
            (do (log (str "removing " me))
              {})))))



(defn- maybe [pred col default]
  (or (first (filter pred col)) default))



(deftype Accessor [^clojure.lang.Fn getter ^clojure.lang.Fn setter]
  Validatable
    (valid? [this] (and (fn? getter)(fn? setter)))
  clojure.lang.IFn
    (invoke [this o] (getter o))
    (invoke [this o v] (setter o v)))

(deftype Target [col]
  Validatable
    (valid? [this] (not (nil? col)))
  clojure.lang.IFn
    (invoke [this] col))

(deftype Goal [^{:volatile-mutable true} start end]
  Validatable
    (valid? [this] (not (empty? (filter nil? [start end]))))
  clojure.lang.IFn
    (invoke [this] end))

(deftype Implementation [^clojure.lang.Fn ease]
  Validatable
    (valid? [this] (fn? ease))
  clojure.lang.IFn
    (invoke [this start end ratio] 
      (ease start end ratio)))

(deftype Effect [^Target t ^Accessor a ^Goal g ^Implementation i ^boolean ^{:volatile-mutable true} relative]
    Validatable
    (valid? [this] (and (map valid? [t a g i])
                        (try (a (t)) (catch Exception e nil))))
    Ratial
    (reset [this]
      (swap! (.start g) nil))
    (show-ratio [this r]
      (when (nil? (.start g))
        (set! (.start g) (a (t))))
      (let [end (if relative (v+ (.start g) (.end g)) (.end g))
            res (i (.start g) end r)]
        (a (t) res))))


(defn transition [& more]
  (let [name (maybe string? more "")
        duration (maybe number? more 1)
        callback (maybe fn? more nil)
        opts (maybe map? more {})
        flags (filter keyword? more)
        defopts {:callback callback
                 :flags (set flags)}]
        (T. name duration (conj opts defopts) (atom []) (atom []))))


(def default-imp 
  (Implementation. 
  (fn [start end ratio]
    (let [f (cond (color? start) color
                  :else ->v3)
          span (v- end start)
          res (v* span ratio)]
    (f (v+ start res))))))

(defn- make-effect [emap]
  (when-let [handler (get @handlers (:path emap))]
            (Effect.
              (Target. (:target emap))
              (Accessor. (:get handler) (:set handler))
              (Goal. nil (:value emap))
              default-imp
              false)))

(defn vecmap? [x] (or (vector? x) (map? x)))
(def nonvecmap? (complement vecmap?))

(defn tween [& more]
      (let [vecmaps (filter vecmap? more)
            data (first vecmaps)
            opts (or (first (rest vecmaps)) {})
            parsed (cond (map? data) (parse-tween data)
                        (sequential? data) [{:target (first data)
                                             :path (pop (vec (rest data))) 
                                             :value (peek data)}])
            trans (apply transition (cons opts (filter nonvecmap? more)))
            effects (mapv make-effect parsed)]
        (when ((:flags trans) :+) 
          (mapv #(set! (.relative %) true) effects))
        (swap! (.effects trans) concat effects)
        trans))



(log "hard.tween fades in")
 
(comment

(def t1
  (tween {(find-name "label") 
        {:position [5 3 50] 
         :local-scale [1 1 1]}}
         2.5 "joe" :pow5 :+))

(def t3
  (tween 1.25 :pow2 [(find-name "label") :text-mesh :color [0.0 6.0 1.0 1.0]] ))

)





