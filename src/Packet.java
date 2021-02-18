import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Packet implements Serializable {
    public enum PacketType{
        //Exchanged when a new client connects to the server.
        CONNECT_REQUEST,
        CONNECT_DENIED,
        CONNECT_GRANTED,

        //Exchanged when a new client chooses a username.
        VALIDATION_REQUEST,
        VALIDATION_DENIED,
        VALIDATION_GRANTED,

        BROADCAST,
        MESSAGE,
        WARNING,

        DISCONNECT_REQUEST,
        DISCONNECT_DENIED,
        DISCONNECT_GRANTED,

        DOD_USER_MESSAGE,
        DOD_CONNECT_REQUEST,
        DOD_CONNECT_GRANTED,
        DOD_GAME_MESSAGE

    }
    public String sender_name;
    public String data;
    public PacketType packet_type;
    private void init(String data, PacketType packet_type)
    {
        this.data = data;
        this.packet_type = packet_type;
    }
    Packet()
    {

    }
    Packet(String sender_name, String data, PacketType packet_type)
    {
        this.sender_name = sender_name;
        this.init(data, packet_type);
    }
    Packet(Packet other)
    {
        this.packet_type = other.packet_type;
        this.data = other.data;
        this.sender_name = other.sender_name;
    }
    String getFormattedString()
    {
        return "[" + this.sender_name + "]: " + this.data;
    }




}
