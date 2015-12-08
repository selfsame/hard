(ns hard.seed
  (:use hard.core))

(defn seed! [v] (set! UnityEngine.Random/seed (hash v)))

(defn srand 
  ([] UnityEngine.Random/value)
  ([n] (* n (srand))))

(defn srand-int [n] 
  (int (* (srand) n)))

(defn srand-nth [col] 
  (get col (srand-int (count col))))

(defn sshuffle [col]
  (sort-by (fn [_](srand)) col))



(defn srand-vec [& more]
  (mapv (fn [col]
    (cond (number? col) (srand col)
    (sequential? col) 
    (case (count col) 
      0 (srand) 
      1 (srand (first col))
      2 (+ (srand (apply - (reverse col))) (first col))
      (srand)
    :else (srand)))) more))



;https://gist.github.com/nasser/de0ddaead927dfa5261b
(defmacro schance [& body]
  (let [parts (partition 2 body)
        total (apply + (map first parts))
        rsym (gensym "random_")
        clauses (->> parts
                  (sort-by first (comparator >))
                  (reductions
                    (fn [[odds-1 _]
                        [odds-2 expr-2]]
                      [(+ odds-1 (/ odds-2 total)) expr-2])
                    [0 nil])
                  rest
                  (mapcat
                    (fn [[odds expr]]
                      [(or (= 1 odds) `(< ~rsym ~odds))
                       expr])))]
    `(let [~rsym (srand)]
       (cond ~@clauses))))




;TODO make noise configurable for user needs
'(* (noise (V* location 0.001)) 100)

'(noise :terrain {:in #(* 0.001) :out #(* % 100)})
'(noise :terrain location)

;TODO consider combinations of noise lookups 
'(def abc (apply juxt (map partial (repeat 3 noise) [:a :b :c])))


(def ^:private PN (atom {}))
(def ^:private PFN (atom {}))

(defn noise 
  ([k v] 
    (cond (vector3? v) (noise k (.x v) (.y v) (.z v))
          (vector2? v) (noise k (.x v) 0.0 (.z v))
          (vector? v) (noise k (or (get v 0) 0.0) 
                               (or (get v 1) 0.0) 
                               (or (get v 2) 0.0))
          (number? v) (noise k v 0.0 0.0)
          (fn? v) (swap! PFN assoc k v)))
  ([k x y] (noise k x y 0))
  ([k x y z]
  (let [pn (or (get @PN k) 
               (get (swap! PN assoc k (PerlinNoise. (srand))) k))]
    (if-let [pfn (get @PFN k)]
      (.Noise pn (float (pfn x)) (float (pfn y)) (float (pfn z)))
      (.Noise pn (float x) (float y) (float z))))))
