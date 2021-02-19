import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class PacketSender extends Thread{
    private ThreadRunner<PacketSender> thread_runner;
    private ObjectOutputStream writer;
    private BufferedReader user_input_reader;
    //Move this to client.
    private String user_name = "Guest";
    private Client client;
    private boolean running = true;

    public PacketSender(Client client, Socket connected_socket, ThreadRunner<PacketSender> thread_runner)
    {
        try {
            this.client = client;
            this.writer = new ObjectOutputStream(connected_socket.getOutputStream());
            this.user_input_reader = new BufferedReader(new InputStreamReader(System.in));
            this.thread_runner = thread_runner;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void run()
    {
        this.thread_runner.run(this);
    }
    public synchronized void send(Packet packet) throws Exception
    {
        this.writer.writeObject(packet);
        this.writer.flush();
    }
    public String getInput() throws Exception
    {
        return this.user_input_reader.readLine();
    }



    /*
        -We need to be able to differentiate between a bot and a client.
        -We should do this when a connection has been established.
        -I.e when we're sending out a validation request.
        -
     */
    public void fetchUsername() throws Exception
    {
        while(!this.client.validated())
        {
            if(!this.client.onServer()) continue;
            System.out.print("Please choose a username: ");
            String user_input = this.user_input_reader.readLine();
            Packet validation_packet = new Packet(this.user_name,user_input, Packet.PacketType.VALIDATION_REQUEST,this.client.getSenderType());
            this.send(validation_packet);
            System.out.println("Checking username...");
            Thread.sleep(1000);
            if (!this.client.validated()) continue;
            this.user_name = user_input;
            return;
        }
    }
    public void terminate()
    {
        running = false;
    }
    public ObjectOutputStream getWriter()
    {
        return this.writer;
    }
    public BufferedReader getUserInputReader()
    {
        return this.user_input_reader;
    }
    public boolean isRunning()
    {
        return this.running;
    }
    public String getUserName()
    {
        return this.user_name;
    }
    public Client getClient()
    {
        return this.client;
    }
}
