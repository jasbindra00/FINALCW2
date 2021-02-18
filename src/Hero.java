import java.util.ArrayList;
import java.util.Arrays;

/**
 * Description : All heroes are human controlled. Each hero has its own gold counter. This class is kept very primitive
 * so as to adhere to well known design principles.
 */
public class Hero extends Player {
    /**
     * The gold count for the hero.
     */
    private int gold_count;
    private String player_action;

    /**
     * Constructor initialising the attributes.
     *
     * @param token               The token of the player, which in DoD is 'P', but can easily be changed if wanted.
     * @param init_pos            The initial position of the hero.
     * @param field_of_vision_dim The dimensions of the field of vision vision of the hero.
     */
    public Hero(char token, Vector2 init_pos, Vector2 field_of_vision_dim) {
        //Description : Passing the arguments to the base class.
        super(token, init_pos, true, field_of_vision_dim);
    }

    /**
     * @return Simply returns the split user input. The game logic is what validates this input.
     */
    @Override
    public ArrayList<String> GetPlayerAction() {
        //We have got the user input already, and thus this is now
        return new ArrayList<>(Arrays.asList(this.player_action.split(" ")));
    }

    /**
     * Description : Increments the gold count by 1.
     */
    public void IncrementGold() {
        ++gold_count;
    }

    /**
     * @return Returns the gold count of the hero. Note that the Integer object is returned, so as to make a copy of the original variable.
     */
    public Integer GetGoldCount() {
        return gold_count;
    }
    public void setPlayerAction(String string)
    {
        this.player_action = string;
    }
};