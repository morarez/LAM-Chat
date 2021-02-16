package it.unipi.lam.client2;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;


public class ClientReceiver extends Thread implements Runnable{
    private final Client c;

    public ClientReceiver(Client c) {
        this.c = c;
    }

    @Override
    public void run() {
        try {
            c.sendListenAddress();
            do {
                c.receive();
            } while (!this.isInterrupted());
        } catch (OtpErlangExit | OtpErlangDecodeException otpErlangExit) {
            otpErlangExit.printStackTrace();
        }
    }
}
