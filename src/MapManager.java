import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description : This class manages the current map, as well as its tiles and parameters.
 * The class itself has been designed with flexibility and longevity in mind, by making it as modular as possible.
 */
public final class MapManager {
    /***
     * Holds the map tiles in a '2D' dynamic array.
     */
    private ArrayList<String> map_tiles = new ArrayList<>();
    /**
     * Description : A record of the registered map parameters for the specific map.
     */
    private HashMap<String, MapParameterChecker> map_parameters = new HashMap<String, MapParameterChecker>();

    /**
     * Description : Registers an input parameter given that it has not already been registered.
     *
     * @param parameter_name    The name of the parameter for registration
     * @param essential         Whether or not the parameter's existence in the map file should be optional or not.
     * @param default_argument  The default argument.
     * @param parameter_checker A callable which validates the parameter.
     * @return Returns whether or not the registration was valid or not.
     */
    public boolean RegisterParameter(String parameter_name, boolean essential, String default_argument, GeneralCallable<Boolean, String> parameter_checker) {
        //Check if the parameter exists already.
        if (map_parameters.containsKey(parameter_name)) return false;
        map_parameters.put(parameter_name, new MapParameterChecker(essential, default_argument, parameter_checker));
        return true;
    }

    /**
     * Description : Entry point for reading the file. The user is able to enter a directory, or a file within the current directory.
     * @param EXAMINER_DEBUG_MODE Whether or not the examiner would like to see useful metrics of the game state.
     * @param file_name The name of the file
     * @return Whether or not the operation was successful or not.
     */
    public boolean ReadMap(String file_name, boolean EXAMINER_DEBUG_MODE) {
        /*
            Explanation : The user can enter either a directory, or a file name.
            Create an array of both possibilities. Try to open a buffered reader based on that potential file name.
            If we have iterated through the entire array, and have unsuccessfully opened a file, then the file name is definitely incorrect.
         */

            try
            {
                return this.ReadFileBuffer(new BufferedReader(new FileReader(file_name)), file_name, EXAMINER_DEBUG_MODE);
            }
            catch (IOException exception)
            {

                Logger.Log("MM00", file_name + ". Please include relevant file extensions.", true);
                return false;
            }
        }



    /**
     * @return Returns the dimensions of the map.
     */
    public Vector2 GetMapDimensions() {
        return new Vector2(this.map_tiles.get(0).length(), this.map_tiles.size());
    }

    /**
     * Description : Returns the tile at a given position. If the position exceeds the bounds of the map, then the default tile is returned
     *
     * @param position The position at which the tile is requested.
     * @return The found tile.
     */
    public char GetTile(Vector2 position) {
        Vector2 map_dim = GetMapDimensions();
        if (!Utility.Contains(position, new Vector2(0, 0), Vector2.Add(GetMapDimensions(), new Vector2(-1, -1))))
            return this.map_parameters.get("default_tile").GetArgument().charAt(0);
        return map_tiles.get(position.y).charAt(position.x);
    }

    /**
     * Description : Requested by our game object. If the tile at that position is indeed a gold tile, then it is changed to our wall tile.
     *
     * @param position The position at which a gold pickup is being requested.
     * @return Whether the pickup was successful or not.
     */
    public boolean TryPickupGold(Vector2 position) {
        if (this.GetTile(position) != GetTile("gold_tile")) return false;
        StringBuilder str = new StringBuilder(this.map_tiles.get(position.y));
        str.setCharAt(position.x, GetTile("fill_tile"));
        this.map_tiles.set(position.y, str.toString());
        return true;
    }

    /**
     * @return A string of all traversable tiles.
     */
    public String GetTraversableTiles() {
        return new String(map_parameters.get("fill_tile").GetArgument() + map_parameters.get("gold_tile").GetArgument() + map_parameters.get("exit_tile").GetArgument());
    }

    /**
     * @return A string of all non-traversable tiles.
     */
    public String GetNonTraversableTiles() {
        return new String(map_parameters.get("wall_tile").GetArgument());
    }

    /**
     * Description : A map subset is requested by the game object, in either wanting to print the result of the LOOK command, or in wanting to update the vision of an enemy player.
     *
     * @param centre           The centre at which the subset should be based.
     * @param subset_dimension The dimensions of the subset. This must be odd, so as to be able to produce a balanced centre.
     * @return An object holding useful information about the map subset.
     */
    public MapSubset GetMapSubset(Vector2 centre, Vector2 subset_dimension) {
        //Explanation : The dimension of the subset must be odd.
        if (subset_dimension.x % 2 == 0 || subset_dimension.y % 2 == 0) {
            subset_dimension.Add(new Vector2(1, 1));
        }
        //Explanation : Finding the top left and bottom right corners of the subset.
        Vector2 offset = new Vector2((subset_dimension.x - 1) / 2, (subset_dimension.y - 1) / 2);
        MapSubset subset = new MapSubset(Vector2.Add(centre, Vector2.Mult(offset, -1)), Vector2.Add(centre, offset), GetMapDimensions(), subset_dimension, GetTraversableTiles());
        //Explanation : Compile the subset array by requesting the tiles contained within the corners.
        for (int y = subset.subset_world_top_left.y; y < subset.subset_world_bottom_right.y + 1; ++y) {
            StringBuilder current_line = new StringBuilder();
            for (int x = subset.subset_world_top_left.x; x < subset.subset_world_bottom_right.x + 1; ++x) {
                //Explanation : Add the tile to our tile line. Notice that out of bounds tiles are automatically defaulted by the GetTile function.
                current_line.append(this.GetTile(new Vector2(x, y)));

            }
            subset.map_subset.add(current_line.toString());
        }
        return subset;
    }

    /**
     * Description : Tries to obtain the parameter for the argument parameter name.
     *
     * @param param_name The name of the requested parameter.
     * @return The argument of the parameter, if it exists, otherwise an empty string.
     */
    public String GetParameterArgument(String param_name) {
        if (!this.map_parameters.containsKey(param_name)) return new String("");
        return new String(map_parameters.get(param_name).GetArgument());
    }

    /**
     * Description : Iterates through the first lines of the file in search of our map parameters.
     *
     * @param file_buffer The buffer for the map file.
     * @param file_name   The name of the file, used for logging purposes
     * @return Whether or not the parse was successful
     * @throws IOException BufferedReader IO exceptions are not hidden, unlike the scanner.
     */
    private boolean ParseMapParameters(BufferedReader file_buffer, String file_name) throws IOException {
        /*
            Explanation : I have given the user the choice to specify whether or not their registered map
            parameter is essential or not.

            If an essential parameter is not found within the first lines of the file, then the map will not read successfully.

            If all essential parameters have been found, and we encounter an unrecognised line (one which does not contain a parameter)
            then we simply stop the search successfully.
         */


        //Explanation : Tracking the number of parameters we have found during our read.
        int found_n_parameters = 0;
        final int n_parameters = this.map_parameters.size();

        //Explanation : Tracking the number of essential parameters we have found during our read.
        int found_n_essential_parameters = 0;
        //Explanation : Count the number of required essential parameters.
        int required_essential_parameters = (int) (this.map_parameters.values().stream().filter(val -> val.IsEssential()).count());
        String current_line;
        //Explanation : The max buffer read size before we are able to revert back to the marked position with BufferedReader::reset
        final int MAX_BUFFER_SIZE = 1000;
        //Explanation : Iterate through each of the lines until we find all parameters.
        while ((found_n_parameters != n_parameters)) {
            //Explanation : Mark the point of the current line.
            file_buffer.mark(MAX_BUFFER_SIZE);
            /*
                Explanation : You may be thinking.. Why not put this condition as part of the while condition(s)?
                The reason is because I need to mark the position of this line before I actually read it, for when I need to revert back.
             */
            if ((current_line = file_buffer.readLine()) == null) break;
            //Explanation : Try to find any of our registered parameters from within the current line.
            boolean found_parameter = false;
            for (HashMap.Entry<String, MapParameterChecker> pair : this.map_parameters.entrySet()) {
                //Explanation : The parameter does not exist within our line.
                if (current_line.indexOf(pair.getKey()) == -1 || current_line.length() == pair.getKey().length())
                    continue;
                //Explanation : The parameter name exists within the line. Obtain the rest of the line (after the name).
                String parameter_argument = current_line.substring(pair.getKey().length() + 1);
                //Explanation : Try to initialise the parameter argument.
                if (pair.getValue().SetParameterArgument(parameter_argument)) {
                    //Explanation : Initialisation of the parameter was successful.
                    if (pair.getValue().IsEssential()) ++found_n_essential_parameters;
                    ++found_n_parameters;
                    found_parameter = true;
                    break;
                }
            }
            //Explanation : If we encounter an unrecognised line, and we have read our essential parameters, it's possible that the users map array has started.
            if (!found_parameter) {
                //Explanation : We encountered an unrecognised line without meeting all of our essential parameters. Unsuccessful read.
                if (found_n_essential_parameters != required_essential_parameters) return false;
                //Explanation : Go back to the line before the current one, as it may be the first line of the users map.
                file_buffer.reset();
                return true;
            }
        }
        return (found_n_essential_parameters == required_essential_parameters);
    }

    /**
     * Description : The file reading process is split into two parts : Reading the essential map parameters, and reading the map array itself. This function calls the necessary functions to facilitate both.
     * @param file_buffer The buffer of the map file.
     * @param file_name   The name of the file, for logging purposes.
     * @param EXAMINER_DEBUG_MODE Whether or not the examiner would like to see useful metrics of the game state.
     * @return Whether or not the file was successfully read or not.
     * @throws IOException BufferedReader IO exceptions are not hidden, unlike the scanner.
     */
    private boolean ReadFileBuffer(BufferedReader file_buffer, String file_name, boolean EXAMINER_DEBUG_MODE) throws IOException {

        //Explanation : Find the map parameters within the first few lines. All essential parameters must be found.
        if (!ParseMapParameters(file_buffer, file_name)) {
            Logger.Log("MM01", "File name - " + file_name, true);
            return false;
        }
        char default_tile = map_parameters.get("default_tile").GetArgument().charAt(0);
        //Explanation : After each essential parameter has been found, iterate through the remainder of the file, which should be the map array itself.
        int y = 0;
        String line;
        while ((line = file_buffer.readLine()) != null) {
            StringBuilder dynamic_line = new StringBuilder(line);
            //Explanation : Iterate through each character of the current line.
            for (int x = 0; x < dynamic_line.length(); ++x) {
                //Explanation : Check to see if the token is a registered tile.
                char current_tile = dynamic_line.charAt(x);
                if (this.IsValidTile(current_tile)) continue;
                //Explanation : If unregistered, default the tile.
                Logger.Log("MM02", "{" + x + "," + y + "} of token " + current_tile + " in the map file of name " + file_name + ". Defaulting tile to " + default_tile + "...", true);
                dynamic_line.setCharAt(x, default_tile);
            }
            //Explanation : Append the map tile line to our map array.
            this.map_tiles.add(dynamic_line.toString());
            ++y;
        }
        //Explanation : The map must have at least one row.
        if (this.map_tiles.isEmpty()) {
            Logger.Log("MM03", "", true);
            return false;
        }
        //Explanation : Make all rows a constant length.
        final int MAX_ROW_SIZE = this.map_tiles.stream().map(String::length).max(Integer::compareTo).get();
        for (int i = 0; i < map_tiles.size(); ++i) {
            final String current_row = map_tiles.get(i);
            if (current_row.length() < MAX_ROW_SIZE)
                map_tiles.set(i, map_tiles.get(i) + Character.toString(default_tile).repeat(MAX_ROW_SIZE - current_row.length()));
        }
        Utility.PrintToConsole("********* MAP READ SUCCESSFUL *********");
        //Explanation : Print metrics if examiner debug mode is active.
        if (EXAMINER_DEBUG_MODE) this.PrintMapMetrics();
        Utility.PrintToConsole("********* Welcome to " + map_parameters.get("name").GetArgument() + " *********");
        return true;
    }

    /**
     * Description : Prints the metrics of the map (used in examiner debug mode).
     */
    public void PrintMapMetrics() {
        //Explanation : Print all registered map parameters.
        Utility.PrintToConsole("*** START REGISTERED PARAMETERS ***");
        this.map_parameters.forEach((key, value) -> {
            Utility.PrintToConsole("* " + key + " : " + value.GetArgument());
        });
        Utility.PrintToConsole("*** END REGISTERED PARAMETERS ***\n");
        //Explanation : Print a preview of the map (with no players)
        System.out.println(("*** START MAP PREVIEW ***"));
        this.map_tiles.forEach((final String tile_row) -> {
            Utility.PrintToConsole(tile_row);
        });
        System.out.println(("*** END MAP PREVIEW ***\n"));
    }

    /**
     * Description : Gets a given tile_type from the registered parameters. For instance, GetTile("wall_tile") returns '#'
     *
     * @param tile_type The type of requested tile.
     * @return The found tile.
     */
    private char GetTile(String tile_type) {
        return map_parameters.get(tile_type).GetArgument().charAt(0);
    }

    /**
     * Description : Checks to see if the input tile is part of our traversable or non traversable tiles.
     *
     * @param tile The tile to be checked.
     * @return Whether or not the input tile is valid or not.
     */
    private Boolean IsValidTile(char tile) {
        return (GetTraversableTiles() + GetNonTraversableTiles()).indexOf(tile) != -1;
    }

    /**
     * Description : Returns a copy of the tile array specifed by the user.
     * @return The tile array.
     */
    public ArrayList<String> GetTileArray() {
        return new ArrayList<String>(this.map_tiles);
    }

    /**
     * Description : This function is just for fun. Choose any image you want, and I can turn it into a map.
     * @param image_name The name of your image.
     */

};