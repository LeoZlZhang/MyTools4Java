package leo.tools.not_organized;

import java.util.Optional;
import java.util.function.Function;

public class NpeHelper {
    public static <T> T ensure(T t) {
        return Optional.ofNullable(t).orElseThrow(ExcepUtil::valueIsNull);
    }

    public static <T, R> R ensure(T t, Function<T, R> map) {
        return Optional.ofNullable(t).map(map).orElseThrow(ExcepUtil::valueIsNull);
    }

    public static final String EMPTY = "";
}
