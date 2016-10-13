(ns hard.seed
  (:use hard.core
    arcadia.linear))

(defn seed! [v] (set! UnityEngine.Random/seed (hash v)))

(defn srand 
  ([] UnityEngine.Random/value)
  ([n] (* n (srand))))

(defn srand-int [n] (int (* (srand) n)))

(defn srand-nth [col] (get col (srand-int (count col))))

(defn sshuffle [col] (sort-by (fn [_](srand)) col))

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


(defn noise* 
  ([] (noise* 0 {}))
  ([seed] (noise* seed {}))
  ([seed opts]
    (let [i (PerlinNoise. (hash seed))
          {:keys [in out]} opts
          nf (fn [v] (.Noise i (double (.x v)) (double (.y v) ) (double (.z v))))]
      (apply comp (filter fn? [out nf in])))))

(defn harmonic [f mags f2] (fn [v] (reduce f2 (map #(f (v3* v %)) mags))))

(defn list-reduce [a b] (if (seq? a) (cons b a) (list b a)))


