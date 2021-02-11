package it.unipi.lam;

import com.ericsson.otp.erlang.*;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private final OtpNode node;
    private final OtpMbox mbox;

    private static Client c = null;

    private String nodename = "ahmed@localhost";
    private String mboxname = "ahmedbox";
    private String cookie = "";
    private User user;

    private String servername = "avg_server";
    private String servermbox = "server@ahmed-HP-15-NoteBook-PC";


    public User getUser() {
        return user;
    }

    public String getServerMbox() {
        return servermbox;
    }

    public String getServername() {
        return servername;
    }


    private Client(User user) throws IOException {
        if(cookie==""){
            this.node = new OtpNode(this.nodename);
        }
        else{
            this.node = new OtpNode(this.nodename, this.cookie);
        }

        this.mbox = this.node.createMbox(this.mboxname);
        this.user = user;
    }

    public static Client getInstance(User u) throws IOException {
       if (c == null) throw new RuntimeException();
       else {
           c = new Client(u);
           return c;
       }
    }

    public static void joinChatRoom(Room r){
            OtpErlangString username = new OtpErlangString(c.getUser().getUsername());
            OtpErlangString msgType = new OtpErlangString("connect");

            OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{c.mbox.self(), msgType, username});

            c.mbox.send(c.getServername(), c.getServerMbox(), outMsg);
    }

    public static void receive(Room r) throws OtpErlangExit, OtpErlangDecodeException {
        OtpErlangObject msg = c.mbox.receive();
        OtpErlangTuple t = (OtpErlangTuple) msg;

        System.out.println("msg received: " + msg.toString());

        OtpErlangString username = (OtpErlangString) t.elementAt(1);
        OtpErlangString message = (OtpErlangString) t.elementAt(2);

        //need to include datetime later

        User sender = new User(username.toString());
        Date date = new Date();
        Message m = new Message(sender, message.toString(), date);

        r.sendMessage(m);

    }

    public static void main(String[] args) throws IOException, OtpErlangExit, OtpErlangDecodeException {

            Room chatRoom = new Room("LAM");
            Scanner sc = new Scanner(System.in);
            String username;
            boolean joined;
            User u = null;
            do {
                System.out.println("Please enter a username to join our chat (x to close the app)");
                username = sc.nextLine();
                if (username.equals("x"))
                    break;
                u = new User(username);
                joined = chatRoom.join(u);
            } while (!username.isEmpty() || !joined);
            getInstance(u);
            joinChatRoom(chatRoom);
            while (true){
                receive(chatRoom);
            }
        }
}
