import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description : An easy to use,decentralised logging class for errors and general messages.
 */
public final class Logger {
    /**
     * Description : Log codes and their messages stored here. For instance {"CM10227" : "This unit is awesome!"}
     */
    //"G01" : "Unregistered Command"
    private static HashMap<String, String> log_codes;
    /**
     * Description : An array of all logs.
     */
    private static ArrayList<String> logs;

    /**
     * Description : Static initialisation of log codes.
     */
    public static void InitialiseLogCodes() {
        log_codes = new HashMap<>();
        logs = new ArrayList<>();
        Logger.RegisterLogCode("G01", "Unregistered command.");
        Logger.RegisterLogCode("G02", "Invalid command arguments.");
        Logger.RegisterLogCode("G03", "Negative argument for number for enemies / heroes.");
        Logger.RegisterLogCode("G04", "Could not find available spawning position. Please leave space for spawnable tiles..");

        Logger.RegisterLogCode("MM00", "Unable to open the map file of name ");
        Logger.RegisterLogCode("MM01", "Unable to parse/find critical map parameters. Ensure parameters are at the top of the file, and its arguments are of the correct type, as specified in ReadMe.txt.");
        Logger.RegisterLogCode("MM02", "Unregistered tile at position ");
        Logger.RegisterLogCode("MM03", "Invalid map dimensions. Ensure that your map has at least one row.");
    }

    /**
     * Description : Support for if a future programmer would want to initialise a log code externally.
     *
     * @param log_code    The log code, for instance "ABCD213".
     * @param log_message The log messsage, for instance "Vivaldi > Mozart".
     */
    public static void RegisterLogCode(String log_code, String log_message) {
        if (log_codes.containsKey(log_code)) return;
        log_codes.put(log_code, log_message);
    }

    /**
     * Description : Registers a given log, by looking up the log code.
     *
     * @param log_code               The reference log code.
     * @param additional_information Any additional information about the log.
     * @param print_to_console       Whether or not the log should be printed to the console.
     */
    public static String Log(String log_code, String additional_information, boolean print_to_console) {
        if (!log_codes.containsKey(log_code)) return "";
        logs.add(log_code);
//        if (!print_to_console) return log_codes.get
        String out = log_codes.get(log_code);
//        System.out.print(log_codes.get(log_code));
//        if (!additional_information.isEmpty()) System.out.print(" : " + additional_information);
        if(!additional_information.isEmpty()) out = out + additional_information;
//        System.out.print("\n");
        return out;
    }
}