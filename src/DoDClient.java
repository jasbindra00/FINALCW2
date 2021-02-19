import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class DoDClient extends Client
{
    //Client name : game instance
    private HashMap<String,Game> game_instances;
    public DoDClient(InetSocketAddress server_config, ThreadRunner<PacketReceiver> thread_runner, PacketHandler server_packet_handler, ThreadRunner<PacketSender> input_thread_runner) throws Exception
    {
        super(SenderType.DODCLIENT, server_config, thread_runner, server_packet_handler, input_thread_runner);
        this.game_instances = new HashMap<>();

    }
    public void send(Packet packet) throws Exception
    {
        this.output_thread.send(packet);
    }
    private void createGame(String client_name)
    {
        Game game = new Game(false);
        game.Load(15,1);
        this.game_instances.put(client_name, game);
    }
    private Game getGameInstance(String client_name)
    {
        return this.game_instances.get(client_name);
    }
    public void removePlayer(String client_name)
    {
        this.game_instances.remove(client_name);
    }
    public static void senderRun(PacketSender sender)
    {
        try
        {
            sender.fetchUsername();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void dodHandlePacket(Packet packet, PacketReceiver receiver)
    {
        DoDClient client = (DoDClient)receiver.getClient();
        try
        {
            Client.clientPacketHandler(packet, receiver);
            //DoD client recieves a message.
            switch(packet.packet_type.toString())
            {
                case("DOD_CONNECT_REQUEST"): //The client just sent "JOIN".
                {
                    client.createGame(packet.sender_name);
                    Packet handshake_packet = new Packet(packet.sender_name, "Welcome to the Dungeons of Doom! Enjoy your game!", Packet.PacketType.DOD_CONNECT_GRANTED, SenderType.DODCLIENT);
                    client.send(handshake_packet);
                    break;
                }
                case("DOD_USER_MESSAGE"):
                {
                    Game user_game = client.getGameInstance(packet.sender_name);
                    ArrayList<String> game_responses = user_game.feedInput(packet.data);
                    String game_response_string = "\n" + String.join("\n", game_responses);
                    Packet.PacketType client_message_type;
                    if(user_game.IsGameOver())
                    {
                        client_message_type = Packet.PacketType.DOD_GAME_OVER;
                        client.removePlayer(packet.sender_name);
                        game_response_string = game_response_string + "\n" + "Returning to server...";
                    }
                    else client_message_type = Packet.PacketType.DOD_GAME_MESSAGE;
                    Packet response = new Packet(packet.sender_name,game_response_string, client_message_type, SenderType.DODCLIENT);
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
    public static void main(String[] args)
    {
        Logger.InitialiseLogCodes();
        try
        {
            DoDClient client = new DoDClient(new InetSocketAddress(InetAddress.getLoopbackAddress(), 4999), DoDClient::receiverRun, DoDClient::dodHandlePacket, DoDClient::senderRun);
            client.start();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }
}