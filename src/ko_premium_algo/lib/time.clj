(ns ko-premium-algo.lib.time
  (:import [java.time Instant ZoneId LocalDateTime Duration]
           [java.time.format DateTimeFormatter]))

(defn now []
  (Instant/now))

(defn parse [time-str pattern]
  (-> time-str
      (LocalDateTime/parse (DateTimeFormatter/ofPattern pattern))
      (.atZone (ZoneId/of "UTC"))
      .toInstant))

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

(defn time->millis [time]
  (.toEpochMilli time))

(defn millis->time [millis]
  (Instant/ofEpochMilli millis))

(defn time->iso8601 [time]
  (.toString time))

(defn iso8601->time [iso8601]
  (Instant/parse iso8601))

(defn hhmmss [iso8601]
  (subs iso8601 11 19))

(defn diff [time1 time2]
  (Duration/between time1 time2))

(defn hours [duration]
  (.toHours duration))

(defn days [duration]
  (.toDays duration))

(defn millis [duration]
  (.toMillis duration))
