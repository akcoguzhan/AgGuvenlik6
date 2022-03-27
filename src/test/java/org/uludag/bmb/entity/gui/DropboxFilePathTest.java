package org.uludag.bmb.entity.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import org.junit.Test;
import org.uludag.bmb.entity.dropbox.DbClient;

public class DropboxFilePathTest {

    @Test
    public void listDropboxFolderPaths() throws IOException, FileLoadException, ListFolderErrorException, DbxException {
        DbClient client = new DbClient();
        client.login();

        ListFolderResult result = client.getClient().files().listFolderBuilder("")
                .withIncludeDeleted(false)
                .withRecursive(true)
                .start();

        List<Metadata> entries = result.getEntries();

        for (Metadata metadata : entries) {
            if (metadata instanceof FolderMetadata) {
                System.out.println(metadata.getPathLower());
            }
        }
    }

}