import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
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
    private int strikes;
    private boolean do_not_disturb_mode;

    public boolean isinDoNotDisturbMode()
    {
        return this.do_not_disturb_mode;
    }
    private Packet encodePacket(String data, Packet.PacketType packet_type)
    {
        String sender_name = "Server";
        return new Packet(sender_name, data, packet_type, SenderType.SERVER);
    }
    public SocketHandler(Server server, Socket entry_socket, int client_ID, Packet handshake) throws Exception
    {
        this.mutex = new ReentrantLock();
        this.server = server;
        this.socket = entry_socket;
        this.output_stream = new ObjectOutputStream(entry_socket.getOutputStream());
        this.input_stream = new ObjectInputStream(entry_socket.getInputStream());

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
            while (this.running)
            {
                packet = (Packet)this.input_stream.readObject();
                if(packet == null) continue;

                //Route string to packet handler.
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
        //Making it to the end of this function routes the packet back to the user.
        try
        {
            Packet.PacketType packet_type = packet_data.packet_type;
            Utility.print("[" + (new Date()).toString() +"]: RECEIVED " + packet_type + " FROM " + packet_data.sender_name);

            Packet response_packet;
            this.mutex.lock();
            switch(packet_type.toString())
            {
                case("DOD_CONNECT_REQUEST"):
                case("DOD_USER_MESSAGE"):
                case("CONNECT_GRANTED"):
                case("BROADCAST"):
                {
                    //Broadcast packet shouldn't be broadcast again.
                    //Let the connect request route to the client.
                    response_packet = packet_data;
                    break;
                }
                case("VALIDATION_REQUEST"):
                {
                    //Username already exists. Route the packet back.
                    if(this.server.usernameExists(packet_data.data, this.client_ID)) response_packet = this.encodePacket("Account with username [" + packet_data.data + "] already exists. Please choose another name.", Packet.PacketType.VALIDATION_DENIED);
                    else
                    {
                        //Broadcast the packet.
                        response_packet = this.encodePacket("Say hi to [" + packet_data.data + "], everyone!", Packet.PacketType.BROADCAST);
                        this.client_name = packet_data.data;
                        this.server.broadcast(response_packet, this.client_ID);

                        //Let the client know that validation has also been granted.
                        response_packet.packet_type = Packet.PacketType.VALIDATION_GRANTED;
                        if(packet_data.sender_type.toString().equals("DODCLIENT"))
                        {
                            this.do_not_disturb_mode = true;
                        }
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
                        this.server.broadcast(new Packet("Server", "[" + packet_data.sender_name + "] has joined a DoD match. Wish the brave warrior luck!", Packet.PacketType.BROADCAST, SenderType.SERVER),this.client_ID);

                        //Then we need to forward this message to the client.
                        this.server.send(packet_data, packet_data.sender_name);
                        return;
                    }
                    //The client endpoint has now received the packet
                    //Send to client.
                    else response_packet = packet_data;
                    break;
                }
                case("DOD_GAME_OVER"):
                {
                    if(!this.client_name.equals(packet_data.sender_name))
                    {
                        //If we are dod client socket, then we need to send the packet to the client socker handler.
                        this.server.send(packet_data, packet_data.sender_name);
                        this.server.broadcast(new Packet(this.client_name, "[" + packet_data.sender_name + "] is back from the Dungeons of Doom! Bring out the champagne!", Packet.PacketType.BROADCAST, SenderType.SERVER), -1);
                        return;
                    }
                    response_packet = packet_data;
                    //Now we are on the client. Disable the in game.
                    if(packet_data.packet_type == Packet.PacketType.DOD_GAME_OVER)
                    {
                        this.do_not_disturb_mode = false;
                    }
                    break;
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
                    response_packet = packet_data;
                    //Now we are on the client. Disable the in game.
                    if(packet_data.packet_type == Packet.PacketType.DOD_GAME_OVER)
                    {
                        this.do_not_disturb_mode = false;
                    }
                    break;
                }
                case("MESSAGE"):
                {
                    //Check the message for sensored words.

                    String sensored_string = Utility.sensorWords(packet_data.data, server.getBannedWords());
                    //The message will be broadcasted to everyone who is not in game.
                    response_packet = new Packet(this.client_name, sensored_string, Packet.PacketType.BROADCAST, SenderType.SERVER);

                    if(!this.do_not_disturb_mode) this.server.broadcast(response_packet, this.client_ID);
                    //Check if the string is a join request.
                    if(sensored_string.equals("JOIN"))
                    {
                        //Send a connect request to a dod client.
                        Packet dod_join_request = new Packet(this.client_name, "", Packet.PacketType.DOD_CONNECT_REQUEST, SenderType.SERVER);
                        this.server.send(dod_join_request, this.server.getDoDClientID());
                        this.do_not_disturb_mode = true;
                        return;
                    }


                    if(this.do_not_disturb_mode)
                    {
                        //Forward the message to dod client.
                        packet_data.packet_type = Packet.PacketType.DOD_USER_MESSAGE;
                        this.server.send(packet_data, server.getDoDClientID());
                        return;
                    }
                    if(sensored_string.equals("EXIT"))
                    {
                        this.server.removeClient(this.client_ID, false);
                        return;
                    }
                    //Message received while in DoD game.
                    //The user has said a banned word.
                    if(!sensored_string.equals(packet_data.data))
                    {
                        packet_data.data = sensored_string;
                        //Increase the strike count.
                        ++this.strikes;
                        //User is now banned.
                        if(this.strikes == Server.strike_threshold)
                        {
                            //Broadcast the kick.
                            this.server.broadcast(new Packet("Server", this.client_name + " has been kicked from the server for cursing too much.", Packet.PacketType.BROADCAST, SenderType.SERVER), -1);
                            this.server.removeClient(this.client_ID, true);
                            return;
                        }
                        else
                        {
                            response_packet = this.encodePacket("Cursing is not permitted on this server. You have been warned and have " + (Server.strike_threshold - this.strikes) + " strikes remaining before you are permanently banned.", Packet.PacketType.WARNING);
                        }
                    }
                    if(packet_data.sender_type == SenderType.BOTCLIENT) return;
                    break;
                }
                case("DISCONNECT_GRANTED"):
                {
                    response_packet = packet_data;
                    break;
                }

                default:
                    throw new IllegalStateException("Unexpected value: " + packet_type.toString());
            }
            this.output_stream.writeObject(response_packet);
            this.output_stream.flush();
            Utility.print("[" + (new Date()).toString() +"]: SENT PACKET OF TYPE " + response_packet.packet_type.toString() + " TO " + this.client_name);
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
    public InetSocketAddress getClientConfig()
    {
        return new InetSocketAddress(this.socket.getInetAddress(), this.socket.getPort());
    }
}
