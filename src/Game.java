import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Description : -This is the main logic of the Dungeons of Doom game.
 * -The game class brings together the primitive helper classes together and bespokely ties them all together.
 * <p>
 * Key Points:
 * -Holds all players.
 * -Holds an instance to the map manager.
 * -Holds all registered commands.
 */
public final class Game {
    /**
     * Description : The has the choice of loading the default map if his load attempt exceeds a threshold.
     */
    private static final String DEFAULT_MAP_NAME = new String("leon_watts_map.txt");
    /**
     * Description : This boolean is meant for you, the examiner. I understand that you have many courseworks to mark, and so I have
     * made a small change to make your life easier. If this boolean is toggled, you are able to play the game in debug mode, wherein
     * useful information, such as the entire map and other metrics will be printed out.
     */
    private boolean EXAMINER_DEBUG_MODE = false;
    /**
     * Description : The dimensions of the field of vision shared by all players.
     */
    /**
     * Description : An instance of the map manager, which loads and handles the tile map.
     */
    private MapManager map_manager = new MapManager();
    /**
     * Description : An array holding polymorphic Player objects.
     */
    private ArrayList<Player> players = new ArrayList<Player>();
    /**
     * Description : A counter which keeps track of whose turn it is by referencing the player array index.
     */
    private int current_player_index = 0;
    /**
     * Description : This boolean is toggled TO on while the game is running, and back to an off state when the game is lost.
     */
    private boolean game_over = false;
    /**
     * Description : A mapping from a string to a command checker.
     * A hashmap was chosen as opposed to an array, for ease of use, intuition, and for longevity purposes.
     */
    private HashMap<String, CommandInfo> game_commands = new HashMap<String, CommandInfo>();

    /**
     * Description : Default constructor registering all necessary map parameters, and commands.
     * Initalisation is deferred to the point at which the map is successfully loaded.
     * @param EXAMINER_DEBUG_MODE Flag indicating whether or not the the examiner debug mode should be turned on.
     */
    public Game(boolean EXAMINER_DEBUG_MODE) {
        //Explanation : Initialise the examiner debug mode.
        this.EXAMINER_DEBUG_MODE = EXAMINER_DEBUG_MODE;
        /*
            Description : Registering the parameters required for our future maps. Note that this flexible design allows for more parameters to be added in the future.
            The lambda expression is a simple checker callable which validates a parameter which has been read from the file.
        */

        //Explanation : All maps in dungeons of doom require the essential non empty 'name' parameter.
        map_manager.RegisterParameter("name", true, null,
                //Explanation : The map name has to be a non empty string.
                Utility.IsEmptyString());

        //Explanation : All maps in dungeons of doom require the essential 'win' parameter which details the amount of ggold.
        //The checker performs a cast to the integer, and checks if the result is positive, to determine the validity.
        map_manager.RegisterParameter("win", true, null,
                (String param_arg) ->
                {
                    //Explanation : The gold count must be a positive integer.
                    try {
                        return Integer.parseInt(param_arg) > 0;
                    }
                    //Explanation : The user has entered a gold argument which is not cast-able to an integer.
                    catch (NumberFormatException e) {
                        return false;
                    }
                });
        //Explanation : Initialising the standard dungeons of doom tiles. These can be controlled by the user in the text file.
        //The predicate is simply a non empty string checker.
        map_manager.RegisterParameter("default_tile", false, "#", Utility.IsEmptyString());
        map_manager.RegisterParameter("wall_tile", false, "#", Utility.IsEmptyString());
        map_manager.RegisterParameter("fill_tile", false, ".", Utility.IsEmptyString());
        map_manager.RegisterParameter("spawnable_tiles", false, "E.", Utility.IsEmptyString());
        map_manager.RegisterParameter("gold_tile", false, "G", (String arg) -> {
            return arg.length() != 1;
        });
        map_manager.RegisterParameter("exit_tile", false, "E", (String arg) -> {
            return arg.length() != 1;
        });
        //Explanation : Registering each command into our string-callable hashmap.
        RegisterCommand("MOVE", new HashSet<String>(Arrays.asList("N", "E", "S", "W")), 1, (ArrayList<String> cmd_args) -> {
            return this.Move(cmd_args);
        });
        //Description : The specification requires us to print "Fail." upon an invalid move, and thus we must register the failure callable.
        this.game_commands.get("MOVE").RegisterFailureCallable((Void) -> {
            Utility.PrintToConsole("Fail");
            return null;
        });
        //Description : You will notice that there is no registered failure callable for the "PICKUP" command. That's because the CommandInfo class only checks
        //if the COMMAND ITSELF is in the correct format. So although the user enters "PICKUP" on a tile with no gold, the command is correct,
        //but its a failure independant of the format of the command (since it's map related).
        RegisterCommand("PICKUP", new HashSet<String>(), 0, (ArrayList<String> cmd_args) -> {
            return this.Pickup(cmd_args);
        });
        RegisterCommand("GOLD", new HashSet<String>(), 0, (ArrayList<String> cmd_args) -> {
            return this.Gold(cmd_args);
        });
        RegisterCommand("LOOK", new HashSet<String>(), 0, (ArrayList<String> cmd_args) -> {
            return this.Look(cmd_args);
        });
        RegisterCommand("QUIT", new HashSet<String>(), 0, (ArrayList<String> cmd_args) -> {
            return this.Quit(cmd_args);
        });
        RegisterCommand("HELLO", new HashSet<String>(), 0, (ArrayList<String> cmd_args) -> {
            return this.Hello(cmd_args);
        });
    }

    /**
     * Description : Prints useful information to the console to make the examiners life easier.
     */
    private void EXAMINER_DEBUG_MODE() {
        Utility.PrintToConsole("@@@@@@@@@@@@@@@START EXAMINER DEBUG MODE@@@@@@@@@@@@@@@");
        //Explanation : Print the entire map with map.
        ArrayList<String> map_array = this.map_manager.GetTileArray();
        for (int y = 0; y < map_array.size(); ++y) {
            for (int x = 0; x < map_array.get(0).length(); ++x) {
                for (final Player player : this.players) {
                    StringBuilder new_row = new StringBuilder(map_array.get(y));
                    if (!Vector2.Compare(new Vector2(x, y), player.GetPosition())) continue;
                    new_row.setCharAt(x, player.GetToken());
                    map_array.set(y, new_row.toString());
                }
            }
        }
        Utility.PrintStringArray(map_array);
        //Explanation : Print the positions of each player, and their vision.
        for (int i = 0; i < this.players.size(); ++i) {
            final Player current_player = this.players.get(i);
            current_player_index = i;
            Vector2 player_position = current_player.GetPosition();
            String player_type = (current_player.IsHuman()) ? ("HERO INFO") : ("ENEMY INFO");
            String pos_string = "POSITION : [" + Integer.toString(player_position.x) + "," + Integer.toString(player_position.y) + "]";
            Utility.PrintToConsole("\n@@ " + player_type + " @@@");
            Utility.PrintToConsole(pos_string);
            if (current_player.IsHuman()) {
                Utility.PrintToConsole(Gold(new ArrayList<>()));
                Utility.PrintToConsole(Hello(new ArrayList<>()));
            }
            MapSubset player_subset = GetSubset(player_position, current_player.GetFieldOfVisionDimensions());
            Utility.PrintStringArray(player_subset.map_subset);
            Utility.PrintToConsole("@@@ END_INFO @@@\n");
        }

        Utility.PrintToConsole("@@@@@@@@@@@@@@@END EXAMINER DEBUG MODE@@@@@@@@@@@@@@@\n");
    }

    /**
     * @param n_enemies The number of enemies to create.
     * @param n_heroes  The number of heroes to create.
     * @throws GameException This throws a game execption since it is possible that a random position cannot be found for each player.
     */
    private void RegisterPlayers(int n_enemies, int n_heroes) throws GameException {
        //Explanation : Negative number of players.
        if (n_enemies <= 0 || n_heroes <= 0) throw new GameException("G03");
        //Explanation : Try to register each hero and enemy.
        for (int j = 0; j < n_heroes; ++j) {
            RegisterHero();
        }
        for (int i = 0; i < n_enemies; ++i) {
            RegisterEnemy();
        }
    }

    /**
     * Description : Obtains a reference to the currently active player.
     *
     * @return Reference to the current player object.
     */
    private Player GetCurrentPlayer() {
        return this.players.get(current_player_index);
    }

    /**
     * Description : Produces a random, spawnable position from our map array. In Dungeons of Doom, the spawnable tiles are "E."
     * It is possible that the user has entered a map of ONLY non-traversable tiles. Therefore, I have decided to set a limit on
     * the number of attempts that a random position should be generated. If a position was not found after these iterations, then
     * a game exception is thrown since the map is too densely populated with non-traversable tiles.
     *
     * @return The final position.
     * @throws GameException The exception (if applicable) describing how the non-traversable-tile density needs to be reduced for a position to be found.
     */
    private Vector2 RandomValidPosition() throws GameException {
        //Explanation : The random position should be bounded by our map dimensions.
        final String spawnable_tiles = this.map_manager.GetParameterArgument("spawnable_tiles");
        Vector2 map_dim = this.map_manager.GetMapDimensions();
        for (int i = 0; i < map_dim.x * map_dim.y; ++i) {
            //Generate a number between 0 and the max dimension of the map.

            Vector2 random_position = new Vector2(Utility.RandomNumber(0, map_dim.x - 1), Utility.RandomNumber(0, map_dim.y - 1));
            //Check if this tile is a exit of . tile.
            if (spawnable_tiles.indexOf(map_manager.GetTile(random_position)) == 1) {
                if (players.isEmpty()) return random_position;
                for (final Player player : players) {
                    if (Vector2.Compare(player.GetPosition(), random_position)) continue;
                    return random_position;
                }
            }
        }
        throw new GameException("G04");
    }

    /**
     * Description : Requests a subset from the map manager, and fills the subset in with the relevant player tokens.
     *
     * @param centre The centre on which the subset should be based.
     * @param size   The dimensions of the subset.
     * @return The object containing information about the subset.
     */
    private MapSubset GetSubset(Vector2 centre, Vector2 size) {
        //Explanation : Obtain the raw, tile-only subset from the map manager (the map manager has no access to the players)
        MapSubset subset = map_manager.GetMapSubset(centre, size);
        //Explanation : Iterate through each player and substitute their tokens into the subset, if they exist within it.
        for (final Player player : players) {
            //Explanation : Convert the player position to the subset's local coordinates.
            Vector2 player_pos = player.GetPosition();
            if (!Utility.Contains(player_pos, subset.subset_world_top_left, subset.subset_world_bottom_right)) continue;
            //Explanation : Convert player position into local subset coordinates.
            player_pos = subset.WorldToSubsetCoord(player_pos);
            //Explanation : Substitute the player token in for the tile at his position within the subset.
            StringBuilder new_row = new StringBuilder(subset.map_subset.get(player_pos.y));
            new_row.setCharAt(player_pos.x, player.GetToken());
            //Explanation : Save the changes to the subset row.
            subset.map_subset.set(player_pos.y, new_row.toString());
        }
        return subset;
    }

    /**
     * Description : Determines whether or not a game will end as a result of the current player moving onto an opposing player.
     * @return Whether or not the game is lost from two opposing players.
     */
    private boolean CheckLoss() {
        for (int player1 = 0; player1 < players.size(); ++player1) {
            for (int player2 = player1 + 1; player2 < players.size(); ++player2) {
                Player ply1 = players.get(player1);
                Player ply2 = players.get(player2);
                //Explanation : The game is lost when an enemy and a non enemy meet.
                if (Vector2.Compare(ply1.GetPosition(), ply2.GetPosition()) && (ply1.IsHuman() && !ply2.IsHuman() || !ply1.IsHuman() && ply2.IsHuman()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Description : Command callable responsible handling for the "LOOK" command request, which requests the subset centred on the current player, and executes the relevant action depending on which player type called it.
     *
     * @param details Considering longevity, a future programmer may want to add additional arguments for this command.
     * @return The verdict of the command.
     */
    private String Look(ArrayList<String> details) {
        Player current_player = GetCurrentPlayer();
        //Description : Obtain the subset centred on the current player.
        MapSubset map_subset = GetSubset(current_player.GetPosition(), current_player.GetFieldOfVisionDimensions());
        //Description : If a human requested this command, then the specification demands us to print the subset to the command line.
        if (current_player.IsHuman()) {
            StringBuilder map_string = new StringBuilder();
            for (String row : map_subset.map_subset) {
                map_string.append(row + "\n");
            }
            return map_string.toString();
        }
        //Description : Otherwise, it was an enemy that requested this command. Update his vision map.
        else {
            ((Enemy) current_player).UpdateVisionMap(map_subset);
            return new String();
        }
    }

    /**
     * Description : Command callable responsible for the "PICKUP" request which attempts to pick the tile up at the current player's position.
     *
     * @param details Considering longevity, a future programmer may want to add additional arguments for this command.
     * @return The verdict of the command.
     */
    private String Pickup(ArrayList<String> details) {
        Hero current_player = (Hero) (GetCurrentPlayer());
        Vector2 player_position = current_player.GetPosition();

        //Description : Attempt to pick up the gold.
        String res = new String();
        if (map_manager.TryPickupGold(player_position)) {
            res = "Success.";
            current_player.IncrementGold();
        } else res = "Fail.";
        return res.toString() + " " + this.Gold(new ArrayList<>());
    }

    /**
     * Description : Command callable which is responsible for handling the "MOVE" command request.
     *
     * @param cmd_args Considering longevity, a future programmer may want to add additional arguments for this command.
     * @return The verdict of the command.
     */
    private String Move(ArrayList<String> cmd_args) {
        /*
            Explanation : A future programmer may want to allow diagonal traversal, in which case
            the command may look like "MOVE N E". This general function facilitates this.
         */
        Player current_player = GetCurrentPlayer();
        Vector2 resultant_position = current_player.GetPosition();
        //Explanation : Iterate through each of the cardinal arguments, and update the position.
        for (String argument : cmd_args) {
            resultant_position.Add(Utility.CardinalToVector2(argument.charAt(0)));
            if (map_manager.GetNonTraversableTiles().indexOf(map_manager.GetTile(resultant_position)) != -1)
                return ("Fail");
        }
        //Explanation : The movement argument(s) is/were valid. Update the position of the player.
        GetCurrentPlayer().SetPosition(resultant_position);
        return "Success";
    }

    /**
     * Description : Command callable which is responsible for handling the "GOLD" command request.
     *
     * @param command Considering longevity, a future programmer may want to add additional arguments for this command.
     * @return The verdict of the command.
     */
    private String Gold(ArrayList<String> command) {
        return "Gold Owned: " + Integer.toString(((Hero) (GetCurrentPlayer())).GetGoldCount());
    }

    /**
     * Description : Command callable which is responsible for the "QUIT" command request.
     *
     * @param command Considering longevity, a future programmer may want to add additional arguments for this command.
     * @return The verdict of the command.
     */
    private String Quit(ArrayList<String> command) {
        game_over = true;
        //Explanation : For the game to be won, each human player must be on an exit tile, and have enough gold.
        for (int i = 0; i < this.players.size(); ++i) {
            if (!players.get(i).IsHuman()) continue;
            final Hero current_player = (Hero) players.get(i);
            if (map_manager.GetTile(current_player.GetPosition()) != map_manager.GetParameterArgument("exit_tile").charAt(0) || current_player.GetGoldCount() < Integer.parseInt(map_manager.GetParameterArgument("win")))
                return new String("LOSE");
        }
        return new String("WIN\n Confutatis Maledictis.");
    }

    /**
     * Description : Command callable which is responsible for the "HELLO" command request.
     *
     * @param command Considering longevity, a future programmer may want to add additional arguments for this command.
     * @return The verdict of the command.
     */
    private String Hello(ArrayList<String> command) {
        return new String("Gold to win: " + Integer.toString(Integer.parseInt(map_manager.GetParameterArgument("win")) - ((Hero) (GetCurrentPlayer())).GetGoldCount()));
    }

    /**
     * Description : The main update loop, which executes the action of each player, while checking for losses.
     */
    public ArrayList<String> Update(String user_input) {
        //We want to execute all of the actions for the hero, and log everything that he should see. Ie the response of all of his commands, as well as if he as won or lost.
        //Explanation : Iterate through each player, ask for their commands, and try to execute their command.
        ArrayList<String> res = new ArrayList<>();
        ((Hero)this.players.get(0)).setPlayerAction(user_input);
        for (int i = 0; i < players.size(); ++i) {
            Player player_entity = players.get(i);
            this.current_player_index = i;
            //Explanation : Ask for the player's command.
            ArrayList<String> cmd_args = player_entity.GetPlayerAction();
            //Explanation : An empty command is moot.
            if (cmd_args.isEmpty()) continue;
            //Explanation : Check to see if this command has been registered.
            final String command_name = cmd_args.get(0);
            if (!game_commands.containsKey(command_name)) {
                //Explanation : The user input entered an invalid command.
                Logger.Log("G01", "", false);
                continue;
            }
            /*
                Explanation : Try to execute the command. Note that a precise number and type of arguments is required by the game command, as outlined previously.
                If the command was successfully read, then the callable executes automagically.
             */
            String game_command_string = game_commands.get(command_name).ExecuteCommand(current_player_index, player_entity.IsHuman(), cmd_args);
            if(game_command_string.isEmpty())
            {
                Logger.Log("G02", "", false);
                continue;
            }
            if(player_entity.IsHuman()) res.add(game_command_string);
            /*
                Explanation : Putting the check loss here instead of within the MOVE command reduces the otherwise tight coupling between them.
                In the future, it is possible that a future programmer may want to add another way in which the game can be lost, which could possibly be
                independent of the MOVE command. It is now easy to change the way in which the game is lost, since you don't have to worry about
                changing the contents of the MOVE function. Because that's all that MOVE should do.. It shouldn't be checking for losses.
                In addition, this is concurrent with the Single Responsibility Principle in software design.
             */
            //Explanation : Check if the position of the current player is on that of an opposing player. If so, the game is lost.
            if (CheckLoss()) {
                game_over = true;
                Utility.PrintToConsole("LOSS");
                res.add("LOSS");
                return res;
            }
        }
        //Explanation : Reset the current turn index.
        current_player_index = 0;
        if (this.EXAMINER_DEBUG_MODE) this.EXAMINER_DEBUG_MODE();
        return res;
    }

    /**
     * Description : The game is updated while the game is not over.
     * @return Whether or not the game is over.
     */
    public Boolean IsGameOver() {
        return game_over;
    }

    /**
     * Description : Tries to initialise the game with a custom number of both enemies and players, as well as a map file name,
     *
     * @param n_enemies The number of enemies to create.
     * @param n_heroes  The number of heroes to create.
     */
    public void Load(int n_enemies, int n_heroes) {
        //Explanation : If the user enters a wrong map a certain number of times, then he will be asked whether or not he would like to load a default map.
        final int max_fail_counter = 3;
        //Explanation : Helper lambda to try and load the file.
        GeneralCallable<Boolean, String> process_load = (String map_name) ->
        {
            if (map_manager.ReadMap(map_name, this.EXAMINER_DEBUG_MODE))
                //Explanation : The map read was successful.
                //Explanation : Try to register the players.
                try {
                    RegisterPlayers(n_enemies, n_heroes);
                    //Explanation : Player registration successful.
                    return true;
                }
                //Explanation : Error, relay to logger using exception log code.
                catch (GameException exception) {
                    Logger.Log(exception.toString(), "", true);
                }
            return false;
        };
        process_load.run("D:\\FILES\\Desktop\\Semester Two\\CM10228\\Coursework\\Chat Client\\jb2865-CW1\\src\\large_example_map.txt");
    }

    /**
     * Description : Short helper function which registers an enemy player into the array.
     * With this architecture, you can create as many enemies as you want (given that the size of your map can accomodate for them).
     *
     * @throws GameException This function makes a call to the random position generator, which is a throwing function, as it is possible that a position cannot be found in time.
     */
    public void RegisterEnemy() throws GameException {
        //Explanation : Create and add the enemy to the players array. The vision of the enemy must be initialised.
        Vector2 random_position = this.RandomValidPosition();
        Enemy enemy = new Enemy('B', random_position, new Vector2(5, 5), new GreedyEuclideanSearch('P'));
        players.add(enemy);
        enemy.UpdateVisionMap(GetSubset(random_position, enemy.GetFieldOfVisionDimensions()));
        //Explanation : Register a subscription of the current enemy to the "LOOK" and "MOVE" commands.
        int enemy_index = players.size() - 1;
        game_commands.get("LOOK").Subscribe(enemy_index);
        game_commands.get("MOVE").Subscribe(enemy_index);
    }

    /**
     * Description : A helper function which registers a hero in the game.
     * Note that with this architecture, you can add as many heroes as you want.
     *
     * @throws GameException This function makes a call to the random position generator, which is a throwing function, as it is possible that a position cannot be found in time.
     */
    public void RegisterHero() throws GameException {
        //Explanation : Add the hero to the array, with a valid positition, and a field of vision of 5x5
        Vector2 pos = new Vector2(5, 5);
        players.add(new Hero('P', this.RandomValidPosition(), new Vector2(5, 5)));
        //Explanation : Register subscriptions for all commands, for this hero.
        int player_index = players.size() - 1;
        //Explanation : Subscribe the hero to each available command.
        this.game_commands.forEach((key, value) -> value.Subscribe(player_index));
    }

    /**
     * Description : Helper function which allows us to register a command within our hashmap.
     *
     * @param command_name       The name of the command to be registered.
     * @param accepted_arguments The set of arguments that this command can accept.
     * @param required_arguments The exact number of arguments required for the command to be able to execute.
     * @param callable           A callable which is invoked when all conditions (default - correct arguments, and number of arguments) have been met.
     * @return Flag indicating whether or not command registration was successful or not.
     */
    public boolean RegisterCommand(String command_name, HashSet<String> accepted_arguments, int required_arguments, GeneralCallable<String, ArrayList<String>> callable) {
        //Explanation : If a command with the same name exists already, return false.
        if (game_commands.containsKey(command_name)) return false;
        //Explanation : Initalise and insert the command.
        this.game_commands.put(command_name, new CommandInfo(accepted_arguments, required_arguments, callable));
        return true;
    }



    public ArrayList<String> feedInput(String str)
    {
        return this.Update(str);
    }
};
