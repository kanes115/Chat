import exceptions.ChannelException;
import org.eclipse.jetty.websocket.api.Session;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Comparing users is based on their usernames.
 */
public class Channel {
    private String name;
    private List<User> users = new LinkedList<>();

    public Channel(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public Boolean hasUser(String username){
        return users.stream().anyMatch(c -> c.getUsername().equals(username));
    }

    public Boolean hasUser(Session user){
        return users.stream().anyMatch(c -> c.getSession().equals(user));
    }

    public List<User> getUsers() {
        return users;
    }

    public List<String> getUsersAsUsernames(){
        return users.stream().map(User::toString).collect(Collectors.toList());
    }


    public void addUser(User user) throws ChannelException {
        if(users.contains(user)){
            throw new ChannelException("Channel.addUser: This user already exists! \n");
        }
        users.add(user);
    }

    //if user does not exist - do nothing
    public void removeUser(Session user) throws ChannelException{
        if(!hasUser(user)){
            throw new ChannelException("Channel.removeUser: User does not exist on this channel. \n");
        }
        users = users.stream().filter(u -> !(u.getSession().equals(user))).collect(Collectors.toList());
    }

    public User getUser(Session user) throws ChannelException{
        if(!hasUser(user)){
            throw new ChannelException("Channel.getUser: User does not exist on this channel. \n");
        }
        return users.stream().filter(u -> u.getSession().equals(user)).collect(Collectors.toList()).get(0);
    }

    public User getUser(String username) throws ChannelException {
        if(!hasUser(username)){
            throw new ChannelException("Channel.getUser: User " + username + "does not exist on this channel. \n");
        }
        return users.stream().filter(u -> u.getUsername().equals(username)).collect(Collectors.toList()).get(0);
    }




    @Override
    public boolean equals(Object obj) {
        return this.name.equals(obj.toString());
    }

    public int hashCode(){
        return name.hashCode();//for simplicity reason
    }

    @Override
    public String toString() {
        return name;
    }
}
