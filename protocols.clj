(ns hard.protocols)

(defprotocol Temporal
   (--update [a b c]))

(defprotocol Linkable
  (-link [a b])
  (linked [a]))

(defprotocol Validatable
  (valid? [o]))

(defprotocol Ratial
  (reset [o])
  (show-ratio [o r]))

(defprotocol Cloneable
  (clone [o]))

(defprotocol Targetable
  (target [o]))
