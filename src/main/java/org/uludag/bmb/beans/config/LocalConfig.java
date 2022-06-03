package org.uludag.bmb.beans.config;


public class LocalConfig {
    private String localDropboxPath;
    private String privateRsaKey;
    private String userEmail;
    private String dataDirectory;
    private String cacheRecievedFileDirectory;
    private String cacheSharedFileDirectory;

    public LocalConfig(String path, String privateRsaKey, String userEmail, String dataDirectory, String cacheSharedFileDirectory, String cacheRecievedFileDirectory) {
        this.localDropboxPath = path;
        this.privateRsaKey = privateRsaKey;
        this.userEmail = userEmail;
        this.dataDirectory = dataDirectory;
        this.cacheSharedFileDirectory = cacheSharedFileDirectory;
        this.cacheRecievedFileDirectory = cacheRecievedFileDirectory;
    }

    public LocalConfig(){

    }

    public String getDataDirectory(){
        return this.dataDirectory;
    }

    public String getCacheSharedFileDirectory() {
        return this.cacheSharedFileDirectory;
    }

    public String getCacheRecievedFileDirectory() {
        return this.cacheRecievedFileDirectory;
    }
    
    public String getLocalDropboxPath(){
        return this.localDropboxPath;
    }

    public String getPrivateRsaKey(){
        return this.privateRsaKey;
    }

    public String getUserEmail(){
        return this.userEmail;
    }
}
