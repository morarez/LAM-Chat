package it.unipi.lam.client2;

import com.ericsson.otp.erlang.*;
import it.unipi.lam.Room;
import it.unipi.lam.User;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LAM {
    private Client client;
    private static int x = 3000;
    private String servername = "lamchat";
    private String servermbox = "server@ahmed-HP-15-NoteBook-PC";

    public LAM(){

    }

    public static void incX() {
        x += 1 ;
    }

    public static int getX() {
        return x;
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

        msgType = (OtpErlangAtom) t.elementAt(0);

        OtpErlangAtom rooms = new OtpErlangAtom("rooms");

        if(msgType.equals(rooms)){
            OtpErlangList availableRooms = (OtpErlangList) t.elementAt(1);
            System.out.print("Current Rooms: ");
            System.out.println(availableRooms);
        }
        else{
            System.out.println("Server is behaving abnormally");
        }
    }

    public Room handleLocalData(Room chatRoom, String username, OtpErlangList currentUsers){
        for (OtpErlangObject u: currentUsers){
            String name = u.toString();
            name = name.replace("\"", "");
            System.out.println(name);
            User user = new User(name);
            chatRoom.join(user);
        }
        User user = new User(username.replace("\"", ""));
        chatRoom.join(user);
        return chatRoom;
    }

    public Room joinRoom(Room chatRoom) throws OtpErlangExit, OtpErlangDecodeException {
        Scanner sc = new Scanner(System.in);
        String username;
        OtpErlangAtom msgType;
        OtpErlangString roomname = new OtpErlangString(chatRoom.getRoomName());
        OtpErlangAtom no = new OtpErlangAtom("taken");
        OtpErlangAtom users = new OtpErlangAtom("users");
        OtpErlangObject content;
        do {
            msgType  = new OtpErlangAtom("connect");
            System.out.println("Please enter a username");
            username = sc.nextLine();
            OtpErlangString potentialUsername = new OtpErlangString(username);
            OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.client.getMbox().self(), msgType, roomname, potentialUsername});

            this.client.getMbox().send(this.client.getServername(), this.client.getServerMbox(), outMsg);

            OtpErlangObject msg = this.client.getMbox().receive();
            OtpErlangTuple t = (OtpErlangTuple) msg;

            msgType = (OtpErlangAtom) t.elementAt(0);
            content = t.elementAt(1);

        }while (msgType.equals(no));

        if(msgType.equals(users)){
            System.out.println("Current Users: " + content);
            OtpErlangList currentUsers = (OtpErlangList) content;
            return handleLocalData(chatRoom, username, currentUsers);
        }

        else{
            System.out.println("Server is behaving abnormally");
            return chatRoom;
        }
    }

    public void leaveRoom() {
        OtpErlangAtom msgType = new OtpErlangAtom("EXIT");
        OtpErlangString roomname = new OtpErlangString(this.getClient().getChatRoom().getRoomName());
        OtpErlangString username = new OtpErlangString(this.getClient().getUser().getUsername());
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType, this.client.getMbox().self(), roomname, username});
        this.client.getMbox().send(this.client.getServername(), this.client.getServerMbox(), outMsg);
    }


    public static void main(String[] args) throws IOException, OtpErlangExit, OtpErlangDecodeException, InterruptedException {

        while (true) {
            ExecutorService myExecSrv = Executors.newFixedThreadPool(2);
            CountDownLatch latch;
            System.out.println("Welcome to LAM!");
            System.out.println("If you want to close the app, press x");

            System.out.println("Please select the chatroom you would like to join or create a new one, otherwise press anything else");

            LAM lam = new LAM();
            Client anonClient = new Client("anon"+LAM.getX()+"@localhost", "anon"+LAM.getX(), "", lam.getServername(), lam.getServermbox());

            lam.setClient(anonClient);
            lam.loadData();
            Scanner sc = new Scanner(System.in);

            String answer = sc.nextLine();

            if (answer.equals("x")){
                break;
            }


            Room chatRoom = new Room();
            User user;
            chatRoom.setRoomName(answer);
            chatRoom = lam.joinRoom(chatRoom);
            List<User> users = chatRoom.getUserList();
            user = users.get(users.size() - 1);

            Client actualClient = new Client(user.getUsername()+LAM.getX()+ "@localhost", user.getUsername()+LAM.getX() + "box", "", lam.getServername(), lam.getServermbox());
            actualClient.setChatRoom(chatRoom);
            actualClient.setUser(user);
            lam.setClient(actualClient);

            ClientReceiver clientReceiver = new ClientReceiver(lam.getClient());

            myExecSrv.execute(clientReceiver);

            while (true) {
                System.out.println("To send a message write send then Enter then the message" +
                        ", to see all users, write show then Enter, to leave the chat room write x");
                String ans = sc.nextLine();
                if (ans.equals("send")) {
                    latch = new CountDownLatch(1);
                    ClientSender clientSender = new ClientSender(lam.getClient(), latch);
                    myExecSrv.execute(clientSender);
                    latch.await();

                } else if (ans.equals("show")) {
                    lam.getClient().getChatRoom().showUsers();
                } else if (ans.equals("x")) {
                    lam.leaveRoom();
                    break;
                }
            }
            clientReceiver.interrupt();
            myExecSrv.shutdownNow();
            LAM.incX();
        }
    }
}
