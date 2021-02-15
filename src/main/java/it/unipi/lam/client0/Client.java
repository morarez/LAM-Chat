package it.unipi.lam.client0;

import com.ericsson.otp.erlang.*;
import it.unipi.lam.Message;
import it.unipi.lam.Room;
import it.unipi.lam.User;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;


public class Client{
    private final OtpNode node;
    private final OtpMbox mbox;

//    private String nodename = "ahmed@localhost";
//    private String mboxname = "ahmedbox";
//    private String cookie = "";

//    private String servername = "lamchat";
//    private String servermbox = "server@ahmed-HP-15-NoteBook-PC";

    private String nodename;
    private String mboxname;
    private String cookie;

    private String servername;
    private String servermbox;

    //local variables
    private User user;
    private Room chatRoom;


    public Client(String nodename, String mboxname, String cookie, String servername, String servermbox) throws IOException {
        this.cookie = cookie;
        this.nodename = nodename;
        this.mboxname = mboxname;
        if(this.cookie==""){
            this.node = new OtpNode(this.nodename);
        }
        else{
            this.node = new OtpNode(this.nodename, this.cookie);
        }
        this.mbox = this.node.createMbox(this.mboxname);
        this.servermbox = servermbox;
        this.servername = servername;
    }

    public String getNodename() { return nodename; }
    public String getMboxname() { return mboxname; }
    public String getCookie() { return cookie; }

    public OtpMbox getMbox() { return mbox; }
    public OtpNode getNode() { return node; }

    public String getServerMbox() {
        return servermbox;
    }
    public String getServername() {
        return servername;
    }

    public User getUser() {
        return user;
    }
    public Room getChatRoom(){
        return chatRoom;
    }


    public void setNodename(String nodename) { this.nodename = nodename; }
    public void setMboxname(String mboxname) { this.mboxname = mboxname; }
    public void setCookie(String cookie) { this.cookie = cookie; }

    public void setServermbox(String servermbox) { this.servermbox = servermbox; }
    public void setServername(String servername) { this.servername = servername; }

    public void setChatRoom(Room chatRoom) {
        this.chatRoom = chatRoom;
    }
    public void setUser(User user) {
        this.user = user;
    }




    public void receive() throws OtpErlangExit, OtpErlangDecodeException {
        OtpErlangObject msg = this.mbox.receive();
        OtpErlangTuple t = (OtpErlangTuple) msg;

        System.out.println("msg received: " + msg.toString());

        OtpErlangAtom msgType = (OtpErlangAtom) t.elementAt(0);


        OtpErlangAtom info = new OtpErlangAtom("info");
        OtpErlangAtom new_msg = new OtpErlangAtom("new_msg");

        if(msgType.equals(info)){
            System.out.println(t.elementAt(1));
            System.out.println(t.elementAt(2));
            if (t.elementAt(2).toString().equals(" left the chatroom.")){
                User u = new User(t.elementAt(1).toString());
                this.chatRoom.leave(u);
            }
            else if (t.elementAt(2).toString().equals(" joined the chatroom.")){
                User u = new User(t.elementAt(1).toString());
                this.chatRoom.join(u);
            }
        }

        else if (msgType.equals(new_msg)){
            OtpErlangString username = (OtpErlangString) t.elementAt(1);
            OtpErlangString message = (OtpErlangString) t.elementAt(2);
            //need to include datetime later
            User sender = new User(username.toString());
            Date date = new Date();
            Message m = new Message(sender, message.toString(), date);
            this.chatRoom.sendMessage(m);
        }
    }

    public void sendListenAddress(){
        OtpErlangString username = new OtpErlangString(this.user.getUsername());
        OtpErlangAtom msgType = new OtpErlangAtom("clientListen");
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.mbox.self(), msgType, username});
        this.mbox.send(this.servername, this.servermbox, outMsg);
    }

    public void send(String to, boolean type) throws OtpErlangExit, OtpErlangDecodeException {
        OtpErlangAtom destType;
        if (type){
            destType = new OtpErlangAtom("room");
        }
        else{
            destType = new OtpErlangAtom("user");
        }
        OtpErlangString username = new OtpErlangString(this.user.getUsername());
        OtpErlangString destination = new OtpErlangString(to);
        OtpErlangAtom msgType = new OtpErlangAtom("send");
        Scanner sc= new Scanner(System.in);
        System.out.println("Enter your message to send: ");
        String message= sc.nextLine();
        OtpErlangString msg = new OtpErlangString(message);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.mbox.self(), msgType, msg, username, destType, destination});
        this.mbox.send(this.servername, this.servermbox, outMsg);
        User sender = new User(username.toString());
        Date date = new Date();
        Message m = new Message(sender,msg.toString(),date);
        this.chatRoom.sendMessage(m);
        System.out.println("Message sent!");
    }
}
