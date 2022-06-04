package org.uludag.bmb.beans.database.sharing;

public class RecievedFile {
    private String fileKey;
    private String senderEmail;
    private String encryptedName;
    private String decryptedName;
    private int id;

    public RecievedFile(String senderEmail, String encryptedName, String decryptedName, String fileKey) {
        this.senderEmail = senderEmail;
        this.encryptedName = encryptedName;
        this.decryptedName = decryptedName;
        this.fileKey = fileKey;
    }

    public RecievedFile(int id, String senderEmail, String encryptedName, String decryptedName, String fileKey) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.encryptedName = encryptedName;
        this.decryptedName = decryptedName;
        this.fileKey = fileKey;
    }

    public RecievedFile() {

    }

    public String getFileKey() {
        return this.fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getSenderEmail() {
        return this.senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getDecryptedName() {
        return this.decryptedName;
    }

    public void setDecryptedName(String decryptedName) {
        this.decryptedName = decryptedName;
    }

    public String getEncryptedName() {
        return this.encryptedName;
    }

    public void setEncryptedName(String encryptedName) {
        this.encryptedName = encryptedName;
    }

    public void setSecondDecryptedKey(String secondDecryptedKey) {
        this.fileKey = secondDecryptedKey;
    }

}
