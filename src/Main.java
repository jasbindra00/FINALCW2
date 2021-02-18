/**
 * Description : The Main class which holds the main entry function.
 */
public class Main {
    /**
     * Description : The main entry point, which loads and updates the game.
     * @param args N/A
     */
    public static void main(String[] args) {
        //Explanation : Initialise the log codes.
        Logger.InitialiseLogCodes();
        //Explanation : Change the flag from within the constructor to change the EXAMINER_DEBUG_MODE.
        Game game = new Game(true);
        //Explanation : Load the game.
        game.Load(1, 1);
        //Explanation : Update the game whilst the game is not over.
//        while (!game.IsGameOver()) {
//            game.Update();
//        }
        //Explanation : Don't forget to close the scanner to System.In
        Utility.CloseInputScanner();
    }
}