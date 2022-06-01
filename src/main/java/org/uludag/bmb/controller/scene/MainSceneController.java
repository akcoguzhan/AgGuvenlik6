package org.uludag.bmb.controller.scene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.dropbox.core.DbxException;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.dropbox.core.v2.files.UploadErrorException;

import org.uludag.bmb.PropertiesReader;
import org.uludag.bmb.beans.database.FileRecord;
import org.uludag.bmb.beans.dataproperty.CloudFileProperty;
import org.uludag.bmb.beans.dataproperty.NotificationListCellFactory;
import org.uludag.bmb.beans.dataproperty.StyledHyperLink;
import org.uludag.bmb.operations.dropbox.FileOperations;
import org.uludag.bmb.operations.scenedatasource.UITrees;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainSceneController extends Controller implements Initializable {
    @FXML
    public Button btnDownload;

    @FXML
    public Button btnNewFolder;

    @FXML
    public Button btnUpload;

    @FXML
    public Button btnClearNotifications;

    @FXML
    public Button btnShowNotification;

    @FXML
    public TreeView<String> directoriesHierarchyView;

    @FXML
    public Pane notificationPane;

    @FXML
    public Circle notificationDot;

    @FXML
    public SplitPane selectedDirectoryPathPane;

    @FXML
    public TableView<CloudFileProperty> fileListView;

    @FXML
    public Pane fileDetailPane;

    @FXML
    public TableColumn<CloudFileProperty, CheckBox> syncStatusColumn;

    @FXML
    public TableColumn<CloudFileProperty, String> filePathColumn;

    @FXML
    public TableColumn<CloudFileProperty, String> fileNameColumn;

    @FXML
    public TableColumn<CloudFileProperty, Date> modificationDateColumn;

    @FXML
    public TableColumn<CloudFileProperty, CheckBox> fileChangeStatusColumn;

    @FXML
    public TableColumn<CloudFileProperty, CheckBox> fileDownloadStatusColumn;

    @FXML
    public ListView<String> notificationListView;

    @FXML
    public Label detailFileName;

    @FXML
    public Label detailFileSize;

    @FXML
    public Label detailModificationDate;

    @FXML
    public ListView<String> fileSharedAccountListView;

    @FXML
    public Tooltip toolTipFileName;

    @FXML
    public Tooltip toolTipFileSize;

    @FXML
    public Tooltip toolTipModificationDate;

    @FXML
    public Button fileIcon;

    @FXML
    public ListView<String> recievedFilesList;

    public MainSceneController() throws FileLoadException {
        super(PropertiesReader.getProperty("mainSceneFxml"),
                Integer.parseInt(PropertiesReader.getProperty("mainSceneWidth")),
                Integer.parseInt(PropertiesReader.getProperty("mainSceneHeigth")));
    }

    @Override
    public void displayScene(Stage stage) {
        this.stage = stage;
        stage.setScene(scene);
        stage.hide();
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeItem<String> root = UITrees.Hierarchy.getAsTreeItem("");
        directoriesHierarchyView.setRoot(root);
        directoriesHierarchyView.setShowRoot(false);

        notificationListView.setCellFactory(param -> new NotificationListCellFactory());
        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        new NotificationPaneController(this);
        // List<SharedFileMetadata> entries;
        // try {
        // entries = Client.client.sharing().listReceivedFiles().getEntries();
        // if (entries.size() != 0) {
        // for (SharedFileMetadata entry : entries) {
        // SharedFile sharedFile =
        // publicInfoOperations.getSharedFileByEncryptedName(entry.getName());
        // Crypto.SHARE.DECRYPT_PREVIEW(sharedFile);
        // String decryptedName =
        // fileRecordOperations.getSharedRecordPreview(entry.getName())
        // .getDecryptedName();
        // sharedFilesList.getItems().add(decryptedName);
        // //paylaşılan dosyadan çıkılacak
        // }
        // }
        // } catch (DbxException e) {
        // e.printStackTrace();
        // }
    }

    @FXML
    void clearNotifications(MouseEvent event) {
        notificationListView.getItems().clear();
        notificationDot.visibleProperty().set(false);
        notificationPane.visibleProperty().set(false);
        fileDetailPane.setLayoutY(34);
    }

    @FXML
    void deleteSelectedFiles(ActionEvent event) {
        for (CloudFileProperty file : fileListView.getSelectionModel().getSelectedItems()) {
            FileOperations.deleteFile(file);
        }
    }

    @FXML
    void changeSyncStatusOn(ActionEvent event) {
        List<CloudFileProperty> selectedItems = fileListView.getSelectionModel().getSelectedItems();
        for (CloudFileProperty item : selectedItems) {
            item.syncStatus().get().selectedProperty().set(true);
            FileOperations.changeSyncStatus(item, true);
        }
    }

    @FXML
    void changeSyncStatusOff(ActionEvent event) {
        List<CloudFileProperty> selectedItems = fileListView.getSelectionModel().getSelectedItems();
        for (CloudFileProperty item : selectedItems) {
            item.syncStatus().get().selectedProperty().set(false);
            FileOperations.changeSyncStatus(item, false);
        }
    }

    @FXML
    void hierarchySelectFolder(MouseEvent event) {
        TreeItem<String> selectedFolder = (TreeItem<String>) directoriesHierarchyView.getSelectionModel()
                .getSelectedItem();
        try {
            StringBuilder folderPath = new StringBuilder();
            for (; selectedFolder.getParent() != null; selectedFolder = selectedFolder.getParent()) {
                folderPath.insert(0, selectedFolder.getValue() + "/");
            }
            ObservableList<CloudFileProperty> files = fileRecordOperations.getRecordByPath(folderPath.toString());
            fileListView.setItems(files);
            fileListView.refresh();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    selectedDirectoryPathPane.getItems().clear();
                    List<String> selectedBarPath = Arrays.asList(folderPath.toString().split("/"));
                    if (selectedBarPath.size() == 0) {
                        selectedDirectoryPathPane.getItems()
                                .add(new StyledHyperLink(selectedDirectoryPathPane, fileListView, ""));
                        return;
                    }
                    for (String pathPart : Arrays.asList(folderPath.toString().split("/"))) {
                        selectedDirectoryPathPane.getItems()
                                .add(new StyledHyperLink(selectedDirectoryPathPane, fileListView, pathPart));
                    }

                }
            });
        } catch (NullPointerException e) {
        }
    }

    @FXML
    void downloadSelectedFile(ActionEvent event) {
        for (var file : fileListView.getSelectionModel().getSelectedItems()) {
            FileOperations.downloadFile(file.getFilePath(), file.getFileName());
        }
    }

    @FXML
    void uploadSelectedFile(ActionEvent event) throws IOException, UploadErrorException, DbxException {
        var folderPathNode = selectedDirectoryPathPane.getItems();
        String uploadDirectory = "/";
        if (folderPathNode.size() != 0) {
            for (int index = 1; index < folderPathNode.size(); index++) {
                uploadDirectory += ((Hyperlink) selectedDirectoryPathPane.getItems().get(index)).getText().toString();
            }
        }

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        FileOperations.uploadFile(uploadDirectory, selectedFile);
    }

    @FXML
    void showNotifications(MouseEvent event) {
        if (!notificationPane.visibleProperty().get()) {
            notificationPane.visibleProperty().set(true);
            fileDetailPane.setLayoutY(360);
        } else {
            notificationPane.visibleProperty().set(false);
            fileDetailPane.setLayoutY(34);
        }

    }

    @FXML
    void showSelectedFileDetails(MouseEvent event) {
        fileIcon.getStyleClass().remove(1);
        CloudFileProperty selectedFile = fileListView.getSelectionModel().getSelectedItem();
        String fileExtension = selectedFile.getFileName().split(Pattern.quote("."))[1];
        switch (fileExtension) {
            case "png":
            case "svg":
            case "jpeg":
            case "jpg":
                fileIcon.getStyleClass().add("iconMedia");
                break;
            case "pdf":
                fileIcon.getStyleClass().add("iconPdf");
                break;
            case "txt":
            case "docx":
            case "doc":
                fileIcon.getStyleClass().add("iconText");
                break;
            default:
                fileIcon.getStyleClass().add("iconDefault");
                break;
        }
        FileRecord selectedFileRecord = fileRecordOperations.getRecordByPathAndName(selectedFile.getFilePath(),selectedFile.getFileName());
        detailFileName.setText(selectedFileRecord.getName());
        detailFileSize.setText(selectedFileRecord.getFileSize());
        detailModificationDate.setText(selectedFileRecord.getModificationDate());
        toolTipFileName.setText(selectedFileRecord.getName());
        toolTipFileSize.setText(selectedFileRecord.getFileSize());
        toolTipModificationDate.setText(selectedFileRecord.getModificationDate());
        fileSharedAccountListView.getItems().clear();
        fileSharedAccountListView.getItems().addAll(selectedFile.getSharedAccounts());
    }

    @FXML
    void saveSelectedSharedFile(ActionEvent event) {
        new SaveSharedFileSceneController(this, "saveSharedFileScene", "Paylaşılan Dosyayı Kaydet");
    }

    @FXML
    void createNewFolder(MouseEvent event) {
        new FolderCreationSceneController(this, "folderCreationScene", "Yeni Klasör Oluştur");
    }

    @FXML
    void shareSelectedFiles(ActionEvent event) {
        new ShareWindowController(this, "shareFilesScene", "Dosya Paylaş");
    }
}