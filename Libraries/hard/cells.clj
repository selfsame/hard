(ns cells.clj
  (:use [hard.core :only [primitive clone destroy!]]))

(declare cube)

(def dirty (atom []))

(def rule30 
   {[true true true] nil
	[true true nil] nil
	[true nil true] nil
	[true nil nil] true
	[nil true true] true
	[nil true nil] true
	[nil nil true] true 
	[nil nil nil] nil})

(defn vis [[x z] v] 
	(when v (swap! dirty #(cons 
		(clone cube [(- x z) 0 z]) %))) v)

(defn aut [rule iters vis-fn]
  (loop [pass 1 prev [true]]
    (if (== pass iters) prev
	  (recur 
		(inc pass)
		(mapv 
		  #(vis-fn [% pass]
		    (get rule
			  [(get prev (- % 2))
			   (get prev (- % 1))
			   (get prev (+ % 0))]))
		  (range (+ 2 (count prev))))))))

(destroy! @dirty)

;(aut rule30 85 vis)



(defm)






