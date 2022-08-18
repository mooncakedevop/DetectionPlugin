package com.glcc;

import java.util.List;
import java.util.Map;

/**
 * @author xc
 * @time 19-3-1.
 */
public class DetectionExtension {
    public Map<String, List<String>> hookPoint;

    public Map<String, String> exceptionHandler;

    @Override
    public String toString() {
        return "AddTryCatchExtension{" +
                "hookPoint=" + hookPoint +
                ", exceptionHandler=" + exceptionHandler +
                '}';
    }
}
