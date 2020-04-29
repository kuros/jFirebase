package in.kuros.jfirebase.util;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomCollectors {

    public static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> toMap(final Function<? super T, K> keyMapper,
                                                                     final Function<T, V> valueMapper) {
        return new CustomCollectorImpl<>(HashMap::new,
                (kvMap, t) -> kvMap.put(keyMapper.apply(t), valueMapper.apply(t)),
                (kvMap, kvMap2) -> {
                    kvMap.putAll(kvMap2);
                    return kvMap;
                },
                Function.identity(),
                Collector.Characteristics.IDENTITY_FINISH);
    }

    public static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> toMap(final Function<? super T, K> keyMapper,
                                                                     final Function<T, V> valueMapper,
                                                                     final BinaryOperator<V> mergeFunction) {
        return new CustomCollectorImpl<>(HashMap::new,
                (kvMap, t) -> {
                    final K key = keyMapper.apply(t);
                    kvMap.put(key, mergeFunction.apply(kvMap.get(key), valueMapper.apply(t)));
                },
                (kvMap, kvMap2) -> {
                    kvMap.putAll(kvMap2);
                    return kvMap;
                },
                Function.identity(),
                Collector.Characteristics.IDENTITY_FINISH);
    }

    @RequiredArgsConstructor
    static class CustomCollectorImpl<T, A, R> implements Collector<T, A, R> {

        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Characteristics characteristics;

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Sets.newHashSet(characteristics);
        }
    }
}
