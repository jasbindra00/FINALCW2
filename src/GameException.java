/**
 * Description : A custom exception class which derives from the Java exception.
 * This was design chosen to be able to express game specific errors.
 */
public class GameException extends Exception {
    /**
     * Description : Constructor with initialising message.
     * @param error_message The error message with which to initialise the exception.
     */
    public GameException(String error_message) {
        super(error_message);
    }
}
