import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class Server{

    private ArrayList<CommandInfo> server_commands;
    private ArrayList<InetSocketAddress> banned_clients;
    private ArrayList<SocketHandler> client_endpoints;
    private ServerSocket input_socket;
    private Socket tmp_output_socket;
    private Integer max_client_ID;
    private boolean is_running;
    private ReentrantLock mutex;
    public static int strike_threshold = 3;
    public static int dod_client_id = 0;
    public Server()
    {
        this.mutex = new ReentrantLock();
        this.banned_clients = new ArrayList<>();
        this.server_commands = new ArrayList<>();

    }
    public String foo(ArrayList<String> str)
    {
        System.out.println("Foo");
        return "kicking user.";
    }
    public void checkServerCommand(String input)
    {
        for(CommandInfo command : this.server_commands)
        {


        }
    }
    private void init(InetSocketAddress address) throws Exception
    {
        this.client_endpoints = new ArrayList<>();
        this.max_client_ID = 0;
        this.is_running = false;
        this.input_socket = new ServerSocket(address.getPort(), 0, address.getAddress());
        this.is_running = true;

    }
    public boolean initialise()
    {
        if(this.is_running) return true;
        try {
            BufferedReader console_reader = new BufferedReader(new InputStreamReader(System.in));
            Utility.print("Enter desired port number: ");
            String port_number = console_reader.readLine();
            Utility.print("Enter desired IP address or name: ");
            String address = console_reader.readLine();
            Utility.print("Waking Apollo...");
            Thread.sleep(1000);
            Utility.print("Oiling robots...");
            this.init(new InetSocketAddress(address, Integer.parseInt(port_number)));
            Thread.sleep(2000);
            Utility.print("Apollo is up.");
            this.start();
            return true;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }

    }
    private SocketHandler getClient(int client_ID)
    {
        return this.client_endpoints.stream().filter(client -> client.getClientID() == client_ID).findAny().orElse(null);
    }
    private SocketHandler getClient(String client_name)
    {
        return this.client_endpoints.stream().filter(client -> client.getClientName().equals(client_name)).findAny().orElse(null);
    }
    //Called by a given thread to remove the client. This is called after a disconnection message has been sent to the client.
    public void removeClient(Integer client_ID) throws Exception
    {
        this.mutex.lock();
        try {
            //Explanation: Find the client handler with the given client ID.
            SocketHandler client_configuration = this.getClient(client_ID);
            //Client did not exist.
            if(client_configuration == null) return;
            //Explanation: Stop the inner thread loop from running.
            client_configuration.terminate();
            //Join the thread back together.
            client_configuration.join();
            //Remove the client_configuration from our array.
            this.client_endpoints.remove(client_ID);
        }
        finally{
            this.mutex.unlock();
        }
    }
    //Called by the sockethandler when receiving a message from a given user.
    public void send(Packet packet, int to_client_ID)
    {
        this.mutex.lock();
        try{
            SocketHandler client_handler = this.getClient(to_client_ID);
            if(client_handler== null)
            {
                //Tried to send to client which didn't exist.
                return;
            }
            client_handler.handlePacket(packet);
        }
        finally{
            this.mutex.unlock();
        }
    }
    public void send(Packet packet, String to_client_name)
    {
        this.mutex.lock();
        try{

            SocketHandler client_handler = this.getClient(to_client_name);
            if(client_handler== null)
            {
                //Tried to send to client which didn't exist.
                return;
            }
            client_handler.handlePacket(packet);
        }
        finally{
            this.mutex.unlock();
        }
    }
    public void broadcast(Packet packet, int sent_from_ID) throws Exception {
        //Do some non specific stuff
        this.mutex.lock();
        try {
            for (SocketHandler handler : this.client_endpoints) {
                if (handler.getClientID() == sent_from_ID || handler.isInGame()) continue;
                handler.handlePacket(packet);
            }
        }
        finally{
            this.mutex.unlock();
        }
    }
    private Packet checkUserBan(Socket output_socket)
    {
        InetAddress client_IP = output_socket.getInetAddress();
        int client_port = output_socket.getPort();
        Packet response_packet = new Packet();
        for (InetSocketAddress banned_config : this.banned_clients)
        {
            if (banned_config.getAddress().toString().equals(client_IP.toString()) && banned_config.getPort() == client_port) {
                return new Packet("Server","Unauthorised request. You have banned from the server because of past behaviour.", Packet.PacketType.CONNECT_DENIED);
            }
        }
        return new Packet("Server","Welcome to the Apollo Server. Please enter your desired username.", Packet.PacketType.CONNECT_GRANTED);
    }
    public boolean usernameExists(String username, int caller_ID)
    {
        for(SocketHandler handler : this.client_endpoints)
        {
            if(handler.getClientID() != caller_ID && handler.getClientName().equals(username)) return true;
        }
        return false;
    }
    public void addClient(Socket output_socket) throws Exception
    {
        Utility.print("Attempting to add user to database.");
        Packet handshake = this.checkUserBan(output_socket);
        if(handshake.packet_type == Packet.PacketType.CONNECT_GRANTED)
        {
            ++max_client_ID;
            SocketHandler client_handler = new SocketHandler(this, output_socket, max_client_ID, handshake);
            this.client_endpoints.add(client_handler);
            client_handler.start();
            Utility.print("Added user to database.");
            return;
        }
        ObjectOutputStream out = new ObjectOutputStream(output_socket.getOutputStream());
        out.writeObject(handshake);
        out.flush();
        output_socket.close();
    }
    public void start() {
        try {
            while(true)
            {
                this.tmp_output_socket = input_socket.accept();
                Utility.print("Found client connection.");
                this.addClient(this.tmp_output_socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                input_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Server server = new Server();
        while(!server.initialise())
        {
        }
    }
    public static String sensorBannedWords(String user_message)
    {
        ArrayList<String> banned_words = new ArrayList<String>();
        banned_words.add("DARN");
        banned_words.add("JAVA");
        banned_words.add("PYTHON");
        banned_words.add("FRICK");
        banned_words.add("YEET");
        banned_words.add("CRAP");

        ArrayList<String> split = new ArrayList<String>(Arrays.asList(user_message.split(" ")));
        for(int i = 0; i < split.size(); ++i)
        {
            String word = split.get(i);
            for(String banned_word : banned_words)
            {
                String upper_word = word.toUpperCase();

                if(upper_word.equals(banned_word))
                {
                    split.set(i,new String("*").repeat(word.length()));
                }
            }
        }
        return String.join(" ", split);
    }
}
