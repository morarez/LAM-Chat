package it.unipi.lam.client0;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;

import java.io.IOException;

public class ClientReceiver implements Runnable{
    private Client c;
    private boolean exit = false;
    public ClientReceiver(Client c) throws IOException {
        this.c = c;
    }

    public void stop() {
        this.exit = true;
    }

    @Override
    public void run() {
        try {
            while(!exit) {
                c.receive();
            }
        } catch (OtpErlangExit otpErlangExit) {
            otpErlangExit.printStackTrace();
        } catch (OtpErlangDecodeException e) {
            e.printStackTrace();
        }
    }
}
