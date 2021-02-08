import it.unipi.lam.Room;
import it.unipi.lam.User;

import java.util.Scanner;

public class LAM {

    public static void main(String[] args){
        Room chatRoom = new Room("LAM");
        Scanner sc = new Scanner(System.in);
        while (true){
            System.out.println("Please enter a username to join our chat (x to close the app)");
            String username = sc.nextLine();
            if (username.equals("x"))
                break;
            User u = new User(username);
            System.out.println(chatRoom.getUserList());
            chatRoom.join(u);
            System.out.println(chatRoom.getUserList());

        }

    }

}
