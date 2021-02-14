package it.unipi.lam.client0;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import it.unipi.lam.User;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ClientSender implements Runnable{
    private Client c;

    public ClientSender(Client c) throws IOException {
        this.c = c;
    }

    @Override
    public void run() {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("To send on the room press r, to private message press p");
            String ans = sc.nextLine();
            if (ans.equals("r")){
                this.c.send(this.c.getChatRoom().getRoomName(), true);
            }
            else if (ans.equals("p")){
                boolean sent = false;
                System.out.println("write the username");
                ans = sc.nextLine();
                List<User> chatUsers = this.c.getChatRoom().getUserList();
                for (User u: chatUsers){
                    if (u.getUsername().equals(ans)){
                        this.c.send(u.getUsername(), false);
                        sent = true;
                        break;
                    }
                }
                if(!sent) System.out.println("Username does not exist in this chat room");
            }
        } catch (OtpErlangExit otpErlangExit) {
            otpErlangExit.printStackTrace();
        } catch (OtpErlangDecodeException e) {
            e.printStackTrace();
        }
    }
}
