package nl.willem.http.jntlm;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.protocol.HttpContext;

class UpgradedConnectionAwareReusingStrategy implements ConnectionReuseStrategy {

    @Override
    public boolean keepAlive(HttpResponse response, HttpContext context) {
        if (((UpgradableHttpContext) context).isConnectionUpgraded()) {
            return true;
        } else {
            return DefaultConnectionReuseStrategy.INSTANCE.keepAlive(response, context);
        }
    }

}
