(ns ko-premium-algo.lib.time
  (:import [java.time Instant]
           [java.time Duration]))

(defn now [] 
  (Instant/now))

(defn make-duration [value unit]
  (cond
    (= unit "s") (Duration/ofSeconds value)
    (= unit "m") (Duration/ofMinutes value)
    (= unit "h") (Duration/ofHours value)
    (= unit "d") (Duration/ofDays value)))

(defn plus-duration [& durations]
  (reduce #(.plus %1 %2) durations))

(defn minus-duration [& durations]
  (reduce #(.minus %1 %2) durations))

(defn minus-time [time duration]
  (.minus time duration))

(defn plus-time [time duration]
  (.plus time duration))

(defn millis [time]
  (.toEpochMilli time))

(defn millis-to-time [millis]
  (Instant/ofEpochMilli millis))

(defn iso8601 [time]
  (.toString time))

(defn hhmmss [time]
  (subs (iso8601 time) 11 19))

(defn diff [time1 time2]
  (Duration/between time1 time2))

(defn hours [duration]
  (.toHours duration))

(defn days [duration]
  (.toDays duration))

