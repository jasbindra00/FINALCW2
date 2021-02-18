/**
 * Description : A wrapper class holding a map argument, as well as some information about the parameter.
 * This class ensures that each argument is properly initialised, automatically. This makes it incredibly easy for any future programmers to add any map parameters to their own maps, which is great for modularity and longevity.
 */
public final class MapParameterChecker {
    /**
     * Description : The lack of initalisation of a non essential parameter is not fatal to the program.
     */
    private final boolean is_essential;
    /**
     * Description : Callable which checks the argument of the parameter.
     */
    private GeneralCallable<Boolean, String> argument_checker;
    /**
     * Description : Stored parameter argument.
     */
    private String parameter_argument;

    /**
     * Constructor initialising the object.
     *
     * @param essential     Whether the parameter is essential or not. For instance, 'win' is an essential parameter.
     * @param default_value The default value of the parameter.
     * @param checker       The callable associated with checking a value before initalising the argument.
     */
    public MapParameterChecker(boolean essential, String default_value, GeneralCallable<Boolean, String> checker) {
        this.parameter_argument = default_value;
        this.is_essential = essential;
        this.argument_checker = checker;
    }

    /**
     * @return Whether or not this parameter is essential or not.
     */
    public Boolean IsEssential() {
        return this.is_essential;
    }

    /**
     * Description : Tries to set the parameter argument with the input string.
     *
     * @param arg The input which may possibly become the new argument
     * @return Whether or not the initialisation was successful.
     */
    public boolean SetParameterArgument(String arg) {
        //Explanation : If it failed the argument checker test, then it cannot be an argument.
        if (!this.argument_checker.run(arg)) return false;
        parameter_argument = arg;
        return true;
    }

    /**
     * @return Returns the string argument as a copy, so as to promote encapsulation.
     */
    public String GetArgument() {
        return new String(this.parameter_argument);
    }

}
