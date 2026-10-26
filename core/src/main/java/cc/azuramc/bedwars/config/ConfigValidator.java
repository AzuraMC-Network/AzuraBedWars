package cc.azuramc.bedwars.config;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Checks references between documents, run after binding and before a change is applied
 *
 * @author an5w1r@163.com
 */
public interface ConfigValidator {

    void validate(View view, Sink sink);

    /**
     * Current values with the document being checked replaced by its candidate
     */
    interface View {

        @NotNull
        <T> T get(Class<T> type);

        /**
         * Every document of a type in file order, keyed by document id
         */
        @NotNull
        <T> Map<String, T> all(Class<T> type);

        @NotNull
        String idOf(Class<?> type);
    }

    interface Sink {

        void error(String documentId, String path, String message);

        void warn(String documentId, String path, String message);
    }
}
