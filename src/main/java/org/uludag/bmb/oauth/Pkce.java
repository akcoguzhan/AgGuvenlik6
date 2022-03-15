package org.uludag.bmb.oauth;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxPKCEWebAuth;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;

import org.uludag.bmb.PropertiesReader;
import org.uludag.bmb.httpserver.AuthCommand;
import org.uludag.bmb.httpserver.AuthHttpServer;
import org.uludag.bmb.httpserver.ServerConfiguration;

import java.io.IOException;

public class Pkce {
    public DbxAuthFinish authorize(DbxAppInfo appInfo) throws IOException {
        DbxRequestConfig requestConfig = new DbxRequestConfig(PropertiesReader.getProperty("clientIdentifier"));
        DbxAppInfo appInfoWithoutSecret = new DbxAppInfo(appInfo.getKey());
        DbxPKCEWebAuth pkceWebAuth = new DbxPKCEWebAuth(requestConfig, appInfoWithoutSecret);
        
        DbxSessionStore sessionStore = new DbxSessionStore() {
            @Override
            public String get() {
                return null;
            }

            @Override
            public void set(String value) {
            }

            @Override
            public void clear() {
            }
        };

        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
                .withRedirectUri(PropertiesReader.getProperty("redirectUri"), sessionStore)
                .withTokenAccessType(TokenAccessType.OFFLINE)
                .withForceReapprove(false)
                .build();

        String authorizeUrl = pkceWebAuth.authorize(webAuthRequest);
        System.out.println("LINK: " + authorizeUrl);

        ServerConfiguration config = new ServerConfiguration();
        AuthHttpServer authHttpServer = new AuthHttpServer(config);
        AuthCommand authCommand = new AuthCommand(authHttpServer);

        String code = authCommand.getCode();
        System.out.println("s");

        try {
            return pkceWebAuth.finishFromCode(code);
        } catch (DbxException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
            return null;
        }
    }

}
