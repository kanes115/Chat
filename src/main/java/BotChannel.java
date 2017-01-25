import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static j2html.TagCreator.span;

/**
 * Created by Kanes on 25.01.2017.
 */
public class BotChannel extends Channel {


    private Random ran = new Random();
    private Chat chat;
    private static LinkedList<String> wordsAboutTime = new LinkedList<>();
    private static LinkedList<String> wordsAboutWeather = new LinkedList<>();
    private static LinkedList<String> wordsAboutWeekdays = new LinkedList<>();

    public BotChannel(String name, Chat chat) {
        super(name);
        String []time = {"time", "hour", "clock"};
        wordsAboutTime.addAll(Arrays.asList(time));
        String []weather = {"weather", "rain", "sunny", "humidity"};
        wordsAboutWeather.addAll(Arrays.asList(weather));
        String []weekdays = {"day", "week", "weekday"};
        wordsAboutWeekdays.addAll(Arrays.asList(weekdays));

        this.chat = chat;
    }

    public void answer(User user, String question){
        String answer = "I don't understand...";


        String []words = question.split(" ");
        List<String> wordsL = Arrays.asList(words);


        if(wordsL.stream().anyMatch(BotChannel::aboutTime)){
            System.out.println("ddd");
            answer = "The time is: " + (new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }else if(wordsL.stream().anyMatch(BotChannel::aboutWeather)){
            int los = ran.nextInt(3);

            switch(los){
                case 0:
                    answer = "It will rain. Remember about umbrella.";
                    break;
                case 1:
                    answer = "It will be sunny. Enjoy.";
                    break;
                case 2:
                    answer = "It will snow.";
                    break;
                default:
                    break;
            }
        }else if(wordsL.stream().anyMatch(BotChannel::aboutWeekdays)){
            Calendar c = Calendar.getInstance();
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);


            switch(dayOfWeek){
                case 1:
                    answer = "It is Monday";
                    break;
                case 2:
                    answer = "It is Tuesday";
                    break;
                case 3:
                    answer = "It is Wednesday";
                    break;
                case 4:
                    answer = "It is Thursday. Best day to pass Programowanie Obiektowe.";
                    break;
                case 5:
                    answer = "It is Friday";
                    break;
                case 6:
                    answer = "It is Sutarday";
                    break;
                case 7:
                    answer = "It is Sunday";
                    break;
                default:
                    break;
            }
        }


        this.chat.broadcastMessageToUser(answer, user);
    }


    private static boolean aboutTime(String word){
        return wordsAboutTime.stream().anyMatch(w -> w.equals(word));
    }

    private static boolean aboutWeather(String word){
        return wordsAboutWeather.stream().anyMatch(w -> w.equals(word));
    }

    private static boolean aboutWeekdays(String word){
        return wordsAboutWeekdays.stream().anyMatch(w -> w.equals(word));
    }

}
