package pe.edu.upc.agroleak.common.domain;
import java.time.*;
/** UTC half-open interval [from,to), bounded to keep interactive queries predictable. */
public record TimeRange(Instant from, Instant to) {
    public TimeRange {
        if (from == null || to == null || !from.isBefore(to)) throw new IllegalArgumentException("from debe ser anterior a to");
        if (Duration.between(from,to).compareTo(Duration.ofDays(31)) > 0) throw new IllegalArgumentException("El rango maximo es 31 dias");
    }
    public static TimeRange of(Instant from, Instant to) {
        Instant end = to == null ? Instant.now() : to;
        return new TimeRange(from == null ? end.minus(Duration.ofHours(24)) : from, end);
    }
    public static int limit(int limit) {
        if (limit < 1 || limit > 1000) throw new IllegalArgumentException("limit debe estar entre 1 y 1000");
        return limit;
    }
}
