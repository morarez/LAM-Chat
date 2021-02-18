package it.unipi.lam.client1;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import it.unipi.lam.entities.User;
import org.apache.log4j.Logger;

public class ClientPingPong implements Runnable {
	private final Client c;
	private Logger logger;
	public ClientPingPong(Client c) {
		this.c = c;
		logger = Logger.getLogger(ClientPingPong.class.getName());
}
	@Override
	public void run() {
        try {
        	do
        	{
            OtpErlangAtom msgType = pingpong();
            OtpErlangAtom pong = new OtpErlangAtom("pong");
            if(msgType!=pong) {
            	logger.info("CLIENT 0: PONG not received.");
            	pingpong();
            	if(msgType.equals(pong)) {
            		logger.info("CLIENT 0: PONG received!");
            		try {
						//Thread.sleep(1000*1000);
						TimeUnit.MINUTES.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
            		OtpErlangString room= new OtpErlangString(c.getChatRoom().getRoomName());
            		List<User> users= c.getChatRoom().getUserList();
            		//List<String> us=null;
            		for(User u: users) {
            			 Client a = new Client(u.getUsername()+LAM.getX()+ "@localhost", u.getUsername()+LAM.getX() + "box", "", c.getServername(), c.getServerMbox());
            	          a.sendListenAddress();
            	          OtpErlangString user= new OtpErlangString(u.getUsername());
            	          OtpErlangAtom r= new OtpErlangAtom("recovery");
                  		OtpErlangTuple recovery= new OtpErlangTuple(new OtpErlangObject[] {a.getMbox().self(),r,room,user});
                  		a.getMbox().send(a.getServername(), a.getServerMbox(), recovery);
                  		break;
            		}

            	}
            }
        	}while(true);
        } 
        
        catch (IOException e) {
			//e.printStackTrace();
		} catch (OtpErlangExit e) {
			e.printStackTrace();
		} catch (OtpErlangDecodeException e) {
			e.printStackTrace();
		}
	}
	public OtpErlangAtom pingpong() {
		try {
		OtpErlangAtom ping= new OtpErlangAtom("ping");
		OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{ping, c.getMbox().self()});
        c.getMbox().send(c.getServername(), c.getServerMbox(), outMsg);
        logger.info("CLIENT 0: PING sent!");
        OtpErlangObject msg = c.getMbox().receive();
        logger.info("CLIENT 0: Message received!");
        OtpErlangTuple t = (OtpErlangTuple) msg;
        OtpErlangAtom msgType = (OtpErlangAtom) t.elementAt(0);
        return msgType;
		
	}
		catch (OtpErlangExit | OtpErlangDecodeException otpErlangExit) {
	            otpErlangExit.printStackTrace();
			return null;
	        }
		}

}
