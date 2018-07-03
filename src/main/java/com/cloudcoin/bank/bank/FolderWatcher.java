package com.cloudcoin.bank.bank;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class FolderWatcher {

    WatchService watcher;

    String filepath;

    public FolderWatcher(String filepath) {
        this.filepath = filepath;
        Initialize(filepath);
    }

    private void Initialize(String filepath) {
        try {
            File folder = new File(filepath);
            if (!folder.exists())
                folder.mkdir();
            Path path = Paths.get(filepath);
            watcher = path.getFileSystem().newWatchService();
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Import folder not being watched. " + e.getMessage());
        }
    }

    public boolean newFileDetected() {
        WatchKey watchKey = watcher.poll();
        if (watchKey == null)
            return false;

        List<WatchEvent<?>> events = watchKey.pollEvents();
        for (int i = 0; i < events.size(); i++) {
            WatchEvent event = events.get(i);
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                System.out.println("Created: " + event.context().toString());

                resetWatcher(watchKey);

                return true;
            }
        }

        resetWatcher(watchKey);

        return false;
    }

    /**
     * If the WatchKey does not successfully reset, then we create a new WatchService.
     *
     * @param watchKey
     */
    private void resetWatcher(WatchKey watchKey) {
        if (!watchKey.reset()) {
            try {
                watcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                watcher = null;
            }

            Initialize(filepath);
        }
    }
}