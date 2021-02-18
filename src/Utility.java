import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Description : A collection of helpful methods which are often used throughout the program.
 * Note that all of these functions are indeed static, since it would be non sensical to have them otherwise.
 */
public final class Utility {
    /**
     * Description : I decided to keep the scanner static, since having multiple scanners reading the same input stream would
     * be detrimental, should I want to make this program multi-threaded.
     */
    private static Scanner input_scanner = new Scanner(System.in);

    /**
     * Description : Closes the scanner based on the System.in stream.
     */
    public static void CloseInputScanner() {
        input_scanner.close();
    }

    /**
     * Description : Obtains the user input from the scanner.
     * @return Returns the obtained user input.
     */
    public static String GetUserInput() {
        return input_scanner.nextLine();
    }

    /**
     * Description : Returns a random number between a lower and upper bound.
     *
     * @param lower_bound The lower bound of the distribution.
     * @param upper_bound The upper bound of the distribution.
     * @return The random number.
     */
    public static int RandomNumber(int lower_bound, int upper_bound) {
        return (int) (Math.random() * upper_bound) + 1;
    }

    /**
     * Description : Returns the Euclidean distance between two points.
     *
     * @param vec1 First position
     * @param vec2 Second position
     * @return Euclidean distance
     */
    public static double EuclideanDistance(Vector2 vec1, Vector2 vec2) {
        return Math.sqrt(Math.pow(vec2.x - vec1.x, 2) + Math.pow(vec2.y - vec1.y, 2));
    }

    /**
     * Description : Converts a cardinal character to the a Vector2.
     *
     * @param cardinal The cardinal character.
     * @return The resultant vector.
     */
    public static Vector2 CardinalToVector2(char cardinal) {
        switch (cardinal) {
            case 'N':
                return new Vector2(0, -1);
            case 'E':
                return new Vector2(1, 0);
            case 'S':
                return new Vector2(0, 1);
            case 'W':
                return new Vector2(-1, 0);
            default:
                return new Vector2(0, 0);
        }
    }

    /**
     * Description : Converts a Vector2 to a cardinal character.
     *
     * @param vec The vector to convert.
     * @return The cardinal direction as a result of the conversion.
     */
    public static char VectorToCardinal(Vector2 vec) {
        if (Vector2.Compare(new Vector2(0, -1), vec)) return 'N';
        else if (Vector2.Compare(new Vector2(1, 0), vec)) return 'E';
        else if (Vector2.Compare(new Vector2(0, 1), vec)) return 'S';
        else if (Vector2.Compare(new Vector2(-1, 0), vec)) return 'W';
        return ' ';
    }

    /**
     * @return Returns a Hashmap of each cardinal character and the Vector2 which corresponds to it.
     */
    public static HashMap<String, Vector2> GetCardinalVectors() {
        HashMap<String, Vector2> result = new HashMap<String, Vector2>() {
            {
                put("N", new Vector2(0, -1));
                put("E", new Vector2(1, 0));
                put("S", new Vector2(0, 1));
                put("W", new Vector2(-1, 0));
            }
        };
        return result;
    }

    /**
     * @return Description : Returns a unary predicate which checks whether or not an argument string is empty.
     */
    public static GeneralCallable<Boolean, String> IsEmptyString() {
        return (String param_arg) -> {
            return !param_arg.isEmpty();
        };
    }

    /**
     * Returns whether or not a point is contained within the rectangle bounded by the top_left and bottom_right coordinates.
     *
     * @param point             The point to check
     * @param top_left_rect     The top left coordinates of the rectangle
     * @param bottom_right_rect The bottom right coordinates of the rectangle.
     * @return Returns a boolean flag whether the coordinate was within the rectangle or not.
     */
    public static boolean Contains(Vector2 point, Vector2 top_left_rect, Vector2 bottom_right_rect) {
        return !(point.x < top_left_rect.x || point.x > bottom_right_rect.x || point.y < top_left_rect.y || point.y > bottom_right_rect.y);
    }

    /**
     * Description : Prints a string array of to the console.
     *
     * @param str_array The argument array.
     */
    public static void PrintStringArray(ArrayList<String> str_array) {
        str_array.forEach((String x) -> {
            System.out.println(x);
        });
    }

    /**
     * Description : Prints the input argument to the console.
     *
     * @param str : Input argument.
     */
    public static void PrintToConsole(String str) {
        System.out.println(str);
    }
    public static void print(String str)
    {
        System.out.println(str);
    }
}
