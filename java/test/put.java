package test;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.ServiceClient;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.axis.utils.Options;

import java.io.File;
import java.io.FileInputStream;

/**
 * A convenient little test program which will send a message as is to
 * the server.  Useful for debugging interoperability problems or 
 * handling of ill-formed messages that are hard to reproduce programmatically.
 *
 * Accepts the standard options, followed by a list of files containing
 * the contents to be sent.
 */
class put {
    static void main(String[] args) throws Exception {
        Options opts = new Options(args);
        String action = opts.isValueSet('a');

        ServiceClient sc = new ServiceClient(opts.getURL());
        if (action != null) sc.set(HTTPTransport.ACTION, action);
  
        args = opts.getRemainingArgs();
        for (int i=0; i<args.length; i++) {
            FileInputStream stream = new FileInputStream(new File(args[i]));
            sc.setRequestMessage(new Message(stream));
    
            sc.invoke();
        
            MessageContext mc = sc.getMessageContext();
            System.out.println(mc.getResponseMessage().getAsString());
        }
    }
}
