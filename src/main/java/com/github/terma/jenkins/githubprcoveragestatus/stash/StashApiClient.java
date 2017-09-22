package com.github.terma.jenkins.githubprcoveragestatus.stash;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Nathan McCarthy
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class StashApiClient {

    private static final int HTTP_REQUEST_TIMEOUT_SECONDS = 60;
    private static final int HTTP_CONNECTION_TIMEOUT_SECONDS = 15;
    private static final int HTTP_SOCKET_TIMEOUT_SECONDS = 15;

    private static final Logger logger = Logger.getLogger(StashApiClient.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private String apiBaseUrl;

    private String project;
    private String repositoryName;
    private Credentials credentials;
    private boolean ignoreSsl;

    public StashApiClient(String stashHost, String username, String password, String project, String repositoryName, boolean ignoreSsl) {
        this.credentials = new UsernamePasswordCredentials(username, password);
        this.project = project;
        this.repositoryName = repositoryName;
        this.apiBaseUrl = stashHost.replaceAll("/$", "") + "/rest/api/1.0/projects/";
        this.ignoreSsl = ignoreSsl;
    }

    public List<StashPullRequestResponseValue> getPullRequests() {
        List<StashPullRequestResponseValue> pullRequestResponseValues = new ArrayList<StashPullRequestResponseValue>();
        try {
            boolean isLastPage = false;
            int start = 0;
            while (!isLastPage) {
                String response = getRequest(pullRequestsPath(start));
                StashPullRequestResponse parsedResponse = parsePullRequestJson(response);
                isLastPage = parsedResponse.getIsLastPage();
                if (!isLastPage) {
                    start = parsedResponse.getNextPageStart();
                }
                pullRequestResponseValues.addAll(parsedResponse.getPrValues());
            }
            return pullRequestResponseValues;
        } catch (IOException e) {
            logger.log(Level.WARNING, "invalid pull request response.", e);
        }
        return Collections.EMPTY_LIST;
    }

    public List<StashPullRequestComment> getPullRequestComments(String projectCode, String commentRepositoryName,
        String pullRequestId) {

        try {
            boolean isLastPage = false;
            int start = 0;
            List<StashPullRequestActivityResponse> commentResponses = new ArrayList<StashPullRequestActivityResponse>();
            while (!isLastPage) {
                String response = getRequest(
                    apiBaseUrl + projectCode + "/repos/" + commentRepositoryName + "/pull-requests/" +
                        pullRequestId + "/activities?start=" + start);
                StashPullRequestActivityResponse resp = parseCommentJson(response);
                isLastPage = resp.getIsLastPage();
                if (!isLastPage) {
                    start = resp.getNextPageStart();
                }
                commentResponses.add(resp);
            }
            return extractComments(commentResponses);
        } catch (Exception e) {
            logger.log(Level.WARNING, "invalid pull request response.", e);
        }
        return Collections.EMPTY_LIST;
    }

    public void deletePullRequestComment(String pullRequestId, String commentId) {
        String path = pullRequestPath(pullRequestId) + "/comments/" + commentId + "?version=0";
        deleteRequest(path);
    }

    public StashPullRequestComment postPullRequestComment(String pullRequestId, String comment) {
        String path = pullRequestPath(pullRequestId) + "/comments";
        try {
            String response = postRequest(path, comment);
            return parseSingleCommentJson(response);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to post Stash PR comment " + path + " " + e);
        }
        return null;
    }

    public StashPullRequestMergableResponse getPullRequestMergeStatus(String pullRequestId) {
        String path = pullRequestPath(pullRequestId) + "/merge";
        try {
            String response = getRequest(path);
            return parsePullRequestMergeStatus(response);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to get Stash PR Merge Status " + path + " " + e);
        }
        return null;
    }

    public boolean mergePullRequest(String pullRequestId, String version) {
        String path = pullRequestPath(pullRequestId) + "/merge?version=" + version;
        try {
            String response = postRequest(path, null);
            return !response.equals(Integer.toString(HttpStatus.SC_CONFLICT));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to merge Stash PR " + path + " " + e);
        }
        return false;
    }

    private HttpContext gethttpContext(Credentials credentials) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        URI stashUri = URI.create(this.apiBaseUrl);
        authCache.put(new HttpHost(stashUri.getHost(), stashUri.getPort(), stashUri.getScheme()), basicAuth);

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        RequestConfig config = RequestConfig.copy(context.getRequestConfig()).
            setConnectTimeout(StashApiClient.HTTP_CONNECTION_TIMEOUT_SECONDS * 1000).
            setSocketTimeout(StashApiClient.HTTP_SOCKET_TIMEOUT_SECONDS * 1000).build();
        context.setRequestConfig(config);

        return context;
    }

    private HttpClient getHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create().useSystemProperties();
        if (this.ignoreSsl) {
            try {
                SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

                sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE);
                builder.setSSLSocketFactory(sslsf);
            } catch (NoSuchAlgorithmException e) {
                logger.log(Level.SEVERE, "Failing to setup the SSLConnectionFactory: " + e.toString());
                throw new RuntimeException(e);
            } catch (KeyStoreException e) {
                logger.log(Level.SEVERE, "Failing to setup the SSLConnectionFactory: " + e.toString());
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                logger.log(Level.SEVERE, "Failing to setup the SSLConnectionFactory: " + e.toString());
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }

    private String getRequest(String path) {
        logger.log(Level.FINEST, "PR-GET-REQUEST:" + path);
        HttpClient client = getHttpClient();
        HttpContext context = gethttpContext(credentials);

        HttpGet httpget = new HttpGet(path);
        //http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html; section 14.10.
        //tells the server that we want it to close the connection when it has sent the response.
        //address large amount of close_wait sockets client and fin sockets server side
        httpget.addHeader("Connection", "close");

        String response = null;
        FutureTask<String> httpTask = null;
        Thread thread;
        try {
            //Run the http request in a future task so we have the opportunity
            //to cancel it if it gets hung up; which is possible if stuck at
            //socket native layer.  see issue JENKINS-30558
            httpTask = new FutureTask<String>(new Callable<String>() {

                private HttpClient client;
                private HttpContext context;
                private HttpGet httpget;

                @Override
                public String call() throws Exception {
                    HttpResponse httpResponse = client.execute(httpget, context);
                    int responseCode = httpResponse.getStatusLine().getStatusCode();
                    String response = httpResponse.getStatusLine().getReasonPhrase();
                    if (!validResponseCode(responseCode)) {
                        logger.log(Level.SEVERE, "Failing to get response from Stash PR GET" + httpget.getURI().getPath());
                        throw new RuntimeException("Didn't get a 200 response from Stash PR GET! Response; '" +
                            responseCode + "' with message; " + response);
                    }
                    InputStream responseBodyAsStream = httpResponse.getEntity().getContent();
                    StringWriter stringWriter = new StringWriter();
                    IOUtils.copy(responseBodyAsStream, stringWriter, "UTF-8");
                    response = stringWriter.toString();

                    return response;

                }

                public Callable<String> init(HttpClient client, HttpGet httpget, HttpContext context) {
                    this.client = client;
                    this.context = context;
                    this.httpget = httpget;
                    return this;
                }

            }.init(client, httpget, context));
            thread = new Thread(httpTask);
            thread.start();
            response = httpTask.get((long) StashApiClient.HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            e.printStackTrace();
            httpget.abort();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            httpget.releaseConnection();
        }
        logger.log(Level.FINEST, "PR-GET-RESPONSE:" + response);
        return response;
    }

    public void deleteRequest(String path) {
        HttpClient client = getHttpClient();
        HttpContext context = gethttpContext(this.credentials);

        HttpDelete httpDelete = new HttpDelete(path);
        //http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html; section 14.10.
        //tells the server that we want it to close the connection when it has sent the response.
        //address large amount of close_wait sockets client and fin sockets server side
        httpDelete.setHeader("Connection", "close");

        int res = -1;
        FutureTask<Integer> httpTask = null;
        Thread thread;

        try {
            //Run the http request in a future task so we have the opportunity
            //to cancel it if it gets hung up; which is possible if stuck at
            //socket native layer.  see issue JENKINS-30558
            httpTask = new FutureTask<Integer>(new Callable<Integer>() {

                private HttpClient client;
                private HttpContext context;
                private HttpDelete httpDelete;

                @Override
                public Integer call() throws Exception {
                    int res = -1;
                    res = client.execute(httpDelete, context).getStatusLine().getStatusCode();
                    return res;

                }

                public Callable<Integer> init(HttpClient client, HttpDelete httpDelete, HttpContext context) {
                    this.client = client;
                    this.httpDelete = httpDelete;
                    this.context = context;
                    return this;
                }

            }.init(client, httpDelete, context));
            thread = new Thread(httpTask);
            thread.start();
            res = httpTask.get((long) StashApiClient.HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            e.printStackTrace();
            httpDelete.abort();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            httpDelete.releaseConnection();
        }

        logger.log(Level.FINE, "Delete comment {" + path + "} returned result code; " + res);
    }

    private String postRequest(String path, String comment) throws UnsupportedEncodingException {
        logger.log(Level.FINEST, "PR-POST-REQUEST:" + path + " with: " + comment);
        HttpClient client = getHttpClient();
        HttpContext context = gethttpContext(credentials);

        HttpPost httppost = new HttpPost(path);
        //http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html; section 14.10.
        //tells the server that we want it to close the connection when it has sent the response.
        //address large amount of close_wait sockets client and fin sockets server side
        httppost.setHeader("Connection", "close");
        httppost.setHeader("X-Atlassian-Token", "no-check"); //xsrf

        if (comment != null) {
            ObjectNode node = mapper.getNodeFactory().objectNode();
            node.put("text", comment);
            StringEntity requestEntity = null;
            try {
                requestEntity = new StringEntity(
                    mapper.writeValueAsString(node),
                    ContentType.APPLICATION_JSON);
            } catch (IOException e) {
                e.printStackTrace();
            }
            httppost.setEntity(requestEntity);
        }

        String response = "";
        FutureTask<String> httpTask = null;
        Thread thread;

        try {
            //Run the http request in a future task so we have the opportunity
            //to cancel it if it gets hung up; which is possible if stuck at
            //socket native layer.  see issue JENKINS-30558
            httpTask = new FutureTask<String>(new Callable<String>() {

                private HttpClient client;
                private HttpContext context;
                private HttpPost httppost;

                @Override
                public String call() throws Exception {

                    HttpResponse httpResponse = client.execute(httppost, context);
                    int responseCode = httpResponse.getStatusLine().getStatusCode();
                    String response = httpResponse.getStatusLine().getReasonPhrase();
                    if (!validResponseCode(responseCode)) {
                        logger.log(Level.SEVERE, "Failing to get response from Stash PR POST" + httppost.getURI().getPath());
                        throw new RuntimeException("Didn't get a 200 response from Stash PR POST! Response; '" +
                            responseCode + "' with message; " + response);
                    }
                    InputStream responseBodyAsStream = httpResponse.getEntity().getContent();
                    StringWriter stringWriter = new StringWriter();
                    IOUtils.copy(responseBodyAsStream, stringWriter, "UTF-8");
                    response = stringWriter.toString();
                    logger.log(Level.FINEST, "API Request Response: " + response);

                    return response;

                }

                public Callable<String> init(HttpClient client, HttpPost httppost, HttpContext context) {
                    this.client = client;
                    this.context = context;
                    this.httppost = httppost;
                    return this;
                }

            }.init(client, httppost, context));
            thread = new Thread(httpTask);
            thread.start();
            response = httpTask.get((long) StashApiClient.HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            e.printStackTrace();
            httppost.abort();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            httppost.releaseConnection();
        }

        logger.log(Level.FINEST, "PR-POST-RESPONSE:" + response);

        return response;
    }

    private boolean validResponseCode(int responseCode) {
        return responseCode == HttpStatus.SC_OK ||
            responseCode == HttpStatus.SC_ACCEPTED ||
            responseCode == HttpStatus.SC_CREATED ||
            responseCode == HttpStatus.SC_NO_CONTENT ||
            responseCode == HttpStatus.SC_RESET_CONTENT;
    }

    private StashPullRequestResponse parsePullRequestJson(String response) throws IOException {
        StashPullRequestResponse parsedResponse;
        parsedResponse = mapper.readValue(response, StashPullRequestResponse.class);
        return parsedResponse;
    }

    private StashPullRequestActivityResponse parseCommentJson(String response) throws IOException {
        StashPullRequestActivityResponse parsedResponse;
        parsedResponse = mapper.readValue(response, StashPullRequestActivityResponse.class);
        return parsedResponse;
    }

    private List<StashPullRequestComment> extractComments(List<StashPullRequestActivityResponse> responses) {
        List<StashPullRequestComment> comments = new ArrayList<StashPullRequestComment>();
        for (StashPullRequestActivityResponse parsedResponse : responses) {
            for (StashPullRequestActivity a : parsedResponse.getPrValues()) {
                if (a != null && a.getComment() != null)
                    comments.add(a.getComment());
            }
        }
        return comments;
    }

    private StashPullRequestComment parseSingleCommentJson(String response) throws IOException {
        StashPullRequestComment parsedResponse;
        parsedResponse = mapper.readValue(
            response,
            StashPullRequestComment.class);
        return parsedResponse;
    }

    protected static StashPullRequestMergableResponse parsePullRequestMergeStatus(String response) throws IOException {
        StashPullRequestMergableResponse parsedResponse;
        parsedResponse = mapper.readValue(
            response,
            StashPullRequestMergableResponse.class);
        return parsedResponse;
    }

    private String pullRequestsPath() {
        return apiBaseUrl + this.project + "/repos/" + this.repositoryName + "/pull-requests/";
    }

    private String pullRequestPath(String pullRequestId) {
        return pullRequestsPath() + pullRequestId;
    }

    private String pullRequestsPath(int start) {
        String basePath = pullRequestsPath();
        return basePath.substring(0, basePath.length() - 1) + "?start=" + start;
    }

}

