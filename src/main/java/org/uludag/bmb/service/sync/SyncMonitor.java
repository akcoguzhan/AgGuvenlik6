package org.uludag.bmb.service.sync;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.uludag.bmb.beans.config.LocalConfig;
import org.uludag.bmb.controller.config.ConfigController;

public class SyncMonitor implements Runnable {
    private FileAlterationObserver observer;
    private FileAlterationMonitor monitor;
    private FileAlterationListener listener;
    private final long interval = 1000;

    @Override
    public void run() {
        try {
            LocalConfig config = ConfigController.Settings.LoadSettings();
            String localPath = config.getLocalDropboxPath();
            File rootDirectory = new File(localPath);

            this.observer = new FileAlterationObserver(rootDirectory);
            this.monitor = new FileAlterationMonitor(interval);
            this.listener = new SyncAdaptor();

            new SyncServer();

            observer.addListener(listener);
            monitor.addObserver(observer);
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}