import java.util.ArrayList;

/**
 * Description : Helper class which transmits information about a subset of the registered map, to different objects.
 * For instance, if a future programmer wants to design a new search algorithm for his bot, then he has lots of flexibility due to the wealth of information provided by this class.
 * Notice that the attributes of the map subset are public and final. This reduces code bloat, while giving the user the correct amount
 * of control and the information that they require.
 */
public class MapSubset {

    /**
     * Description :  The top left coordinate of the subset map.
     */
    public final Vector2 subset_world_top_left;
    /**
     * Description :  The bot right coordinate of the subset map.
     */
    public final Vector2 subset_world_bottom_right;
    /**
     * Description :  The dimensions of the subset map.
     */
    public final Vector2 subset_dim;
    /**
     * Description :  The dimensions of the full map.
     */
    public final Vector2 full_map_dim;
    /**
     * Description :  The tiles onto which each player is able to traverse.
     */
    public final String traversable_tiles;
    /**
     * Description : The actual map subset as an array of strings. This is not final, as it is changed, and should be able to be changed by other classes.
     */
    public ArrayList<String> map_subset = new ArrayList<String>();

    /**
     * Description : Constructor which initialises all of the final arguments.
     *
     * @param top_left          The top left corner of the subset.
     * @param bot_right         The bottom right corner of the subset.
     * @param map_dim           The dimensions of the full map.
     * @param subset_dim        The dimensions of the subset.
     * @param traversable_tiles The tiles onto which a given player can traverse.
     */
    MapSubset(Vector2 top_left, Vector2 bot_right, Vector2 map_dim, Vector2 subset_dim, String traversable_tiles) {
        this.subset_world_top_left = top_left;
        this.subset_world_bottom_right = bot_right;
        this.full_map_dim = map_dim;
        this.subset_dim = subset_dim;
        this.traversable_tiles = traversable_tiles;
    }

    /**
     * Description : Converts local subset coordinates into the world coordinates.
     *
     * @param subset_coord The subset coordinates.
     * @return The world coordinates.
     */
    public Vector2 SubsetToWorldCoord(Vector2 subset_coord) {
        return Vector2.Add(subset_coord, this.subset_world_top_left);
    }

    /**
     * Description : Converts world coordinates into local subset coordinates.
     *
     * @param world_coord The world coordinates.
     * @return The subset coordinate.
     */
    public Vector2 WorldToSubsetCoord(Vector2 world_coord) {
        return Vector2.Add(world_coord, Vector2.Mult(subset_world_top_left, -1));
    }
};
