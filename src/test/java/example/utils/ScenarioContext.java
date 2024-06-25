package example.utils;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void setContext(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object getContext(String key) {
        return context.get().get(key);
    }

    public static void clearContext() {
        context.get().clear();
    }

}
