package nl.willem.http.jntlm;

import org.apache.http.HttpHost;

public class JNTLM {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Please specify a proxy host and port");
            System.exit(1);
        }
        
        String proxyHost = args[0];
        int proxyPort = Integer.parseInt(args[1]);
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        
        int port = 4242;
        if (args.length >= 3) {
            port = Integer.parseInt(args[2]);
        }
        
        Thread t = new ProxyProxy(port, proxy);
        t.start();
    }

}
