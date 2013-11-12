package nl.willem.http.jntlm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class Tunneler {

    private Socket proxyConnection;
    private Socket connectionToServer;

    public Tunneler(Socket proxyConnection, Socket connectionToServer) {
        this.proxyConnection = proxyConnection;
        this.connectionToServer = connectionToServer;
    }

    void start() {
        Thread readFromServer = startReadFromServerThread();
        pipe(proxyConnection, connectionToServer);
        try {
            readFromServer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void pipe(Socket from, Socket to) {
        try {
            InputStream input = from.getInputStream();
            OutputStream output = to.getOutputStream();
            int length;
            byte[] buffer = new byte[1024];
            while (!Thread.interrupted() && !to.isClosed() && (length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Thread startReadFromServerThread() {
        Thread readFromServer = new Thread() {
            @Override
            public void run() {
                pipe(connectionToServer, proxyConnection);
            }
        };
        readFromServer.setDaemon(true);
        readFromServer.start();
        return readFromServer;
    }

}
