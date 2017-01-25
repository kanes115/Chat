import java.util.concurrent.TimeUnit;

/**
 * Created by Kanes on 24.01.2017.
 */
public class Main {

    public static void main(String []args){
        WebSocketHandler socket = new WebSocketHandler();
        socket.run();
    }
}
