/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class Logger {
    static int serverIndex;

    public static void debug(String msg) {
        System.out.println("[" + serverIndex + "]" + msg);
    }

    public static void println(String msg) {
        System.out.println(msg);
    }
}
