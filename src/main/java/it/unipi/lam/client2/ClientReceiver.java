package it.unipi.lam.client2;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangTuple;


public class ClientReceiver extends Thread implements Runnable{
    private final Client c;
    boolean running = true;
//    boolean waitingAck = false;
    public ClientReceiver(Client c) {
        this.c = c;
    }
//
//    public void setWaitingAck(boolean waitingAck){
//        this.waitingAck = waitingAck;
//    }

    @Override
    public void run() {
        try {
            c.sendListenAddress();
            OtpErlangAtom down = new OtpErlangAtom("down");
            OtpErlangAtom interrupt = new OtpErlangAtom("interrupt");
            while(running){
                OtpErlangTuple reply = c.receive();
                if (reply != null){
                    if (reply.elementAt(0).equals(interrupt)) running = false;
                    else if (reply.elementAt(0).equals(down)){
                        running = false;
                        System.out.println("server is down");
                    }
                }
            }
        } catch (OtpErlangExit | OtpErlangDecodeException otpErlangExit) {
            otpErlangExit.printStackTrace();
        }
    }
}
