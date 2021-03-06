package com.puppetlabs.http.client;

import org.apache.http.nio.client.HttpAsyncClient;
//import org.httpkit.client.HttpClient;
//
//import org.httpkit.client.IFilter;
//import org.httpkit.client.MultipartEntity;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class RequestOptions {
    public static final String[] DEFAULT_SSL_PROTOCOLS =
            new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"};

    private HttpAsyncClient client = null;

    private URI uri;
    private HttpMethod method = null;
    private Map<String, String> headers;
    private SSLContext sslContext;
    private String sslCert;
    private String sslKey;
    private String sslCaCert;
    private String[] sslProtocols;
    private String[] sslCipherSuites;
    private boolean insecure = false;
    private Object body;
    private boolean decompressBody = true;
    private ResponseBodyType as = ResponseBodyType.STREAM;
    private boolean forceRedirects = false;
    private boolean followRedirects = true;

    public RequestOptions (String url) throws URISyntaxException { this.uri = new URI(url); }
    public RequestOptions(URI uri) {
        this.uri = uri;
    }

    public HttpAsyncClient getClient() {
        return client;
    }
    public RequestOptions setClient(HttpAsyncClient client) {
        this.client = client;
        return this;
    }

    public URI getUri() {
        return uri;
    }
    public RequestOptions setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }
    public RequestOptions setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    public RequestOptions setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }
    public RequestOptions setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public String getSslCert() {
        return sslCert;
    }
    public RequestOptions setSslCert(String sslCert) {
        this.sslCert = sslCert;
        return this;
    }

    public String getSslKey() {
        return sslKey;
    }
    public RequestOptions setSslKey(String sslKey) {
        this.sslKey = sslKey;
        return this;
    }

    public String getSslCaCert() {
        return sslCaCert;
    }
    public RequestOptions setSslCaCert(String sslCaCert) {
        this.sslCaCert = sslCaCert;
        return this;
    }

    public String[] getSslProtocols() {
        return sslProtocols;
    }
    public RequestOptions setSslProtocols(String[] sslProtocols) {
        this.sslProtocols = sslProtocols;
        return this;
    }

    public String[] getSslCipherSuites() {
        return sslCipherSuites;
    }
    public RequestOptions setSslCipherSuites(String[] sslCipherSuites) {
        this.sslCipherSuites = sslCipherSuites;
        return this;
    }

    public boolean getInsecure() {
        return insecure;
    }
    public RequestOptions setInsecure(boolean insecure) {
        this.insecure = insecure;
        return this;
    }

    public Object getBody() {
        return body;
    }
    public RequestOptions setBody(Object body) {
        this.body = body;
        return this;
    }

    public boolean getDecompressBody() { return decompressBody; }
    public RequestOptions setDecompressBody(boolean decompressBody) {
        this.decompressBody = decompressBody;
        return this;
    }

    public ResponseBodyType getAs() {
        return as;
    }
    public RequestOptions setAs(ResponseBodyType as) {
        this.as = as;
        return this;
    }

    public boolean getForceRedirects() { return forceRedirects; }
    public RequestOptions setForceRedirects(boolean forceRedirects) {
        this.forceRedirects = forceRedirects;
        return this;
    }

    public boolean getFollowRedirects() { return followRedirects; }
    public RequestOptions setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }
}
