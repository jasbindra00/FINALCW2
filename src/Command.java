import java.util.ArrayList;
import java.util.HashSet;

/**
 * Description : Wrapper class around ParameterChecker, used to validate the user input arguments for a given command.
 * This class utilises a very simple observer / subscription pattern, which is very suitable for the command chain of responsibility architecture that I have chosen.
 * <p>
 * Key points :
 * Checks:
 * -If the user input has the correct number of arguments.
 * -If the user input has the correct arguments.
 * -If the player who is requesting the execution of this command is actually permitted to call this command. (Simple Observer pattern).
 * -If all the above points are met, the registered command callable is executed, and its result returned.
 */

public class Command<CALLABLE_RETURN>
{
    /**
     * Description : The callable which does the checking of the arguments. This predicate is fully modular in that its to the user's discretion
     * to determine how exactly a parameter is validated.
     */
    protected GeneralCallable<Boolean, ArrayList<String>> parameter_checker;
    /**
     * Description : The number of arguments required for the command to execute. In this assignment, 'MOVE N E' is an invalid command, but we can easily accomodate it
     * by simply changing the number of critical arguments.
     */
    protected int critical_arguments;
    /**
     * Description : A set of the accepted arguments. For the MOVE command, this will look like {"N", "E", "S", "W"}.
     * A set is an obvious choice for this since we would not like repeats.
     */
    protected HashSet<String> accepted_arguments;

    /**
     * Description : The callable which is executed once all of the pre-exquisite command parameters have been met. For instance, a 'MOVE N' command by a hero will
     * call Game::Move, with the arguments passed in.
     */
    protected GeneralCallable<CALLABLE_RETURN, ArrayList<String>> execute_command;
    /**
     * Description : The callable which is executed when a user input did not meet the execution requirements for this command OR if an unsubscribed player
     * has requested the execution of this command.
     */
    protected GeneralCallable<Void, Void> execute_failure;

    /**
     * Description : Constructor initialising the object attributes.
     *
     * @param accepted_arguments   A set of the arguments accepted by this command. The user input arguments will be checked against this set.
     * @param n_critical_arguments The exact number of arguments required for the command to execute.
     * @param cmd_callable         The callable which will execute when the correct arguments have been passed into the object.
     */
    public Command(HashSet<String> accepted_arguments, int n_critical_arguments, GeneralCallable<CALLABLE_RETURN, ArrayList<String>> cmd_callable) {
        this.critical_arguments = n_critical_arguments;
        this.accepted_arguments = accepted_arguments;
        this.execute_command = cmd_callable;
        //Explanation : The default parameter checker simply iterates through the user input arguments, and checks if any of the arguments are not accepted. If so, the request has failed.
        this.parameter_checker =
                (ArrayList<String> user_arguments) ->
                {
                    for (int i = 1; i < user_arguments.size(); ++i) {
                        for (String accepted_argument : this.accepted_arguments) {
                            if (user_arguments.get(i).equals(accepted_argument)) return true;
                        }
                        return false;
                    }
                    return true;
                };
    }

    /**
     * Description : Registration of the callable to be executed when the player input was not validated by the command.
     * @param failure_callable The callable to register.
     */
    public void RegisterFailureCallable(GeneralCallable<Void, Void> failure_callable) {
        this.execute_failure = failure_callable;
    }

    /**
     * Description : Facilities the ability to override the default callable associated with validating the input arguments.
     *
     * @param checker The new unary predicate.
     */
    public void ChangeParameterChecker(GeneralCallable<Boolean, ArrayList<String>> checker) {
        this.parameter_checker = checker;
    }

    /**
     * Description : This method is called when a player is requesting the execution of this command with the user input.
     * The user input may be complete garbage, or it could contain the invalid or incorrect number of arguments. It's even possible
     * that an Enemy is trying to call the "PICKUP" command! This method does all of the checking.
     *
     * @param player_index             The index of the player who is requesting the execution of the command. This is required as we need to check if this player is even subscribed to this command.
     * @param print_success_to_console The string result of the command callable is outputted to the console if and only if the requesting player is a human.
     * @param user_input               The user input split into an array list.
     * @return A flag indicative of the operation status.
     */
    public CALLABLE_RETURN ExecuteCommand(int player_index, boolean print_success_to_console, ArrayList<String> user_input) {
        //Explanation : Check to see if the player and input qualifies for the execution of the command.
        //Explanation : Only subscribed players can execute the command.
        if ((user_input.size() - 1 == this.critical_arguments && (user_input.size() == 1 || this.parameter_checker.run(user_input)))) {
            //Explanation : The command arguments have passed all of the tests, and the correct player has called the command.
            //Explanation : Remove the command name (indexed at 0). This is now a list ONLY of command arguments, which is in the format for our command callables.
            return this.execute_command.run(user_input);
        }
        //Explanation : Execute the failure callable if it's registered.
        if (this.execute_failure != null) this.execute_failure.run(null);
        return null;
    }

}
