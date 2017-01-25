import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import spark.Request;

import java.util.stream.Collectors;


/**
 * Created by Kanes on 24.01.2017.
 */

@WebSocket
public class WebSocketHandler {

    private String sender, msg;
    private Chat chat = new Chat();

    private String createChannelCmd = "$Create_channel";
    private String switchChannelCmd = "$Change_forChannel";
    private String cmdDelimeter = "::";

    public WebSocketHandler(){
        System.out.print("ss");
    }

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = user.getUpgradeRequest().getCookies().stream().filter(c -> c.getName().equals("username")).collect(Collectors.toList()).get(0).getValue();

        User newUser = new User(username, chat.getMainChannel(), user);

        chat.addUser(newUser);
        chat.broadcastControlMessage();
        chat.broadcastMessageAsServer(msg = (username + " joined the chat"), chat.getUsersChannel(username));
    }

    @OnWebSocketClose
    public void onClose(Session userSession, int statusCode, String reason) {
        User user = chat.getUser(userSession);
        chat.broadcastMessageAsServer(msg = (user.getUsername() + " left the chat"), chat.getUsersChannel(user.getUsername()));
        chat.removeUser(user);
        chat.broadcastControlMessage();
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        System.out.println("message: " + message);

        if(isCreateChannel(message)){

            chat.addChannel(new Channel(message.split(cmdDelimeter)[1]));
            chat.broadcastControlMessage();

        } else if (isSwitchChannels(message)){

            chat.switchChannels(user, message.split(cmdDelimeter)[1]);

            chat.broadcastControlMessage();

        } else {

            chat.broadcastMessage(sender = chat.getUser(user).getUsername(), msg = message);

        }
    }



    private boolean isCreateChannel(String message) {
        return message.split(cmdDelimeter).length > 1 && message.split(cmdDelimeter)[0].equals(createChannelCmd);
    }

    private boolean isSwitchChannels(String message){
        return message.split(cmdDelimeter).length > 1 && message.split(cmdDelimeter)[0].equals(switchChannelCmd);
    }


    public void run(){
        chat.runChat();
    }

}
