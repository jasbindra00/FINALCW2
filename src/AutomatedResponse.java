import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AutomatedResponse
{
    private ArrayList<String> keys;
    private ArrayList<String> responses;
    public String hasResponse(String input)
    {
        String caps_input = input.toUpperCase();
        for(String key : this.keys)
        {
            if(caps_input.equals(key) || key.contains(caps_input) || caps_input.contains(key))
            {
                int randomNum = ThreadLocalRandom.current().nextInt(0,responses.size());
                return responses.get(randomNum);
            }
        }
        return "";
    }
    AutomatedResponse(ArrayList<String> keys, ArrayList<String> responses)
    {
        this.keys = keys;
        this.responses = responses;
    }
}
