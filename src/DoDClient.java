import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class DoDClient extends Client
{

    //client_name : GAME
    private HashMap<String,Game> game_instances;



    public DoDClient() throws Exception
    {
        super();
        this.game_instances = new HashMap<>();
    }
    void send(Packet packet) throws Exception
    {
        this.writing_thread.send(packet);
    }

    private void createGame(String client_name)
    {
        Game game = new Game(false);
        game.Load(1,1);
        this.game_instances.put(client_name, game);
    }
    private Game getGameInstance(String client_name)
    {
        return this.game_instances.get(client_name);
    }

    public static void main(String[] args)
    {

        try {
            DoDClient client = new DoDClient();

            UserListener listener = new UserListener(client, client.getConnectedSocket())
            {
                @Override
                public void run()
                {
                    //We don't listen to the user input.
                    try{
                        this.fetchUsername();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }
            };

            ServerListener server_listener = new ServerListener(client, client.getConnectedSocket())
            {
                @Override
                public void handlePacket(Packet packet)
                {
                    DoDClient client = (DoDClient)this.client;
                    try {
                        super.handlePacket(packet);
                        //DoD client recieves a message.
                        switch(packet.packet_type.toString())
                        {
                            case("DOD_CONNECT_REQUEST"): //The client just sent "JOIN".
                            {

                                //Create a game instance for the client.
                                client.createGame(packet.sender_name);


                                //Grant the request. Send a welcome packet to the DoDClient socket handler.
                                Packet handshake_packet = new Packet(packet.sender_name, "Welcome to the Dungeons of Doom! Enjoy your game!", Packet.PacketType.DOD_CONNECT_GRANTED);
                                client.send(handshake_packet);
                                break;
                            }
                            case("DOD_USER_MESSAGE"): //The client has sent a message while in DoD
                            {
                                //Received a user message.

                                //Grab the game instance for this user.

                                Game user_game = client.getGameInstance(packet.sender_name);

                                //Feed the user response into the game.
                                //Obtain the response from the game.
                                ArrayList<String> game_responses = user_game.feedInput(packet.data);

                                //Send the response to the DoDClient socket handler.
                                Packet response = new Packet(packet.sender_name, String.join("\n", game_responses), Packet.PacketType.DOD_GAME_MESSAGE);
                                client.send(response);
                                break;
                            }
                        }

                    }
                    catch(Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
            };
            Logger.InitialiseLogCodes();
            client.initialise(listener, server_listener);
            client.start();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }


}