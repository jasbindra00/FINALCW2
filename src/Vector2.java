/**
 * Description : Simple helper class used to represent a co-ordinate in two dimensional space.
 */

public final class Vector2 {
    /**
     * Description : x position.
     */
    public int x = 0;
    /**
     * Description : y position.
     */
    public int y = 0;

    /**
     * Description : Default constructor.
     */
    public Vector2() {
    }

    /**
     * Description : Copy constructor.
     *
     * @param other Other object to copy from.
     */

    public Vector2(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Description : Coordinate constructor.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     */
    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //Description : Basic static and non static arithmetic (and a comparison) functions.

    /**
     *
     * @param vec1 Vector argument 1
     * @param vec2 Vector argument 2
     * @return Returns the result of their element wise addition.
     */
    public static Vector2 Add(Vector2 vec1, Vector2 vec2) {
        return new Vector2(vec1.x + vec2.x, vec1.y + vec2.y);
    }

    /**
     * Description : Static scalar multiplication of a vector.
     * @param vec1 The vector to multiply.
     * @param scalar The scalar to multiply by.
     * @return The resultant multiplied vector.
     */
    public static Vector2 Mult(Vector2 vec1, int scalar) {
        return new Vector2(vec1.x * scalar, vec1.y * scalar);
    }

    /**
     * Description : Two vectors are equal if their corresponding x and y coordinates are equal.
     *
     * @param vec1 The first vector to compare.
     * @param vec2 The second vector to compare.
     * @return The flag result of the comparison.
     */
    public static boolean Compare(Vector2 vec1, Vector2 vec2) {
        return (vec1.x == vec2.x && vec1.y == vec2.y);
    }

    /**
     * Description : Adds another vector to this one.
     *
     * @param vec The input vector to add on.
     * @return The result of the addition.
     */
    public Vector2 Add(Vector2 vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    /**
     * Description : Supporting scalar multiplication of the vector.
     *
     * @param scalar Scalar with which to multiply the element
     * @return The result of the operation
     */
    public Vector2 Mult(int scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
}
