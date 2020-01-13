package model;

public class MemoryUtils {

    private static long initialMemory = getUsedMemory();
    private static long maxMemory = -1;

    private static long getUsedMemory() {
        return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
    }

    public static void measure() {
        long curr_memory = getUsedMemory();
        maxMemory = curr_memory > maxMemory ? curr_memory : maxMemory;
    }

    public static long getActualMaxUsedMemory() {
        return maxMemory - initialMemory;
    }
}
