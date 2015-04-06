(ns hard.protocols)

(defprotocol IBumpEnter
	(OnBumpEnter [a b]))
(defprotocol IBumpEnter2D
	(OnBumpEnter2D [a b]))
(defprotocol IBumpExit
	(OnBumpExit [a b]))
(defprotocol IBumpExit2D
	(OnBumpExit2D [a b]))
(defprotocol IBumpStay
	(OnBumpStay [a b]))
(defprotocol IBumpStay2D
	(OnBumpStay2D [a b]))


(defprotocol IBumpTriggerEnter
	(OnBumpTriggerEnter [a b]))
(defprotocol IBumpTriggerEnter2D
	(OnBumpTriggerEnter2D [a b]))
(defprotocol IBumpTriggerExit
	(OnBumpTriggerExit [a b]))
(defprotocol IBumpTriggerExit2D
	(OnBumpTriggerExit2D [a b]))
(defprotocol IBumpTriggerStay
	(OnBumpTriggerStay [a b]))
(defprotocol IBumpTriggerStay2D
	(OnBumpTriggerStay2D [a b]))