package org.uludag.bmb.entity.dropbox;

import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;

import org.uludag.bmb.PropertiesReader;

public class DbClient {
    private DbxClientV2 client;
    private DbAuth auth;
    private DbxCredential credential;

    public DbClient() throws IOException, FileLoadException {
        this.auth = new DbAuth();
    }

    public void login() {
        try {
            credential = DbxCredential.Reader.readFromFile(PropertiesReader.getProperty("authinfo"));
            this.client = new DbxClientV2(auth.getRequestConfig(), credential);
            this.client.users().getCurrentAccount();
        } catch (DbxException | FileLoadException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public DbxClientV2 getClient() {
        return this.client;
    }
}
