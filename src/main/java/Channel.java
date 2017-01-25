import org.eclipse.jetty.websocket.api.Session;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Kanes on 24.01.2017.
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


    public void addUser(User user){
        if(users.contains(user)){
            //exception
        }
        users.add(user);
    }

    public void removeUser(Session user){
        users = users.stream().filter(u -> !(u.getSession().equals(user))).collect(Collectors.toList());
    }

    public User getUser(Session user){
        return users.stream().filter(u -> u.getSession().equals(user)).collect(Collectors.toList()).get(0);
    }

    public User getUser(String username){
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
