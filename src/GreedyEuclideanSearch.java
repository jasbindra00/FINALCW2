import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description : A simple custom algorithm which determines the route to take toward the token it seeks.
 * Here is how the algorithm operates :
 * REQUESTS A VISION UPDATE (By the "LOOK" command) IF
 * -The player is on the edge of its vision field
 * -or if the player has not seen its target for a while
 * -or after a certain number of turns
 * -or if it has reached reached its target
 * MOVEMENT
 * -Finds the best route to its target by choosing the neighbor which is the closest to it.
 * -Starts moving randomly if it cannot see the player.
 */
public class GreedyEuclideanSearch extends SearchAlgorithm {
    /**
     * Description : Once the move_counter has reached this threshold, a vision refresh will be requested.
     */
    final private int max_move_before_refresh = 1;
    /**
     * Description : The threshold of the above counter, whereby a refresh vision request will be dispatched to the game.
     */
    final private int max_missing_target_before_refresh = 1;
    /**
     * Description : Keeps track of how many moves have been made after a refresh of the vision has been given.
     */
    private int move_counter = 0;
    /**
     * Description : Keeps track of the number of times we have failed to find our target.
     */
    private int missing_target_counter = 0;

    /**
     * Description : Constructor which asks for a target to seek. This flexible design allows future programmers to change the targets which a given enemy seeks, for instance, "G" instead of the hero.
     *
     * @param target The seeked target.
     */
    public GreedyEuclideanSearch(char target) {
        //Explanation : Calling the base constructor.
        super(target);
    }

    /**
     * Description : Returns whether or not our position is on the edge of our vision map or not.
     *
     * @param current_position The current position of the entity in local coordinates.
     * @return Flag returning whether the player is on the edge or not.
     */
    private boolean OnEdgeOfSubset(Vector2 current_position) {
        return current_position.x == 0 || current_position.x == map_subset.subset_dim.x - 1 || current_position.y == 0 || current_position.y == map_subset.subset_dim.y;
    }

    /**
     * Description : Returns whether or not we have reached our target.
     *
     * @param current_position The local position of the entity.
     * @return Whether or not the target has been reached.
     */
    private boolean ReachedTarget(Vector2 current_position) {
        return Vector2.Compare(current_position, this.local_target_position);
    }

    /**
     * Description : Returns a random position from within our vision map.
     *
     * @return The random position.
     */
    private Vector2 RandomSubsetPosition() {
        return new Vector2(Utility.RandomNumber(0, map_subset.subset_dim.x), Utility.RandomNumber(0, map_subset.subset_dim.y));
    }

    /**
     * Description : Returns an array of the traversable neighbors centred on the argument position.
     * Note that the generality of this function accommodates for the fact that a future programmer would want
     * to add the ability for a given entity to traverse diagonally.
     *
     * @param entity_position The local position of the entity.
     * @return The traversable neighbors centred at the argument position
     */
    public HashMap<String, Vector2> GetNeighbors(Vector2 entity_position) {
        //Explanation : Obtain the cardinal directions, since we need to test a movement in each direction.
        HashMap<String, Vector2> traversable_directions = Utility.GetCardinalVectors();
        HashMap<String, Vector2> res = new HashMap<String, Vector2>();
        for (HashMap.Entry<String, Vector2> pair : traversable_directions.entrySet()) {
            //Explanation : Find the future position after traversing in the direction of the current cardinal direction.
            Vector2 future_position = new Vector2(entity_position);
            future_position.Add(pair.getValue());
            //Explanation : If this future position is out of the bounds of our subset, then it's not a valid neighbor.
            if (!Utility.Contains(future_position, new Vector2(0, 0), Vector2.Add(map_subset.subset_dim, new Vector2(-1, -1)))) {
                continue;
            }
            //Explanation : Check to see if the tile at this future position is indeed traversable. If not, remove this direction from the traversable directions.
            char next_tile = this.map_subset.map_subset.get(future_position.y).charAt(future_position.x);
            if (this.map_subset.traversable_tiles.indexOf(next_tile) == -1 && next_tile != this.target_token) continue;
            res.put(pair.getKey(), pair.getValue());
        }
        return res;
    }

    /**
     * Description : Determines whether or not an update to the vision map is required by evaluating the state of the position, and the counters.
     *
     * @param current_position The local position of the entity.
     * @return Whether or not a "LOOK" command should be dispatched.
     */
    public boolean UpdateVisionStatus(Vector2 current_position) {
                /*
                        Explanation : An update is required when :
                                -The entity is on the edge of his local map.
                                -The entity has reached his target.
                                -The entity has moved past a specific threshold counter.
                                -The entity has failed to encounter his target according to a threshold.
                 */
        if (OnEdgeOfSubset(current_position) ||
                ReachedTarget(current_position) ||
                move_counter == max_move_before_refresh ||
                missing_target_counter == max_missing_target_before_refresh) {
            move_counter = 0;
            missing_target_counter = 0;
            requires_vision_update = true;
            local_target_position = RandomSubsetPosition();
        }
        return requires_vision_update;
    }

    /**
     * Description : Iterates through our subset in search for the target token, and returns whether or not our target has been found.
     *
     * @return Whether or not the target has been found.
     */
    public boolean TargetAcquired() {
        //Explanation : Iterate through each subset tile.
        for (int y = 0; y < map_subset.subset_dim.y; ++y) {
            for (int x = 0; x < map_subset.subset_dim.x; ++x) {
                //Explanation : Check if the subset tile is our token tile.
                char current_ch = this.map_subset.map_subset.get(y).charAt(x);
                if (current_ch == this.target_token) {
                    //Explanation : If so, update the target position, and reset the counter.
                    this.local_target_position = new Vector2(x, y);
                    missing_target_counter = 0;
                    return true;
                }
            }
        }
        //Explanation : The target was not found; increment the counter.
        ++missing_target_counter;
        return false;

    }

    /**
     * Description : Overrides the abstract base method and returns a command based on the evaluation of the above methods and attributes.
     *
     * @param start_position The start position of the entity in global coordinates.
     * @return The verdict command of the search, for instance {"MOVE", "N"}.
     */
    @Override
    public ArrayList<String> ExecuteSearch(Vector2 start_position) {
        //Explanation : Convert the world start position into local subset coordinates.
        start_position.Add(Vector2.Mult(map_subset.subset_world_top_left, -1));
        //Explanation : Check to see if a refresh is required, and if so, return the required command.
        if (UpdateVisionStatus(start_position)) return new ArrayList<String>() {{
            add("LOOK");
        }};
        //Explanation : If we couldn't find our target, then our target is a random position from within our vision.
        if (!TargetAcquired())
            local_target_position = new Vector2(Utility.RandomNumber(0, map_subset.subset_dim.x), Utility.RandomNumber(0, map_subset.subset_dim.y));
        ++move_counter;
        //Explanation : Finding the first neighbor which gives us the lowest heuristic in getting to our goal.
        //ArrayList<Vector2> neighbors = GetNeighbors(new Vector2(start_position));
        HashMap<String, Vector2> traversable_neighbors = GetNeighbors(new Vector2(start_position));
        //Explanation : We cannot move at all, and so there is no command.
        if (traversable_neighbors.isEmpty()) return new ArrayList<String>();
        //Explanation : Find the record heuristic and corresponding direction.
        double record_heuristic = Integer.MAX_VALUE;
        String record_direction = new String("");
        //Explanation : Iterate through each of the traversable neighbors
        for (HashMap.Entry<String, Vector2> direction : traversable_neighbors.entrySet()) {
            Vector2 future_position = new Vector2(start_position);
            future_position.Add(direction.getValue());
            double heuristic = Utility.EuclideanDistance(future_position, local_target_position);
            //Explanation : Check to see if the distance from the neighbor to our target beats any other previous record.
            if (heuristic < record_heuristic) {
                //Explanation : If so, it's our best move so far. Log it.
                record_heuristic = heuristic;
                record_direction = direction.getKey();
            }
        }
        //Explanation : Return a "MOVE" command in that direction.
        //Explanation : Java ArrayList requires us to insert a final string as an element of the array list.
        final String final_record_direction = record_direction;
        return new ArrayList<String>() {{
            add("MOVE");
            add(new String(final_record_direction));
        }};
    }
};
