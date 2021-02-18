import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public final class SocketHandler extends Thread
{
    private Socket socket;
    private ObjectInputStream input_stream;
    private ObjectOutputStream output_stream;
    private Server server;
    private String client_name;
    private int client_ID;
    private boolean running;
    private ReentrantLock mutex;
    private boolean validated;
    private int strikes;
    private boolean in_game;

    public boolean isInGame()
    {
        return this.in_game;
    }
    private Packet encodePacket(String data, Packet.PacketType packet_type)
    {
        String sender_name = "Server";
        return new Packet(sender_name, data, packet_type);
    }
    public SocketHandler(Server server, Socket entry_socket, int client_ID, Packet handshake) throws Exception
    {
        this.mutex = new ReentrantLock();
        this.server = server;
        this.socket = entry_socket;
        this.input_stream = new ObjectInputStream(entry_socket.getInputStream());
        this.output_stream = new ObjectOutputStream(entry_socket.getOutputStream());
        this.client_ID = client_ID;
        this.client_name = "";
        this.running = true;
        this.handlePacket(handshake);
        this.strikes = 0;
    }
    @Override
    public void run()
    {
        Packet packet = null;
        try {
            while (running) {
                packet = (Packet)this.input_stream.readObject();
                if(packet == null) continue;
                //Packet at this point is valid.
                this.handlePacket(packet);
                this.output_stream.reset();
                packet = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void handlePacket(final Packet packet_data)
    {
        this.mutex.lock();
        try
        {
            Packet.PacketType packet_type = packet_data.packet_type;
            Packet response_packet;
            Utility.print(packet_type.toString());
            switch(packet_type.toString())
            {
                case("CONNECT_GRANTED"):
                {
                    response_packet = packet_data;
                    break;
                }
                case("VALIDATION_REQUEST"):
                {
                    if(this.server.usernameExists(packet_data.data, this.client_ID))
                    {
                        response_packet = this.encodePacket("Account with username [" + packet_data.data + "] already exists. Please choose another name.", Packet.PacketType.VALIDATION_DENIED);
                    }
                    else{
                        response_packet = this.encodePacket("Say hi to [" + packet_data.data + "], everyone!", Packet.PacketType.BROADCAST);
                        this.client_name = packet_data.data;
                        this.server.broadcast(new Packet(response_packet), this.client_ID);
                        response_packet.packet_type = Packet.PacketType.VALIDATION_GRANTED;
                    }
                    break;
                }
                case("DOD_CONNECT_GRANTED"):
                {
                    //DoD Client has granted a join request.


                    //Check if we are on the client socket handler by comparing the names.
                    if(!this.client_name.equals(packet_data.sender_name))
                    {
                        //Broadcast the grant to everyone.
                        this.server.broadcast(new Packet("Server", "[" + packet_data.sender_name + "] has joined a DoD match. Wish the brave warrior luck!", Packet.PacketType.BROADCAST),-1 );
                    }
                    //Send the welcome message from the dod client to the client.
                    response_packet = packet_data;
                }
                case("DOD_GAME_MESSAGE"):
                {

                    //If there is a mismatch between names, then we are definitely on the DoDClient socket handler.

                    //Check if we are the DoDClient SocketHandler.
                    if(!this.client_name.equals(packet_data.sender_name))
                    {
                        //If we are dod client socket, then we need to send the packet to the client socker handler.
                        this.server.send(packet_data, packet_data.sender_name);
                        return;
                    }
                }
                case("DOD_CONNECT_REQUEST"):
                {
                    response_packet = packet_data;
                    break;
                }
                case("DOD_USER_MESSAGE"):
                {
                    response_packet = packet_data;
                    break;
                }
                case("MESSAGE"):
                {
                    //Check the message for sensored words.
                    String sensored_string = this.server.sensorBannedWords(packet_data.data);
                    //The message will be broadcasted regardless.
                    response_packet = new Packet(this.client_name, sensored_string, Packet.PacketType.BROADCAST);
                    if(!this.in_game) this.server.broadcast(response_packet, this.client_ID);
                    //Check if the string is a join request.
                    if(sensored_string.equals("JOIN"))
                    {
                        Packet dod_join_request = new Packet(this.client_name, "", Packet.PacketType.DOD_CONNECT_REQUEST);
                        this.server.send(dod_join_request, 3);
                        this.in_game = true;
                        return;
                    }
                    if(this.in_game)
                    {
                        //Forward the message to dod client.
                        packet_data.packet_type = Packet.PacketType.DOD_USER_MESSAGE;
                        this.server.send(packet_data, 3);
                        return;
                    }

                    //The user has cursed.
                    if(!sensored_string.equals(packet_data.data))
                    {

                        packet_data.data = sensored_string;
                        ++this.strikes;
                        Packet packet;
                        Packet broadcast_packet = null;
                        //User is now banned.
                        if(this.strikes == Server.strike_threshold)
                        {
                            //Let the user know.
                            packet = this.encodePacket("You have cursed too much during your time on this server. You have been banned.", Packet.PacketType.DISCONNECT_GRANTED);
                            //Broadcast his/her kick.
                            broadcast_packet = this.encodePacket(this.client_name + " has been kicked from the server for cursing too much.", Packet.PacketType.BROADCAST);

                        }
                        else {
                            packet = this.encodePacket("Cursing is not permitted on this server. You have been warned and have " + (Server.strike_threshold - this.strikes) + " strikes remaining before you are permanently banned.", Packet.PacketType.WARNING);
                        }


                        //Send the packet.
                        this.output_stream.writeObject(packet);
                        this.output_stream.flush();
                        if(broadcast_packet != null) this.server.broadcast(broadcast_packet, this.client_ID);
                    }
                    return;
                }
                case("BROADCAST"):
                {
                    //If this is a broadcast packet, it shouldn't be broadcast again.

                    response_packet = packet_data;
                    break;
                }

                default:
                    throw new IllegalStateException("Unexpected value: " + packet_type.toString());
            }
            this.output_stream.writeObject(response_packet);
            this.output_stream.flush();
            Utility.print("SENT PACKET");
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
        finally{
            this.mutex.unlock();
        }
    }
    public int getClientID()
    {
        return new Integer(this.client_ID);
    }
    public void terminate()
    {
        this.running = false;
    }
    public String getClientName()
    {
        return new String(this.client_name);

    }
}
