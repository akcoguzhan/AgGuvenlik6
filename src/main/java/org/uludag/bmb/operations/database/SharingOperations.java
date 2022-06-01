package org.uludag.bmb.operations.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.uludag.bmb.beans.constants.Constants.TABLES;
import org.uludag.bmb.beans.database.sharing.SharedFile;
import org.uludag.bmb.controller.config.ConfigController;

public class SharingOperations extends DatabaseOperations {

    public void insertNewUser(String publicKey) {
        String eMail = ConfigController.Settings.LoadSettings().getUserEmail();
        String publicKeyQuery = "BEGIN IF NOT EXISTS (SELECT * FROM " + TABLES.userInformation
                + " WHERE email=?)"
                + "BEGIN INSERT INTO " + TABLES.userInformation
                + "(email, publicKey) VALUES(?,?) END "
                + "END";
        try {
            PreparedStatement statement = databaseController.getAzureCon().prepareStatement(publicKeyQuery);
            statement.setString(1, eMail);
            statement.setString(2, eMail);
            statement.setString(3, publicKey);

            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SharedFile getSharedFileByEncryptedName(String encryptedName) {
        try {
            String query = "SELECT * FROM " + TABLES.sharedFiles
                    + " WHERE encryptedName=?";
            PreparedStatement statement = databaseController.getAzureCon().prepareStatement(query);
            statement.setString(1, encryptedName);
            ResultSet rst = statement.executeQuery();
            List<SharedFile> sharedFiles = new ArrayList<SharedFile>();
            while (rst.next()) {
                sharedFiles.add(new SharedFile(rst.getString("recieverEmail"), rst.getString("senderEmail"),
                        rst.getString("encryptedName"),
                        rst.getString("fileKeyPart1"), rst.getString("fileKeyPart2")));
            }
            return sharedFiles.get(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> getApplicationUsersList() {
        try {
            Statement statement = databaseController.getAzureCon().createStatement();
            String query = "SELECT email FROM " + TABLES.userInformation;
            ResultSet rst = statement.executeQuery(query);
            List<String> emailList = new ArrayList<String>();
            while (rst.next()) {
                emailList.add(rst.getString("email"));
            }
            return emailList;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getPublicKey(String email) {
        try {
            String query = "SELECT publicKey FROM " + TABLES.userInformation + " WHERE email=?";
            PreparedStatement statement = this.databaseController.getAzureCon().prepareStatement(query);
            statement.setString(1, email);
            String publicKey = "";
            ResultSet rst = statement.executeQuery();
            while (rst.next()) {
                publicKey = rst.getString("publicKey");
            }
            return publicKey;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getPrivateKey() {
        return ConfigController.Settings.LoadSettings().getPrivateRsaKey();
    }

    public String getUserEmail() {
        return ConfigController.Settings.LoadSettings().getUserEmail();
    }

    public void insertSharedFile(String recieverEmail, String encryptedFileName, String fileKeyPart1, String fileKeyPart2) {
        String query = "INSERT INTO " + TABLES.sharedFiles
                + "(recieverEmail, senderEmail, encryptedName, fileKeyPart1, fileKeyPart2) VALUES(?,?,?,?,?)";
        try {
            PreparedStatement statement = this.databaseController.getAzureCon().prepareStatement(query);
            statement.setString(1, recieverEmail);
            statement.setString(2, getUserEmail());
            statement.setString(3, encryptedFileName);
            statement.setString(4, fileKeyPart1);
            statement.setString(5, fileKeyPart2);

            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
