import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    public Socket connected_socket;
    private SenderType sender_type;
    protected PacketReceiver input_thread;
    protected PacketSender output_thread;
    protected boolean on_server;
    protected boolean validated;
    protected ReentrantLock mutex;
    Client(SenderType sender_type,InetSocketAddress server_config, ThreadRunner<PacketReceiver> input_run, PacketHandler server_packet_handler, ThreadRunner<PacketSender> output_run)
    {
        this.sender_type = sender_type;
        try {
            this.connected_socket = new Socket(server_config.getAddress(), server_config.getPort());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        this.output_thread = new PacketSender(this, this.connected_socket,output_run);
        this.input_thread = new PacketReceiver(this,this.connected_socket,server_packet_handler, input_run);
        this.mutex = new ReentrantLock();

    }
    public boolean isOnServer()
    {
        return this.on_server;
    }
    public static void receiverRun(PacketReceiver listener)
    {
        try {
            Packet packet;
            while (listener.isRunning())
            {
                packet = (Packet) listener.getReader().readObject();
                if (packet == null) continue;
                listener.handlePacket(packet);
                packet = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void senderRun(PacketSender listener)
    {
        try {
            listener.fetchUsername();
            String input;
            while (listener.isRunning())
            {
                //The client needs to choose a user name.
                input = listener.getInput();
                listener.send(new Packet(listener.getUserName(), input, Packet.PacketType.MESSAGE, listener.getClient().getSenderType()));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    public static void clientPacketHandler(Packet data, PacketReceiver listener)
    {
        Utility.print(data.getFormattedString());
        //We need to send this packet to the input reader to handle.
        switch(data.packet_type.toString())
        {
            case("CONNECT_GRANTED"):
            {
                listener.getClient().toggleOnServer();
                break;
            }
            case("CONNECT_DENIED"):
            case("DISCONNECT_GRANTED"): {
                try
                {
                    listener.getClient().stop();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                break;
            }
            case("VALIDATION_GRANTED"):
            {
                listener.getClient().toggleValidated();
                break;
            }
            case("VALIDATION_DENIED"):
            {
                break;
            }
            case("BROADCAST"):
            {
                break;
            }
        }
    }
    public Socket getConnectedSocket()
    {
        return this.connected_socket;
    }

    public synchronized SenderType getSenderType(){return this.sender_type;}
    public synchronized boolean onServer()
    {
        return this.on_server;
    }
    public void stop() throws Exception
    {
        this.mutex.lock();
        try {
            this.output_thread.terminate();
            this.input_thread.terminate();
        }
        finally{
            this.mutex.unlock();
        }
    }
    public synchronized boolean validated()
    {
        return this.validated;
    }
    public synchronized void toggleValidated()
    {
        this.validated = (this.validated)? false : true;
    }
    public synchronized void toggleOnServer()
    {
        this.on_server = (this.on_server)? false : true;
    }
    public void start()
    {
        this.input_thread.start();
        this.output_thread.start();
    }
    public static void main(String[] args) {
        try {
            Client client = new Client(SenderType.HUMANCLIENT, new InetSocketAddress(InetAddress.getLoopbackAddress(), 4999), Client::receiverRun, Client::clientPacketHandler,Client::senderRun);
            client.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
         }
    }
}