package nl.willem.http.jntlm;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

class ProxyProxy extends Thread {

    private final ServerSocket serversocket;
    private final HttpService httpService;

    ProxyProxy(final int port, final HttpHost proxy) throws IOException {
        this.serversocket = new ServerSocket(port);

        HttpProcessor httpproc = HttpProcessorBuilder.create().add(new ResponseConnControl()).build();
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        reqistry.register("*", new HttpForwardingHandler(proxy));
        this.httpService = new HttpService(httpproc, new UpgradedConnectionAwareReusingStrategy(), null, reqistry);

    }

    @Override
    public void run() {
        System.out.println("Listening on port " + this.serversocket.getLocalPort());
        while (!Thread.interrupted()) {
            try {
                Socket socket = this.serversocket.accept();
                System.out.println("Incoming connection from " + socket.getInetAddress());
                Thread t = new WorkerThread(this.httpService, socket);
                t.setDaemon(true);
                t.start();
            } catch (InterruptedIOException ex) {
                break;
            } catch (IOException e) {
                System.err.println("I/O error initialising connection thread: " + e.getMessage());
                break;
            }
        }
    }

    private static class WorkerThread extends Thread {

        private static AtomicInteger connectionCount = new AtomicInteger();

        private final HttpService httpservice;
        private final Socket socket;

        public WorkerThread(final HttpService httpservice, Socket socket) {
            this.httpservice = httpservice;
            this.socket = socket;
        }

        @Override
        public void run() {
            int connections = connectionCount.incrementAndGet();
            System.out.println("New connection thread (" + connections + ")");
            UpgradableHttpContext context = new UpgradableHttpContext();
            HttpServerConnection conn = null;
            try {
                conn = DefaultBHttpServerConnectionFactory.INSTANCE.createConnection(socket);
                while (!Thread.interrupted() && conn.isOpen() && !context.isConnectionUpgraded()) {
                    httpservice.handleRequest(conn, context);
                }
                if (context.isConnectionUpgraded()) {
                    new Tunneler(context.getUpgradedConnection(), socket).start();
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
                ex.printStackTrace();
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                System.out.println("Connection thread: " + connections + " closed");
                connectionCount.decrementAndGet();
                try {
                    if (conn != null) {
                        conn.shutdown();
                    }
                    if (context.isConnectionUpgraded()) {
                        context.getUpgradedConnection().close();
                    }
                } catch (IOException ignore) {
                }
            }
        }
    }

}