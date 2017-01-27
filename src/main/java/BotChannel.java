import exceptions.ChatException;

import java.text.SimpleDateFormat;
import java.util.*;

import static j2html.TagCreator.span;

/**
 * Created by Kanes on 25.01.2017.
 */
public class BotChannel extends Channel {


    private Chat chat;
    private LinkedList<String> wordsAboutTime = new LinkedList<>();
    private LinkedList<String> wordsAboutWeather = new LinkedList<>();
    private LinkedList<String> wordsAboutWeekdays = new LinkedList<>();

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

    public void answer(User user, String question) throws ChatException {
        String answer = "I don't understand...";


        String []words = question.split(" ");
        List<String> wordsL = Arrays.asList(words);


        if(wordsL.stream().anyMatch(this::aboutTime)){
            answer = "The time is: " + (new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }else if(wordsL.stream().anyMatch(this::aboutWeather)){


            WeatherBuilder weather = new WeatherBuilder();
            answer = weather.getWeather();

        }else if(wordsL.stream().anyMatch(this::aboutWeekdays)){
            Calendar c = Calendar.getInstance();
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);


            switch(dayOfWeek){
                case 1:
                    answer = "It is Sunday";
                    break;
                case 2:
                    answer = "It is Monday";
                    break;
                case 3:
                    answer = "It is Tuesday";
                    break;
                case 4:
                    answer = "It is Wednesday";
                    break;
                case 5:
                    answer = "It is Thursday.";
                    break;
                case 6:
                    answer = "It is Friday";
                    break;
                case 7:
                    answer = "It is Sutarday";
                    break;
                default:
                    break;
            }
        }


        this.chat.broadcastMessageToUserAsServer(answer, user);
    }


    private boolean aboutTime(String word){
        return wordsAboutTime.stream().anyMatch(w -> w.equals(word));
    }

    private boolean aboutWeather(String word){
        return wordsAboutWeather.stream().anyMatch(w -> w.equals(word));
    }

    private boolean aboutWeekdays(String word){
        return wordsAboutWeekdays.stream().anyMatch(w -> w.equals(word));
    }

}
