(ns hard.seed)

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
