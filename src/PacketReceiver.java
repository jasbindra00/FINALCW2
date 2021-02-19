import java.io.ObjectInputStream;
import java.net.Socket;


//What if we have a private thread.
public class PacketReceiver extends Thread
{
    private ObjectInputStream reader;
    private Client client;
    private PacketHandler packet_handler;
    private ThreadRunner<PacketReceiver> thread_runner;
    private boolean running = true;

    public PacketReceiver(Client client, Socket connected_socket, PacketHandler packet_handler, ThreadRunner<PacketReceiver> thread_runner)
    {
        try { this.reader = new ObjectInputStream(connected_socket.getInputStream()); }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
        this.client = client;
        this.packet_handler = packet_handler;
        this.thread_runner = thread_runner;
    }
    @Override
    public void run()
    {
        this.thread_runner.run(this);
    }
    public synchronized void handlePacket(Packet data) throws Exception
    {
        this.packet_handler.handlePacket(data, this);
    }

    public void terminate()
    {
        running = false;
    }

    public ObjectInputStream getReader()
    {
        return this.reader;
    }
    public Client getClient()
    {
        return this.client;
    }
    public boolean isRunning()
    {
        return this.running;
    }


}
