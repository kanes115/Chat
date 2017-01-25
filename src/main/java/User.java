import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by Kanes on 24.01.2017.
 */
public class User {
    private String username;
    private Session session;
    private Channel currentChannel;


    public User(String username, Channel currentChannel, Session session){
        this.currentChannel = currentChannel;
        this.username = username;
        this.session = session;
    }


    public String getUsername() {
        return username;
    }

    public Session getSession() {
        return session;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    @Override
    public String toString() {
        return username;
    }


    @Override
    public boolean equals(Object obj) {
        return this.username.equals(obj.toString());
    }

    public int hashCode(){
        return username.hashCode();
    }

    public void setCurrentChannel(Channel newChannel){
        this.currentChannel = newChannel;
    }

}
