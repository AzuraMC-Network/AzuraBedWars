package cc.azuramc.bedwars.config;

import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Value
public class ApplyResult {

    public enum Status {
        APPLIED,
        /** Stored, the running game keeps the old values */
        NEXT_GAME,
        /** Everything else applied, {@link #getRestartPaths()} keep their old values until a restart */
        RESTART_REQUIRED,
        /** Nothing changed, see {@link #getProblems()} */
        REJECTED
    }

    String documentId;
    Status status;
    List<ConfigProblem> problems;
    List<String> restartPaths;

    static ApplyResult rejected(String documentId, List<ConfigProblem> problems) {
        return new ApplyResult(documentId, Status.REJECTED, problems, Collections.<String>emptyList());
    }
}
