import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    protected Socket connected_socket;
    protected ServerListener listening_thread;
    protected UserListener writing_thread;
    protected boolean on_server;
    protected boolean validated;
    protected ReentrantLock mutex;
    protected boolean is_chatbot;

    public boolean isChatbot()
    {
        return this.is_chatbot;
    }

    Client() throws Exception
    {
        this.connected_socket = new Socket("localhost", 4999);
    }
    public void initialise(UserListener user_listener, ServerListener server_listener) throws Exception
    {
        this.listening_thread = server_listener;
        this.writing_thread = user_listener;
        this.mutex = new ReentrantLock();
    }
    public Socket getConnectedSocket()
    {
        return this.connected_socket;
    }

    //One at a time.
    public synchronized boolean onServer()
    {
        return this.on_server;
    }
    public void stop() throws Exception
    {
        this.mutex.lock();
        try {
            this.writing_thread.terminate();
            this.listening_thread.terminate();
            writing_thread.join();
            listening_thread.join();
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
        this.listening_thread.start();
        this.writing_thread.start();
    }
    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.initialise(new UserListener(client, client.getConnectedSocket()), new ServerListener(client, client.getConnectedSocket()));
            client.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
         }
    }
}