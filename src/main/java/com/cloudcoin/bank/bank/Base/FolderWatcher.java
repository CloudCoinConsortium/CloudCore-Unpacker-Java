package com.cloudcoin.bank.bank.Base;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class FolderWatcher {

    WatchService watcher;

    public FolderWatcher(String filepath) {
        Path path = Paths.get(filepath);
        try {
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
                return true;
            }
        }

        return false;
    }
}
