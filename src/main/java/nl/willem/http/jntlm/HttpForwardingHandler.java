package nl.willem.http.jntlm;

import static org.apache.http.auth.AuthScope.ANY;
import static org.apache.http.client.config.AuthSchemes.NTLM;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyClient;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

class HttpForwardingHandler implements HttpRequestHandler {

    private final CloseableHttpClient httpClient;
    private final ProxyClient proxyClient;
    private final HttpHost proxy;

    HttpForwardingHandler(HttpHost proxy) {
        this.proxy = proxy;
        this.httpClient = buildHttpClient(proxy);
        this.proxyClient = buildProxyClient();
    }

    private CloseableHttpClient buildHttpClient(HttpHost proxy) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(ANY, noCredentials());
        Registry<AuthSchemeProvider> registry = RegistryBuilder.<AuthSchemeProvider> create().register(NTLM, new NTLMSchemeProvider()).build();
        return HttpClients.custom().setRoutePlanner(new DefaultProxyRoutePlanner(proxy)).setDefaultAuthSchemeRegistry(registry)
                .setDefaultCredentialsProvider(credentialsProvider).disableRedirectHandling().build();
    }

    private ProxyClient buildProxyClient() {
        ProxyClient proxyClient = new ProxyClient();
        proxyClient.getAuthSchemeRegistry().register(NTLM, new NTLMSchemeProvider());
        return proxyClient;
    }

    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        if (isTunnelRequest(request)) {
            upgradeToTunneling(request, response, (UpgradableHttpContext) context);
        } else {
            forward(request, response);
        }
    }

    private boolean isTunnelRequest(HttpRequest request) {
        return "CONNECT".equalsIgnoreCase(request.getRequestLine().getMethod());
    }

    private void upgradeToTunneling(HttpRequest request, HttpResponse response, UpgradableHttpContext context) throws IOException, HttpException {
        response.setStatusLine(HttpVersion.HTTP_1_1, 200, "Connection established");
        Socket proxyTunnel = proxyClient.tunnel(proxy, createHttpHost(request), noCredentials());
        context.setUpgradedConnection(proxyTunnel);
    }

    private void forward(final HttpRequest request, final HttpResponse response) throws IOException, ClientProtocolException {
        HttpResponse clientResponse = httpClient.execute(createHttpHost(request), request);
        response.setStatusLine(clientResponse.getStatusLine());
        response.setHeaders(clientResponse.getAllHeaders());
        response.setEntity(clientResponse.getEntity());
    }

    private NTCredentials noCredentials() {
        return new NTCredentials("", "", "", "");
    }

    private HttpHost createHttpHost(final HttpRequest request) {
        String uri = request.getRequestLine().getUri();
        if (uri.contains("://")) {
            uri = uri.substring(uri.lastIndexOf("://") + 3);
        }
        String[] parts = uri.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
        return new HttpHost(host, port);
    }

}