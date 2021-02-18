import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import java.util.HashMap;
import java.util.Map;

public class ChatBot extends Client
{
    static public ArrayList<AutomatedResponse> catered_responses;
    static public AutomatedResponse default_responses;
    static public void initialise_responses()
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

    public ChatBot() throws Exception
    {
        super();
    }
    public static String getAutomatedResponse(String message)
    {
        message = message.toUpperCase();
        //Remove all punctuation
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

    void send(Packet packet) throws Exception
    {
        this.writing_thread.send(packet);
    }
    public static void main(String[] args)
    {
        try {
            ChatBot chatbot = new ChatBot();
            UserListener user_listener = new UserListener(chatbot, chatbot.getConnectedSocket()) {
                @Override
                public void run()
                {
                    try {
                        this.fetchUsername();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    //This does not need to be anything at all since our response is a function of the user.
                }
            };
            ServerListener server_listener = new ServerListener(chatbot, chatbot.getConnectedSocket())
            {
                //Listens for server requests on thread.
                //Handles and outputs to the server requests on another thread.
                @Override
                public void handlePacket(Packet packet) throws Exception
                {
                    super.handlePacket(packet);
                    switch(packet.packet_type.toString())
                    {
                        case("BROADCAST"):
                        {
                            //Recieves a broadcast.
                            //Get the automated response based on the message.
                            String automated_response = ChatBot.getAutomatedResponse(packet.data);
                            ((ChatBot)this.client).send(new Packet("Chatbot", automated_response, Packet.PacketType.MESSAGE));
                            //Route this message to the reader.
                        }
                    }
                }
            };
            ChatBot.initialise_responses();
            chatbot.initialise(user_listener,server_listener);
            chatbot.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }






}
