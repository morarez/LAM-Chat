package it.unipi.lam;

import java.util.Date;

public class Message {

    private User author;
    private String content;
    private Date date;

    public Message(){
        author = null;
        content = null;
        date = null;
    }

    public Message(User author, String content, Date date){
        this.author = author;
        this.content = content;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    public User getAuthor() {
        return author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
