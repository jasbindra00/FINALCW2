import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;


public class UserListener extends Thread{
    protected ObjectOutputStream writer;
    protected BufferedReader user_input_reader;
    protected String user_name = "Guest";
    protected Client client;
    protected boolean running = true;
    protected Packet.PacketType expecting_response;
    protected void terminate()
    {
        running = false;
    }
    public UserListener(Client client,Socket client_socket) throws Exception
    {
        this.client = client;
        this.writer = new ObjectOutputStream(client_socket.getOutputStream());
        this.user_input_reader = new BufferedReader(new InputStreamReader(System.in));
    }
    public String getInput() throws Exception
    {
        return this.user_input_reader.readLine();
    }
    public Packet encodeMessage(Packet.PacketType packet_type, String data)
    {
        return new Packet(this.user_name,data, packet_type);
    }
    public void send(Packet packet) throws Exception
    {
        this.writer.writeObject(packet);
        this.writer.flush();
    }
    public void fetchUsername() throws Exception
    {

        while(!this.client.validated()) {
            if(!this.client.onServer()) continue;
            System.out.print("Please choose a username: ");
            String user_input = this.user_input_reader.readLine();
            this.send(this.encodeMessage(Packet.PacketType.VALIDATION_REQUEST, user_input));
            System.out.println("Checking username...");
            Thread.sleep(1000);
            if (!this.client.validated()) continue;
            this.user_name = user_input;
            return;
        }
    }
    @Override
    public void run()
    {

        try {
            this.fetchUsername();
            String input;
            while (running)
            {
                //The client needs to choose a user name.
                input = this.getInput();
                this.send(this.encodeMessage(Packet.PacketType.MESSAGE, input));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
