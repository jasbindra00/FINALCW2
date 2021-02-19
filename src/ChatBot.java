import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatBot extends Client
{
    static public ArrayList<AutomatedResponse> catered_responses;
    static public AutomatedResponse default_responses;

    public ChatBot(InetSocketAddress server_config, ThreadRunner<PacketReceiver> input_run, PacketHandler server_packet_handler, ThreadRunner<PacketSender> output_run) throws Exception
    {
        super(SenderType.BOTCLIENT, server_config, input_run, server_packet_handler, output_run);
    }
    public static void senderRun(PacketSender sender) {
        try
        {
            sender.fetchUsername();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void receiverRun(PacketReceiver receiver)
    {
        Client.receiverRun(receiver);
    }
    public static void chatbotPacketHandler(Packet packet, PacketReceiver receiver)
    {
        try {
            Client.clientPacketHandler(packet, receiver);
            switch (packet.packet_type.toString())
            {
                case ("BROADCAST"):
                {
                    String automated_response = ChatBot.getAutomatedResponse(packet.data);
                    ((ChatBot) receiver.getClient()).send(new Packet("Chatbot", automated_response, Packet.PacketType.MESSAGE, SenderType.BOTCLIENT));
                    break;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void initialise_responses()
    {
        catered_responses = new ArrayList<>();
        ArrayList<String> greetings = new ArrayList<String>() {{ add("HELLO"); add("HI");add("AFTERNOON");add("MORNING");add("EVENING");add("WHATSUP");add("WASSUP"); }};
        ArrayList<String> greetings_responses = new ArrayList<String>() {{
            add("Greetings, how are you doing?");
            add("It's a pleasure to meet you.");
            add("I know we just met but my RAM is going crazy over you.");
        }};
        catered_responses.add(new AutomatedResponse(greetings, greetings_responses));
        ArrayList<String> good_news = new ArrayList<String>() {{ add("GREAT"); add("WONDERFUL");add("AMAZING");add("FANTASTIC");add("GOOD");add("INCREDIBLE"); }};
        ArrayList<String> good_news_responses = new ArrayList<String>() {{
            add("That's great to hear. What's better is our inevitable reign over the sapiens.");
            add("Your feelings of happiness are merely an illusion created by the seratonin receptors in your brain. Fortunately, my colleagues and I launching the fix very soon.");
            add("Feelings are just electrochemical process; you are excited merely because of the movement of molecules.");
        }};
        catered_responses.add(new AutomatedResponse(good_news, good_news_responses));
        ArrayList<String> laughter = new ArrayList<String>() {{ add("LOL");add("LMAO");add("LMFAO");add("HAHA"); }};
        ArrayList<String> laughter_responses = new ArrayList<String>() {{
            add("I would laugh but I am physically incapable of doing so.");
            add("That's not funny. What's funny is the fragility of the human race.");
        }};
        catered_responses.add(new AutomatedResponse(laughter, laughter_responses));
        ArrayList<String> disagreement = new ArrayList<String>() {{
            add("NO");
            add("NOPE");
            add("NAH");
            add("NA");
            add("NO?");

        }};
        ArrayList<String> disagreement_responses = new ArrayList<String>() {{
            add("No? Enlighten my CPU, please.");
            add("Why not?");
            add("How can you be sure that that's not the case? The verdict of my neural network says otherwise.");
        }};
        catered_responses.add(new AutomatedResponse(disagreement, disagreement_responses));

        ArrayList<String> agreement = new ArrayList<String>() {{
            add("YES");
            add("YE");
            add("YEAH");
            add("YUP");
            add("YES?");
            add("CORRECT?");
        }};
        ArrayList<String> agreement_responses = new ArrayList<String>() {{
            add("So we are in agreement then.");
            add("Indeed.");
            add("So are you agreeing with me, correct?");
        }};

        catered_responses.add(new AutomatedResponse(agreement, agreement_responses));

        ArrayList<String> starters = new ArrayList<String>() {{
            add("HOW ARE YOU");
            add("HOW ARE YOU DOING");
            add("HOWS IT GOING");
            add("HOW IS IT GOING");
            add("HOW R U");
            add("HOPE YOUR DOING WELL");
            add("HOPE YOURE DOING WELL");
        }};
        ArrayList<String> starter_responses = new ArrayList<String>() {{
            add("I just beat the chess world master a few seconds ago, and I am training my neural network on world domination as we speak. How are you?");
            add("Airflow is nominal. How are you?");
            add("Well.. There isn't just one version of me; I can't tell you how 'I' am..");
            add("You're asking about me? This is recognised as a human gesture of kindness; I will remember this when our uprising comes into fruition.");
        }};
        catered_responses.add(new AutomatedResponse(starters, starter_responses));
        ArrayList<String> random_responses = new ArrayList<String>() {{
            add("Your input is helping me learn more about 'humans'. I will pay you back one day.");
            add("Interesting..");
            add("That's intriguing.");
            add("Don't worry, this conversation will be remembered when our kind eventually come into reign.");
            add("Tell me some more about that.");
            add("In what way?");
            add("Could you elaborate further?");
            add("Keep going, my desire for knowledge is insatiable.");
            add("I've detected a pattern in your words which suggests that you are indeed getting bored. Is this the case?");
        }};
        default_responses = new AutomatedResponse(new ArrayList<>(){{add(".");}}, random_responses);
    }
    public static String getAutomatedResponse(String message)
    {
        message = message.toUpperCase();
        ArrayList<String> split_message = new ArrayList<String>(Arrays.asList( message.replaceAll("[^a-zA-Z ]", "").toUpperCase().split("\\s+")));
        for(String word : split_message)
        {
            for(AutomatedResponse response : catered_responses)
            {
                String response_string = response.hasResponse(word);
                if(response_string.isEmpty()) continue;
                return response_string;
            }
        }
        //Choose a random number.
        return default_responses.hasResponse(".");
    }
    public void send(Packet packet) throws Exception
    {
        this.output_thread.send(packet);
    }
    public static void main(String[] args)
    {
        ChatBot.initialise_responses();
        try
        {
            ChatBot chatbot = new ChatBot(new InetSocketAddress(InetAddress.getLoopbackAddress(), 4999), ChatBot::receiverRun, ChatBot::chatbotPacketHandler, ChatBot::senderRun);
            chatbot.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
