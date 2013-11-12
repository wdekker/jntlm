package nl.willem.http.jntlm;

import java.net.Socket;

import org.apache.http.protocol.BasicHttpContext;

class UpgradableHttpContext extends BasicHttpContext {

    private static final String JNTLM_PROXY_TUNNEL = "JNTLM.PROXY-TUNNEL";

    void setUpgradedConnection(Socket proxyTunnel) {
        setAttribute(JNTLM_PROXY_TUNNEL, proxyTunnel);
    }

    boolean isConnectionUpgraded() {
        return getAttribute(JNTLM_PROXY_TUNNEL) != null;
    }

    Socket getUpgradedConnection() {
        return (Socket) getAttribute(JNTLM_PROXY_TUNNEL);
    }

}
