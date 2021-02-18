import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerListener extends Thread
{
    private ObjectInputStream reader;
    protected Client client;
    private boolean running = true;
    public void terminate()
    {
        running = false;
    }
    public ServerListener(Client client, Socket client_socket) throws Exception
    {
        try {
            this.reader = new ObjectInputStream(client_socket.getInputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        this.client = client;
    }
    public void handlePacket(Packet data) throws Exception
    {
        Utility.print(data.getFormattedString());
        //We need to send this packet to the input reader to handle.

        switch(data.packet_type.toString())
        {
            case("CONNECT_GRANTED"):
            {
                this.client.toggleOnServer();
                break;
            }
            case("CONNECT_DENIED"):
            case("DISCONNECT_GRANTED"): {
                this.client.stop();
                break;
            }
            case("VALIDATION_GRANTED"):
            {
                this.client.toggleValidated();
                break;
            }
            case("VALIDATION_DENIED"):
            {
                break;
            }
            case("BROADCAST"):
            {
                //The user has receieved a broadcast.
                //If this client is a chatbot, then we simply need to send a message back.
                if(this.client.isChatbot())
                {
                    //Then simply ask the user listener thread to handle this
                }
                break;
            }
        }

    }
    @Override
    public void run()
    {
        //Checks for messages from the server concurrently.
        try {
            Packet packet;
            while (running)
            {
                packet = (Packet) this.reader.readObject();
                if (packet == null) continue;
                this.handlePacket(packet);
                packet = null;

            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
