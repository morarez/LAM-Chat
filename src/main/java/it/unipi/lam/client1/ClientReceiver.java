package it.unipi.lam.client1;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;

import java.io.IOException;

public class ClientReceiver implements Runnable{
    private Client c;

    public ClientReceiver(Client c) throws IOException {
        this.c = c;
    }


    @Override
    public void run() {
        try {
            c.sendListenAddress();
            while(true) {
                c.receive();
            }
        } catch (OtpErlangExit otpErlangExit) {
            otpErlangExit.printStackTrace();
        } catch (OtpErlangDecodeException e) {
            e.printStackTrace();
        }
    }
}
