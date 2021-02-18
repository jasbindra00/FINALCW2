import java.util.ArrayList;

/**
 * Description : All enemies are bots. All enemies have a search algorithm object which
 * is used to help them navigate through the map. Note that this search algorithm component
 * is extremely extensible in that the user can choose whichever algorithm he wants. Note that this
 * class is not final, since a future programmer may want to make different enemy types which derive from this.
 * <p>
 * Key Points:
 * -All enemies are players.
 * -All enemies are bots.
 * -All enemies have a SearchAlgorithm component which makes decisions for them.
 * -The vision of the enemy is updated according to internal heuristics.
 */
public class Enemy extends Player {
    /**
     * Description : The 'brain' of the enemy; produces a command using heuristics.
     */
    private SearchAlgorithm search_algorithm;

    /**
     * Constructor initialising the arguments.
     *
     * @param token               The token of the enemy, which in DoD is 'E' (but can easily be changed in the future).
     * @param init_pos            The initial position of the enemy.
     * @param field_of_vision_dim The dimensions of the field of vision.
     * @param search_algorithm    The search algorithm object which is responsible for the navigation of the entity.
     */
    public Enemy(char token, Vector2 init_pos, Vector2 field_of_vision_dim, SearchAlgorithm search_algorithm) {
        //Explanation : Passing the relevant arguments to the base class.
        super(token, init_pos, false, field_of_vision_dim);
        this.search_algorithm = search_algorithm;
    }

    /**
     * Description : An enemy returns the verdict of his 'brain', the SearchAlgorithm, back to the game object.
     *
     * @return A command. For instance, "MOVE N" or "LOOK". Note that although a future programmer may accidently return a command like "GOLD"
     * (which is not applicable to an enemy) in a derived class, this will be negated since the enemy is not subscribed to the gold command!
     */
    @Override
    public ArrayList<String> GetPlayerAction() {
        return search_algorithm.ExecuteSearch(new Vector2(position));
    }
        /*
        Description : Updates the vision of the enemy. This is called when the 'brain' of the enemy, the SearchAlgorithm
        has successfully returned a "LOOK" command to the game.
         */

    /**
     * Description : Updates the vision map of the enemy.
     *
     * @param mapsubset The information about the map subset.
     */
    public void UpdateVisionMap(MapSubset mapsubset) {
        search_algorithm.UpdateMapSubset(mapsubset);
    }
};