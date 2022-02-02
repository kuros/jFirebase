package in.kuros.jfirebase.entity;

public enum TemporalType {
    TIME,
    /**
     * Date without time.
     */
    DATE,
    /**
     * Date including time.
     */
    DATE_TIME,
    TIMESTAMP,
    /**
     * Parse as LocalDate without time.
     */
    LOCAL_DATE,
    /**
     * Parse as LocalDateTime.
     */
    LOCAL_DATE_TIME
}
