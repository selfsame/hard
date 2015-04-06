 
(ns hard.boiled
	(:use hard.core)
	(:require 
	[clojure.string :as string])
  (:require arcadia.core)
  (:import [UnityEngine]))


 (defn hyphen->camel [charseq]
	(let [parted (partition-by #(= \- %) charseq)]
	(apply str (cons (apply str (first parted))
		(map 
		(fn [col]
			(cond (= '(\-) col) nil
				:else (str (string/upper-case (str (first col)))
						(apply str (rest col)))))
		(rest parted))))))

(defn kw->sym [kw]
	(symbol (hyphen->camel (rest (str kw)))))



;https://gist.github.com/nasser/de0ddaead927dfa5261b
(defmacro chance [& body]
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
    `(let [~rsym (rand)]
       (cond ~@clauses))))