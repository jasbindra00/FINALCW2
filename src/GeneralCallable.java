/**
 * Description : A simple callable interface which wraps a method accepting any one argument, and returning anything.
 *
 * @param <RETURN_TYPE> The type of the return.
 * @param <INPUT_TYPE>  The type of the input.
 */

public interface GeneralCallable<RETURN_TYPE, INPUT_TYPE> {
    /**
     * Description : A general callable which can bind to anything with any return and input type.
     * @param arg : Input argument.
     * @return The return type of the callable.
     */
    RETURN_TYPE run(INPUT_TYPE arg);
}