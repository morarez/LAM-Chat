package it.unipi.lam;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private String roomName;
    private List<User> userList;
    private List<Message> chatMessages;

    public Room(String roomName){
        this.roomName = roomName;
        this.userList = new ArrayList<>();
        this.chatMessages = new ArrayList<>();
    }

    public List<Message> getChatMessages() {
        return chatMessages;
    }

    public List<User> getUserList() {
        return userList;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setChatMessages(List<Message> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void join(User u){
        for (User member: userList){
            if(u.getUsername().equals( member.getUsername())){
                System.out.println("This username is currently being used");
                return;
            }
        }
        userList.add(u);
    }

    public void leave(User u){
        u.changeStatus(false);
        userList.remove(u);
    }

    public void suspend(User u){
        u.changeStatus(false);
    }

    public void wakeup(User u){
        u.changeStatus(true);
    }

    public void sendMessage(Message m){
        chatMessages.add(m);
    }


}
