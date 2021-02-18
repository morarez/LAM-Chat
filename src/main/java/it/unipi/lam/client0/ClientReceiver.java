package it.unipi.lam.client0;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangTuple;


public class ClientReceiver extends Thread implements Runnable{
    private final Client c;
    boolean running = true;
    public ClientReceiver(Client c) {
        this.c = c;
    }

    @Override
    public void run() {
        try {
            c.sendListenAddress();
            while(running){
                OtpErlangTuple reply = c.receive();
                if (reply != null){
                    running = false;
                }
            }
        } catch (OtpErlangExit | OtpErlangDecodeException otpErlangExit) {
            otpErlangExit.printStackTrace();
        }
    }
}
