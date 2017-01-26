
import exceptions.ChannelException;
import exceptions.ChatException;
import org.eclipse.jetty.websocket.api.*;
import org.json.*;

import javax.annotation.Nullable;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {

    private List<Channel> channels = new LinkedList<>();
    private Channel mainChannel = new Channel("main chat");
    private BotChannel chatbox = new BotChannel("chatbox", this);
    private String serverName = "Server";


    public void runChat() {
        staticFiles.location("/public");
        staticFiles.expireTime(600);
        webSocket("/chat", WebSocketHandler.class);
        init();
    }


    //--- COMMUNICATING WITH USERS SESSIONS -----

    //Messages from users, casual messages that we will broadcast to all of the users on the channel
    public void broadcastMessage(String sender, String message) throws ChatException {

        Channel sendersChannel;

        if(mainChannel.hasUser(sender)){        //checking if user is in main channel
            sendersChannel = mainChannel;
        } else if(chatbox.hasUser(sender)){
            try {
                chatbox.answer(chatbox.getUser(sender), message);
            }catch(ChannelException e){
                System.out.println("This error should not appear.");
            }catch(ChatException e){
                throw new ChatException("Chat.broadcastMessage: Error while broadcatsing message. \n" + e.getMessage());
            }
            return;
        } else {                                //if not - we look for him in other channels
            sendersChannel = channels.stream().filter(channel -> channel.hasUser(sender)).collect(Collectors.toList()).get(0);
        }

        if(sendersChannel == null){             //if somehow he disappeared... exception
            throw new ChatException("Channel has not been found.");
        }

        sendersChannel.getUsers().stream().filter(user -> user.getSession().isOpen()).forEach(user -> {
            try {
                user.getSession().getRemote().sendString(prepareNormalMessage(sender, message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Write message as server, you can specify a channel to which you want to send it to!
    public void broadcastMessageAsServer(String msg, Channel toThisChannel){
        toThisChannel.getUsers().forEach(user -> broadcastMessageToUserAsServer(msg, user));

    }

    //refresh list of users and list of channels and for each user his current channel
    public void updateSessionsInfo(){

        getAllUsers().stream().filter(user -> user.getSession().isOpen()).forEach(user -> {
            try {
                user.getSession().getRemote().sendString(String.valueOf(new JSONObject()
                        .put("messageType", "updateInfoMessage")
                        .put("channellist", allChannelsExeptMain().stream().map(Channel::toString).collect(Collectors.toList()))
                        .put("userlist", getUsersChannel(user.getUsername()).getUsersAsUsernames())
                        .put("currentChannel", user.getCurrentChannel().getName())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //you can also send a message to a specific user (as server)
    public void broadcastMessageToUserAsServer(String msg, User user){

        if(!user.getSession().isOpen()){
            return;
        }

        try {
            user.getSession().getRemote().sendString(prepareNormalMessage(serverName, msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------




    public Channel getMainChannel() {
        return mainChannel;
    }

    public void addUser(User user) throws ChatException{

        try {
            if (user.getCurrentChannel().equals(this.mainChannel)) {
                this.mainChannel.addUser(user);
                return;
            }
            if (user.getCurrentChannel().equals(chatbox)) {
                this.chatbox.addUser(user);
            }

            if (!channels.contains(user.getCurrentChannel())) {
                throw new ChatException("addUser: User could not be found");
            }

            Channel toAdd = channels.stream().filter(ch -> ch.equals(user.getCurrentChannel())).collect(Collectors.toList()).get(0);

            toAdd.addUser(user);
        }catch(ChannelException e){
            throw new ChatException("Chat.addUser: " + user.getUsername() + "could not be added to the chat. \n" + e.getMessage());
        }
    }

    public void removeUser(User user) throws ChatException{

        List<Channel> chan = allChannels().stream().filter(ch -> ch.hasUser(user.getUsername())).collect(Collectors.toList());

        if(chan.size() != 1){
            throw new ChatException("Chat.removeUser: could not find user " + user.getUsername() + "\n");
        }

        Channel hisChannel = chan.get(0);

        try {
            hisChannel.removeUser(user.getSession());
        }catch(ChannelException e){
            throw new ChatException("Chat.removeUser: couldn't remove user from channel " + hisChannel.getName() + "\n" + e.getMessage());
        }

    }

    public User getUser(Session user) throws ChatException{
        try {
            if (mainChannel.hasUser(user)) {
                return mainChannel.getUser(user);
            }
            if (chatbox.hasUser(user)) {
                return chatbox.getUser(user);
            }
        }catch(ChannelException e){
            System.out.println("This error should not appear.");
        }


        try {
            Channel hisChannel = channels.stream().filter(ch -> ch.hasUser(user)).collect(Collectors.toList()).get(0);
            return hisChannel.getUser(user);
        }catch(ChannelException e){
            throw new ChatException("Chat.getUser: user could not be found. \n" + e.getMessage());
        }
    }

    public Channel getUsersChannel(String username) throws ChatException{
        if(mainChannel.hasUser(username)){
            return this.mainChannel;
        }
        if(chatbox.hasUser(username)){
            return this.chatbox;
        }

        List<Channel> chan = channels.stream().filter(ch -> ch.hasUser(username)).collect(Collectors.toList());

        if(chan.size() != 1){
            throw new ChatException("Chat.getUsersChannel: user seem to not be in any channel. \n");
        }

        return chan.get(0);
    }

    public void addChannel(Channel channel) throws ChatException {

        if(existChannel(channel)){
            throw new ChatException("Channel already exist! \n");
        }
        channels.add(channel);
    }

    public void switchChannels(Session userSession, String channelName) throws ChatException{

        try {
            User user = getUser(userSession);
            Channel oldChannel = getUsersChannel(user.getUsername());

            if (oldChannel.equals(channelName)) {
                return;
            }

            oldChannel.removeUser(user.getSession());

            if (channelName.equals(mainChannel.getName())) {
                mainChannel.addUser(user);
                user.setCurrentChannel(mainChannel);
                return;
            }
            if (channelName.equals(chatbox.getName())) {
                chatbox.addUser(user);
                user.setCurrentChannel(chatbox);
                return;
            }

            List<Channel> chan = allChannels().stream().filter(ch -> ch.equals(channelName)).collect(Collectors.toList());

            if (chan.size() < 1) {
                //exception
                return;
            }

            Channel thisChannel = chan.get(0);

            thisChannel.addUser(user);

            user.setCurrentChannel(thisChannel);
        }catch (ChannelException e){
            throw new ChatException("Chat.switchChannels: Accoured problems when switching channels. \n" + e.getMessage());
        }

    }


    // --- PRIVATES ---

    private String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    private List<User> getAllUsers(){
        return allChannels().stream().map(ch -> ch.getUsers()).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<Channel> allChannels(){
        List<Channel> res = new LinkedList<>(channels);
        res.add(this.mainChannel);
        res.add(this.chatbox);
        return res;
    }

    private List<Channel> allChannelsExeptMain(){
        List<Channel> res = new LinkedList<>(channels);
        res.add(this.chatbox);
        return res;
    }

    private boolean existChannel(Channel channel){
        return channels.contains(channel) || channel.equals(mainChannel) || channel.equals(chatbox);
    }

    private String prepareNormalMessage(String sender, String msg){
        try{
        return String.valueOf(new JSONObject()
                .put("messageType", "normalMessage")
                .put("userMessage", createHtmlMessageFromSender(sender, msg)));
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

}