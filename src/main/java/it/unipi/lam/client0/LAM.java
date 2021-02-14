package it.unipi.lam.client0;

import com.ericsson.otp.erlang.*;
import it.unipi.lam.Room;
import it.unipi.lam.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LAM {
    private Client client;
    private String servername = "lamchat";
    private String servermbox = "server@ahmed-HP-15-NoteBook-PC";

    public LAM(){

    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public void setServermbox(String servermbox) {
        this.servermbox = servermbox;
    }

    public String getServername() {
        return servername;
    }

    public String getServermbox() {
        return servermbox;
    }

    public void loadData() throws OtpErlangExit, OtpErlangDecodeException {
        OtpErlangAtom msgType = new OtpErlangAtom("newuser");
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.client.getMbox().self(), msgType});
        this.client.getMbox().send(this.client.getServername(), this.client.getServerMbox(), outMsg);

        OtpErlangObject msg = this.client.getMbox().receive();
        OtpErlangTuple t = (OtpErlangTuple) msg;

        System.out.println("msg received: " + msg.toString());

        msgType = (OtpErlangAtom) t.elementAt(0);

        OtpErlangAtom rooms = new OtpErlangAtom("rooms");

        if(msgType.equals(rooms)){
            OtpErlangList availableRooms = (OtpErlangList) t.elementAt(1);
            List<String> chatRooms = new ArrayList<>();
            for (OtpErlangObject r: availableRooms){
                chatRooms.add(r.toString());
            }
            System.out.println(chatRooms);
        }
        else{
            System.out.println("Server is behaving abnormally");
        }
    }

    public Room handleLocalData(Room chatRoom, String username, OtpErlangList currentUsers){
        for (OtpErlangObject u: currentUsers){
            String name = u.toString();
            User user = new User(name);
            chatRoom.join(user);
        }
        User user = new User(username);
        System.out.println(user.getUsername());
        chatRoom.join(user);
        return chatRoom;
    }

    public Room joinRoom(Room chatRoom) throws OtpErlangExit, OtpErlangDecodeException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter a username");
        String username = sc.nextLine();


        OtpErlangAtom msgType = new OtpErlangAtom("connect");
        OtpErlangString roomname = new OtpErlangString(chatRoom.getRoomName());
        OtpErlangString potentialUsername = new OtpErlangString(username);

        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.client.getMbox().self(), msgType, roomname});
        this.client.getMbox().send(this.client.getServername(), this.client.getServerMbox(), outMsg);


        OtpErlangObject msg = this.client.getMbox().receive();
        OtpErlangTuple t = (OtpErlangTuple) msg;
        System.out.println("msg received: " + msg.toString());

        msgType = (OtpErlangAtom) t.elementAt(0);
        OtpErlangAtom users = new OtpErlangAtom("users");
        if(msgType.equals(users)){
            OtpErlangList currentUsers = (OtpErlangList) t.elementAt(1);

            boolean unique = true;
            do {
                for (OtpErlangObject u : currentUsers) {
                    if(username.equals(u.toString())){
                        unique = false;
                        break;
                    }
                    else{
                        unique = true;
                    }
                }
                if (!unique){
                    System.out.println("Please enter a new username");
                    username = sc.nextLine();
                }
            } while(!unique);

            msgType = new OtpErlangAtom("done");
            potentialUsername = new OtpErlangString(username);
            outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.client.getMbox().self(), msgType, potentialUsername});
            this.client.getMbox().send(this.client.getServername(), this.client.getServerMbox(), outMsg);

            return handleLocalData(chatRoom, username, currentUsers);
        }

        else{
            System.out.println("Server is behaving abnormally");
            return chatRoom;
        }
    }

    public static void main(String[] args) throws IOException, OtpErlangExit, OtpErlangDecodeException, InterruptedException {
        ExecutorService myExecSrv = Executors.newFixedThreadPool(2);
        System.out.println("Welcome to LAM!");
        System.out.println("Please select the chatroom you would like to join or create a new one");
        LAM lam = new LAM();
        Client anonClient = new Client("anon4@localhost", "anon4", "", lam.getServername(), lam.getServermbox());
        lam.setClient(anonClient);
        lam.loadData();

        Room chatRoom = new Room();
        User user;



        Scanner sc = new Scanner(System.in);
        chatRoom.setRoomName(sc.nextLine());

        chatRoom = lam.joinRoom(chatRoom);
        List<User> users = chatRoom.getUserList();
        user = users.get(users.size()-1);

        Client actualClient = new Client(user.getUsername()+"@localhost", user.getUsername()+"box", "", lam.getServername(), lam.getServermbox());
        actualClient.setChatRoom(chatRoom);
        actualClient.setUser(user);
        lam.setClient(actualClient);

        Thread clientReceiver = new Thread(new ClientReceiver(lam.getClient()));
        Thread clientSender = new Thread(new ClientSender(lam.getClient()));

        clientReceiver.start();

        while (true){
            System.out.println("To send a message write send then Enter then the message" +
                    ", to leave write x");
            String ans = sc.nextLine();
            if (ans.equals("send")){
                clientSender.start();
                clientSender.join();
            }
            else if (ans.equals("x")){
                break;
            }
        }
        clientReceiver.stop();
    }
}
