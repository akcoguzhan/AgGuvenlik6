package org.uludag.bmb.operations.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.MemberSelector;

import org.uludag.bmb.beans.config.Config;
import org.uludag.bmb.beans.constants.Constants;
import org.uludag.bmb.beans.database.FileRecord;
import org.uludag.bmb.beans.dataproperty.CloudFileProperty;
import org.uludag.bmb.controller.config.ConfigController;
import org.uludag.bmb.service.cryption.Crypto;

import javafx.collections.ObservableList;

public class FileOperations {
    public static final void downloadFile(String filePath, String fileName) {
        new Thread(() -> {
            try {
                String localPath = ConfigController.Settings.LoadSettings().getLocalDropboxPath();
                String fileWithPath = localPath + filePath + fileName;

                if (!Files.exists(Paths.get(fileWithPath))) {
                    if (!Files.exists(Paths.get(localPath + filePath))) {
                        File fileFolder = new File(localPath + filePath);
                        fileFolder.mkdirs();
                    }
                    FileRecord record = Constants.fileRecordOperations.getRecordByPathAndName(filePath, fileName);
                    OutputStream downloadFile = new FileOutputStream(localPath + filePath + record.getEncryptedName());
                    Client.client.files().downloadBuilder(filePath + record.getEncryptedName()).download(downloadFile);
                    Constants.fileRecordOperations.updateRecordDownloadStatus(filePath, fileName, true);
                    Constants.fileRecordOperations.updateRecordSyncStatus(filePath, fileName, true);
                    downloadFile.close();
                    String decryptedName = Crypto.decryptName(
                            Base64.getUrlDecoder().decode(record.getEncryptedName().getBytes(StandardCharsets.UTF_8)),
                            record.getKey());
                    OutputStream decryptedFile = new FileOutputStream(localPath + filePath + decryptedName);
                    byte[] fileBytes = Crypto.decryptFile(
                            Files.readAllBytes(Paths.get(localPath + filePath + record.getEncryptedName())),
                            record.getKey());
                    decryptedFile.write(fileBytes);
                    decryptedFile.close();

                    Files.delete(Paths.get(localPath + filePath + record.getEncryptedName()));

                    Constants.notificationOperations
                            .insertNotification(filePath + fileName + " dosyası başarı ile indirildi!");
                }
            } catch (DbxException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static final void deleteFile(String path, String fileName) {
        Config config = ConfigController.Settings.LoadSettings();
        String localPath = config.getLocalDropboxPath();
        String filePath = localPath;
        String os = System.getProperty("os.name").toLowerCase();
        if (!path.equals("/")) {
            if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
                filePath += "/";
            } else {
                filePath += "\\";
            }
        }
        filePath += path + fileName;

        File file = new File(filePath);
        file.delete();
    }

    public static final void uploadFile(String uploadDirectory, File file) {
        Config config = ConfigController.Settings.LoadSettings();
        String localPath = config.getLocalDropboxPath();
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
        } else {
            uploadDirectory = uploadDirectory.replace("/", "\\");
        }

        String fileDirectory = localPath + uploadDirectory;
        try {
            String fileWithPath = fileDirectory + file.getName();
            if (!Files.exists(Paths.get(fileWithPath))) {
                if (!Files.exists(Paths.get(fileDirectory))) {
                    File fileFolder = new File(fileDirectory);
                    fileFolder.mkdirs();
                }

                try {
                    InputStream is = new FileInputStream(file);
                    Path destinationPath = (Path) Paths.get(fileWithPath);
                    Files.copy(is, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
        }

    }

    public static String getHash(String path, String fileName) {
        String localPath = ConfigController.Settings.LoadSettings().getLocalDropboxPath();
        byte[] inputBytes;
        try {
            inputBytes = Files.readAllBytes((Path) Paths.get(localPath + path + fileName));
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(inputBytes);
            byte[] digestedBytes = messageDigest.digest();

            String digestString = Base64.getUrlEncoder().encodeToString(digestedBytes);
            return digestString;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void changeSyncStatus(CloudFileProperty item, boolean status) {
        if (status) {
            String filePath = ConfigController.Settings.LoadSettings().getLocalDropboxPath();
            filePath += item.getFilePath() + item.getFileName();
            if (item.getFileSyncStatus()) {
                try {
                    InputStream is = new FileInputStream(new File(filePath));
                    deleteFromLocal(item.getFilePath(), item.getFileName());
                    deleteFromCloud(item.getFilePath(), item.getFileName());
                    Path destinationPath = (Path) Paths.get(filePath);
                    Files.copy(is, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                FileRecord record = Constants.fileRecordOperations.getRecordByPathAndName(item.getFilePath(),
                        item.getFileName());
                File file = new File(filePath);
                if (!record.getModificationDate()
                        .equals(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(file.lastModified()))) {
                    item.setChangeStatus(true);
                    Constants.fileRecordOperations.updateRecordChangedStatus(item.getFilePath(), item.getFileName(), true);
                    changeSyncStatus(item, status);
                }
            }
            Constants.fileRecordOperations.updateRecordSyncStatus(item.getFilePath(), item.getFileName(), true);
        } else {
            Constants.fileRecordOperations.updateRecordSyncStatus(item.getFilePath(), item.getFileName(), false);
        }
    }

    public static FileMetadata getMetadata(String filePath, String fileName) {
        FileRecord record = Constants.fileRecordOperations.getRecordByPathAndName(filePath,
                fileName);
        ListFolderResult result;
        try {
            if (filePath.equals("/")) {
                filePath = "";
            }
            result = Client.client.files().listFolder(filePath);

            List<Metadata> entries = result.getEntries();
            for (Metadata metadata : entries) {
                if (metadata instanceof FileMetadata &&
                        metadata.getName().equals(record.getEncryptedName())) {
                    FileMetadata data = (FileMetadata) metadata;
                    return data;

                }
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean shareFile(ObservableList<CloudFileProperty> fileList, List<String> userEmailList) {
        try {
            String filePath = fileList.get(0).getFilePath();
            String myPrivateKey = Constants.publicInfoOperations.getPrivateKey();
            for (String recieverEmail : userEmailList) {
                String recieverPublicKey = Constants.publicInfoOperations.getUserPublicKey(recieverEmail);
                for (CloudFileProperty shareFile : fileList) {
                    FileRecord file = Constants.fileRecordOperations.getRecordByPathAndName(filePath,
                            shareFile.getFileName());
                    String fileAESKey = file.getKey();
                    String encryptedAES = Crypto.KEY_EXCHANGE.encryptWithPrivate(fileAESKey, myPrivateKey);
                    String AESfirstPart = encryptedAES.substring(0, 200);
                    String AESsecondPart = encryptedAES.substring(200, encryptedAES.length());
                    String secondEncryptedAES1 = Crypto.KEY_EXCHANGE.encryptWithPublic(AESfirstPart, recieverPublicKey);
                    String secondEncryptedAES2 = Crypto.KEY_EXCHANGE.encryptWithPublic(AESsecondPart,
                            recieverPublicKey);
                    String encryptedFileName = Constants.fileRecordOperations
                            .getRecordByPathAndName(shareFile.getFilePath(), shareFile.getFileName())
                            .getEncryptedName();
                    Constants.publicInfoOperations.insertSharedFileKey(recieverEmail,
                    Constants.publicInfoOperations.getUserEmail(), secondEncryptedAES1,
                            secondEncryptedAES2,
                            encryptedFileName);

                    List<MemberSelector> member = new ArrayList<>();
                    member.add(MemberSelector.email(recieverEmail));
                    Constants.fileRecordOperations.updateRecordSharedAccounts(userEmailList, shareFile.getFilePath(), shareFile.getFileName());
                    Client.client.sharing().addFileMember(file.getPath() + file.getEncryptedName(), member);
                    Constants.notificationOperations.insertNotification(shareFile.getFilePath() + shareFile.getFileName() + " dosyası seçili hesaplar ile paylaşıldı!");
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Constants.notificationOperations.insertNotification("Dosya Paylaşım Hatası!");
            return false;
        }
    }

    public static void deleteFile(CloudFileProperty file) {
        if (file.getFileSyncStatus()) {
            FileOperations.deleteFromCloud(file.getFilePath(), file.getFileName());
        } else {
            FileOperations.deleteFromLocal(file.getFilePath(), file.getFileName());
        }
    }

    private static void deleteFromCloud(String path, String fileName) {
        try {
            FileRecord file = Constants.fileRecordOperations.getRecordByPathAndName(path, fileName);
            DeleteResult ds = Client.client.files().deleteV2(path + file.getEncryptedName());
            Constants.fileRecordOperations.deleteRecord(fileName, path);
            FileOperations.deleteFile(path, fileName);
            Constants.notificationOperations.insertNotification(path + fileName + " dosyası buluttan ve yerelden silindi!");
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFromLocal(String path, String fileName) {
        Config config = ConfigController.Settings.LoadSettings();
        String localPath = config.getLocalDropboxPath();
        String filePath = localPath;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            filePath += "/" + path;
        } else {
            filePath += "\\" + path;
        }
        filePath += fileName;

        try {
            if (Files.deleteIfExists((Path) Paths.get(filePath))) {
                if (Constants.fileRecordOperations.getRecordByPathAndName(path, fileName) != null) {
                    Constants.fileRecordOperations.updateRecordDownloadStatus(path, fileName, false);
                }
                Constants.notificationOperations.insertNotification(path + fileName + " dosyası yerelden silindi!");
            } else {
                Constants.notificationOperations
                        .insertNotification(path + fileName + " dosyası yerelde bulunmadığı için silinemedi!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
