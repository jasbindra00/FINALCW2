import java.util.ArrayList;

/**
 * Description : An abstract base class used for the 'brains' of an enemy.
 * This design allows for a future programmer to use any algorithm he wants!
 */
public abstract class SearchAlgorithm {
    /**
     * Description : A flag indicating whether a "LOOK" command should be dispatched or not.
     */
    protected boolean requires_vision_update = true;
    /**
     * Description : Holds information of the subset of the map, which is going to be used to seek the target.
     */
    protected MapSubset map_subset;
    /**
     * Description : The position of the target in local subset coordinates.
     */
    protected Vector2 local_target_position = new Vector2(0, 0);
    /**
     * Description : The target which the player wants to seek.
     */
    protected char target_token;

    /**
     * Description : A constructor initialising the object attributes.
     *
     * @param target_token The token to be seeked.
     */
    public SearchAlgorithm(char target_token) {
        this.target_token = target_token;
    }

    /**
     * Description : This method returns the verdict command of the algorithm, and has to be overriden by derived classes.
     *
     * @param start_position The global position of the entity.
     * @return The command, for instance {"MOVE","N"}
     */
    public abstract ArrayList<String> ExecuteSearch(Vector2 start_position);

    /**
     * Description : Updates the vision portion of the algorithm / player.
     *
     * @param subset Update value.
     */
    public void UpdateMapSubset(MapSubset subset) {
        map_subset = subset;
        requires_vision_update = false;
    }
};