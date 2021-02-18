import java.util.ArrayList;

/**
 * Description : The abstract base class for all future entities, whether they be human or bot controlled.
 * Note that I have kept this class incredibly primitive. An isolated player has no bearing
 * whatsoever on the flow of the game.This is in accordance with the "Chain of Responsibility" and "Observer" design patterns
 * with which I have designed this game.
 */
public abstract class Player {
    /**
     * Description : The identifying token for this player.
     */
    protected final char token;
    /**
     * Description : All players are either human or computer controlled. This flag differentiates between them, and mitigates expensive casting.
     */
    protected final boolean is_human;
    //Description : All players are either human or computer controlled. This boolean helps the to differentiate that, and mitigates expensive casting.
    /**
     * Description : The position of the player entity.
     */
    protected Vector2 position;
    /**
     * Description : The dimensions of the players field of vision.
     */
    protected Vector2 field_of_vision_dim;

    /**
     * Constructor initialising the attributes.
     *
     * @param token               The token of the player.
     * @param init_pos            The initial position of the player.
     * @param is_human            Flag indicating whether or not the player is human.
     * @param field_of_vision_dim The dimensions of the field of vision of the player.
     */
    Player(char token, Vector2 init_pos, boolean is_human, Vector2 field_of_vision_dim) {
        this.token = token;
        this.position = init_pos;
        this.is_human = is_human;
        this.field_of_vision_dim = field_of_vision_dim;

    }

    /**
     * Description : An abstract function which must be defined for each derived Player.
     * For instance, a (Human) hero would request from the command line, while an Enemy (Bot) would return the result of his search algorithm.
     *
     * @return The verdict of his action, ie a command. For instance, a bot could return {"MOVE","N"}
     */
    public abstract ArrayList<String> GetPlayerAction();

    /**
     * Description : Future programmers may want to allow for diagonal movement, for instance "MOVE N E". This function would be used to facilitate
     * the execution of each movement.
     *
     * @param offset The amount by which the player should be moved.
     */
    public void Move(Vector2 offset) {
        position.Add(offset);
    }

    /**
     * Description : Sets the position of the player.
     *
     * @param pos The new position.
     */
    public void SetPosition(Vector2 pos) {
        position = pos;
    }

    /**
     * @return A copy of the player's position.
     */
    public Vector2 GetPosition() {
        return new Vector2(position);
    }

    /**
     * @return The token of the player.
     */
    public Character GetToken() {
        return token;
    }

    /**
     * @return The field of vision dimensions of the player.
     */
    public Vector2 GetFieldOfVisionDimensions() {
        return new Vector2(field_of_vision_dim);
    }

    /**
     * @return A flag indicating whether or not the player is a human.
     */
    public Boolean IsHuman() {
        return is_human;
    }

};
