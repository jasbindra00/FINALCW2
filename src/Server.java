import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class Server{

    private ArrayList<InetSocketAddress> banned_clients;
    private ArrayList<SocketHandler> client_endpoints;
    private ArrayList<String> sensored_words;

    private ServerSocket input_socket;
    private Socket tmp_output_socket;
    private Integer max_client_ID;
    private boolean is_running;
    private ReentrantLock mutex;
    public static final int strike_threshold = 3;

    public Server(ArrayList<String> sensored_words)
    {
        this.sensored_words = sensored_words;
        this.mutex = new ReentrantLock();
        this.banned_clients = new ArrayList<>();
        this.client_endpoints = new ArrayList<>();
        this.max_client_ID = 0;
        this.is_running = false;
    }
    private void introduceServer() throws Exception
    {
        ArrayList<String> dialogue = new ArrayList<String>(){{
        add("Waking Apollo...");
        add("Swapping space and time...");
        add("Feeding monkeys...");
        add("Checking flux capacitors...");
        add("Apollo is up.");
        }};
        for(String string : dialogue)
        {
            Utility.print(string);
            Thread.sleep(1500);
        }
    }
    public void launch()
    {
        if(this.is_running) return;
            while(!this.is_running)
            {
                BufferedReader console_reader = new BufferedReader(new InputStreamReader(System.in));
                try
                {
                    Utility.print("Enter desired port number: ");
                    String port_number = console_reader.readLine();
                    Utility.print("Enter desired IP address or name: ");
                    String address = console_reader.readLine();
                    this.input_socket = new ServerSocket(Integer.parseInt(port_number), 0, InetAddress.getByName(address));
                    this.is_running = true;
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
            try
            {
                ArrayList<String> dialogue = new ArrayList<String>(){{
                    add("Waking Apollo...");
                    add("Swapping space and time...");
                    add("Feeding monkeys...");
                    add("Checking flux capacitors...");
                    add("Apollo is up.");
                }};
                for(String string : dialogue)
                {
                    Utility.print(string);
                    Thread.sleep(1000);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            this.listen();

            //Add the DoD client here.

        }

    public void listen() {
            while(this.is_running)
            {
                try { this.tmp_output_socket = input_socket.accept(); }
                catch(Exception e) { e.printStackTrace(); }
                Thread thread = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try { addClient(tmp_output_socket); }
                        catch(Exception e) {e.printStackTrace();}
                    }
                };
                thread.start();
                //this.addClient(this.tmp_output_socket);
            }
            try { this.input_socket.close(); }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    public synchronized void send(Packet packet, int to_client_ID)
    {
        SocketHandler client_handler = this.getClient(to_client_ID);
        if(client_handler == null) return;
        client_handler.handlePacket(packet);
    }
    public synchronized void send(Packet packet, String client_name) {
        SocketHandler client_handler = this.getClient(client_name);
        if (client_handler == null) return;
        client_handler.handlePacket(packet);
    }


    public synchronized void broadcast(Packet packet, int sent_from_ID) throws Exception
    {
        for (SocketHandler handler : this.client_endpoints)
        {
            if (handler.isinDoNotDisturbMode() || handler.getClientID() == sent_from_ID) continue;
            handler.handlePacket(packet);
        }
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
    public void removeClient(Integer client_ID, boolean ban) throws Exception
    {
        this.mutex.lock();
        try {
            //Explanation: Find the client handler with the given client ID.
            SocketHandler client_configuration = this.getClient(client_ID);
            //Client did not exist.
            if(client_configuration == null) return;
            //Explanation: Stop the inner thread loop from running.
            if(!ban)
            {
                Packet broadcast_packet = new Packet("Server", client_configuration.getClientName() + " has left the server. Say goodbye!", Packet.PacketType.BROADCAST, SenderType.SERVER);
                this.broadcast(broadcast_packet, -1);
            }
            Packet goodbye_packet = new Packet("Server", "Apollo thanks you for your stay. Take care.", Packet.PacketType.DISCONNECT_GRANTED, SenderType.SERVER);
            client_configuration.handlePacket(goodbye_packet);
            if(ban) this.banned_clients.add(client_configuration.getClientConfig());
            client_configuration.terminate();
            //Remove the client_configuration from our array.
            this.client_endpoints.remove(client_configuration);
        }
        finally{
            this.mutex.unlock();
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
    private Packet checkUserBan(Socket output_socket)
    {
        InetAddress client_IP = output_socket.getInetAddress();
        int client_port = output_socket.getPort();
        for (InetSocketAddress banned_config : this.banned_clients)
        {
            if (banned_config.getAddress().toString().equals(client_IP.toString()) && banned_config.getPort() == client_port)
            {
                return new Packet("Server","Unauthorised request. You have banned from the server because of past behaviour.", Packet.PacketType.CONNECT_DENIED, SenderType.SERVER);
            }
        }
        return new Packet("Server","Welcome to the Apollo Server. Please enter your desired username.", Packet.PacketType.CONNECT_GRANTED, SenderType.SERVER);
    }
    public boolean usernameExists(String username, int caller_ID)
    {
        for(SocketHandler handler : this.client_endpoints)
        {
            if(handler.getClientID() != caller_ID && handler.getClientName().equals(username)) return true;
        }
        return false;
    }
    public int getDoDClientID()
    {
        return 3;
    }
    public ArrayList<String> getBannedWords()
    {
        return this.sensored_words;
    }
    public static void main(String[] args) {
        new ArrayList<String>(){{add("");}};
        Server server = new Server(new ArrayList<>(){{add("DARN"); add("JAVA"); add("PYTHON"); add("FRICK"); add("YEET"); add("CRAP");}});
        server.launch();
    }


}
