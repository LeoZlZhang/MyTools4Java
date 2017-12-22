package leo.tools.not_organized;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static leo.tools.not_organized.NpeHelper.ensure;

public class CacheKit {
    private static final String DEFAULT_NAMESPACE = "dux";
    private Map<String, Map<String, Object>> caches = new HashMap<>();

    private static CacheKit self = new CacheKit();

    private CacheKit() {
    }

    public static CacheKit me(){
        return self;
    }

    public void put(String nameSpace, String key, Object value) {
        Map<String, Object> data = Optional.ofNullable(caches.get(nameSpace)).orElseGet(HashMap::new);
        data.put(key, value);
        caches.put(nameSpace, data);
    }

    public void put(String key, Object value) {
        put(DEFAULT_NAMESPACE, key, value);
    }

    public Optional<Object> get(String nameSpace, String key) {
        return Optional.of(caches).map(ca -> ca.get(nameSpace)).map(data -> data.get(key));
    }

    public Optional<Object> get(String key) {
        return get(DEFAULT_NAMESPACE, key);
    }

    public <T> Optional<T> get(String nameSpace, String key, Class<T> cls) {
        return get(nameSpace, key).map(cls::cast);
    }

    public <T> Optional<T> get(String key, Class<T> cls){
        return get(DEFAULT_NAMESPACE, key, cls).map(cls::cast);
    }

    public void clearByKey(String nameSpace, String key){
        Optional.of(caches.get(nameSpace)).ifPresent(data-> data.remove(key));
    }

    public void clearByKey(String key){
        clearByKey(DEFAULT_NAMESPACE, key);
    }

    public void clearByNameSpace(String nameSpace){
        caches.remove(nameSpace);
    }

    public void refreshByKey(String nameSpace, String key, Supplier<Object> generator){
        ensure(nameSpace);
        ensure(key);
        ensure(generator);
        clearByKey(nameSpace, key);
        put(nameSpace, key, generator.get());
    }

    public void refreshByKey(String key, Supplier<Object> generator){
        refreshByKey(DEFAULT_NAMESPACE, key, generator);
    }
}
