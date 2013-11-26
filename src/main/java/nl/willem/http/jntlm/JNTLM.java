package nl.willem.http.jntlm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;

public class JNTLM {

    public static void main(String[] args) throws Exception {
        HttpHost proxy = extractProxy(args);
        int listenPort = extractListenHost(args);
        InetAddress listenAddress = extractListenAddress(args);
        new ProxyProxy(listenPort, listenAddress, proxy).start();
    }

    private static HttpHost extractProxy(String[] args) {
        if (args.length < 2) {
            System.err.println("Please specify a proxy host and port");
            System.exit(1);
        }

        String proxyHost = args[0];
        int proxyPort = Integer.parseInt(args[1]);
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        return proxy;
    }

    private static int extractListenHost(String[] args) {
        int port = 4242;
        if (args.length >= 3) {
            port = Integer.parseInt(args[2]);
        }
        return port;
    }

    private static InetAddress extractListenAddress(String[] args) {
        InetAddress listenAddress = null;
        if (args.length >= 4 && "open".equals(args[3])) {
            listenAddress = null;
        } else {
            try {
                listenAddress = InetAddress.getByName("::1");
            } catch (UnknownHostException e) {
                throw new RuntimeException("Cannot get loopback address", e);
            }
        }
        return listenAddress;
    }

}
