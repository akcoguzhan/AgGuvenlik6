package org.uludag.bmb.oauth;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxPKCEWebAuth;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.dropbox.core.oauth.DbxCredential;

import org.springframework.mock.web.MockHttpServletRequest;
import org.uludag.bmb.PropertiesReader;
import org.uludag.bmb.operations.dropbox.ClientUtils;

import com.sun.net.httpserver.HttpServer;

public class OAuthFlow {
    private ClientUtils dbAuth;

    public OAuthFlow() throws IOException, FileLoadException {
        dbAuth = new ClientUtils(new MockHttpServletRequest());
    }

    private DbxPKCEWebAuth createAuthRequest() throws IOException {
        return new DbxPKCEWebAuth(dbAuth.getRequestConfig(), dbAuth.getAppInfo());
    }

    public void startWithRedirect() throws IOException {
        DbxPKCEWebAuth pkceWebAuth = createAuthRequest();
        String authorizeUrl = pkceWebAuth.authorize(dbAuth.buildWebAuthRequest());

        openRedirectUri(authorizeUrl);

        try {
            CountDownLatch latch = new CountDownLatch(2);
            HttpServer server = HttpServer.create(new InetSocketAddress(PropertiesReader.getProperty("host"),
                    Integer.parseInt(PropertiesReader.getProperty("port"))), 0);

            server.createContext("/success", new OAuthSuccess(latch));

            server.createContext(PropertiesReader.getProperty("context"), exchange -> {
                try {
                    DbxAuthFinish authFinish = pkceWebAuth.finishFromRedirect(
                            dbAuth.getRedirectUri(),
                            dbAuth.getSession(),
                            RedirectParamsMapper.params(exchange.getRequestURI().getQuery()));

                    DbxCredential credential = new DbxCredential(authFinish.getAccessToken(), authFinish
                            .getExpiresAt(), authFinish.getRefreshToken(), dbAuth.getAppInfo().getKey());

                    DbxCredential.Writer.writeToFile(credential, new File(PropertiesReader.getProperty("authinfo")));
                    exchange.getResponseHeaders().set("Location", PropertiesReader.getProperty("successUri"));
                    exchange.sendResponseHeaders(302, 0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
            server.start();
            latch.await(Integer.parseInt(PropertiesReader.getProperty("timeout")), TimeUnit.SECONDS);
            server.stop(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openRedirectUri(String authorizeUrl) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        if (os.indexOf("mac") >= 0) {
            rt.exec("open " + authorizeUrl);
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            rt.exec("xdg-open " + authorizeUrl);
        } else {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + authorizeUrl);
        }

    }
}