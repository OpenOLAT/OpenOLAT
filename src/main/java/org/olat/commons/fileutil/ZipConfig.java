package org.olat.commons.fileutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.stereotype.Component;

/**
 * Retrieves zip configuration properties and provide utility methods. <br>
 * Selected file size to zip cannot exceed the configured max size.
 *
 * Initial Date: 23.01.2015 <br>
 *
 * @author lavinia
 * @author anvarzonzurajev
 */
@Component
public class ZipConfig {

    private final static int DEFAULT_ZIP_SELECTION_MAX_SIZE_MB = 500;
    private final static int ONE_MEGA_BYTE_IN_BYTES = 1000000;

    private static AtomicInteger maxSize = new AtomicInteger(0);

    ZipConfig() {
        // spring default constructor
    }

    /**
     * property in olat.properties <br>
     * This is the configured max size for the selected items, or a default.
     *
     */
    private int getSelectionMaxSizeBytes() {
        if (maxSize.get() == 0) {
            int maxSizeMB = FolderConfig.getMaxZipSizeMB();
            if (maxSizeMB > 0) {
                maxSize.set(maxSizeMB * ONE_MEGA_BYTE_IN_BYTES);
            } else {
                maxSize.set(DEFAULT_ZIP_SELECTION_MAX_SIZE_MB * ONE_MEGA_BYTE_IN_BYTES);
            }
        }
        return maxSize.get();
    }

    /**
     * Checks if the total size of vfsFiles doesn't exceed the configured maxSize.
     */
    public boolean isItemsSizeOK(List<VFSItem> vfsFiles) {
        return isItemsSizeOK(vfsFiles, getSelectionMaxSizeBytes());
    }

    /**
     * Checks if the total size of vfsFiles doesn't exceed maxSize. The items are supposed to be zipped.
     */
    boolean isItemsSizeOK(List<VFSItem> vfsFiles, long maxSize) {
        ItemsSizeTotal filesSize = new ItemsSizeTotal();
        Iterator<VFSItem> iter = vfsFiles.iterator();
        while (iter.hasNext()) {
            getItemsSizeRecursive(iter.next(), filesSize);
        }
        // System.out.println("checkFilesSize: " + filesSize.getSizeInBytes());
        boolean isSizeOK = filesSize.getSizeInBytes() < maxSize;
        // System.out.println("isSizeOK: " + isSizeOK);
        return isSizeOK;
    }

    /**
     * Traverse recursive and calculate total size.
     */
    void getItemsSizeRecursive(VFSItem vfsItem, ItemsSizeTotal filesSize) {
        if (vfsItem instanceof VFSContainer) {
            List<VFSItem> items = ((VFSContainer) vfsItem).getItems();
            for (Iterator<VFSItem> iter = items.iterator(); iter.hasNext();) {
                VFSItem item = iter.next();
                getItemsSizeRecursive(item, filesSize);
            }
        } else {
            if (vfsItem instanceof VFSLeaf) {
                long itemSize = ((VFSLeaf) vfsItem).getSize();

                long size = filesSize.getSizeInBytes() + itemSize;
                filesSize.setSizeInBytes(size);
                filesSize.addFileName(vfsItem.getName());
            }
        }
    }
}

class ItemsSizeTotal {

    private List<String> debugFileList = new ArrayList<String>();

    private long sizeBytes = 0;

    protected long getSizeInBytes() {
        return sizeBytes;
    }

    protected void setSizeInBytes(long size) {
        this.sizeBytes = size;
    }

    /**
     * Just for debugging.
     */
    void addFileName(String fileName) {
        debugFileList.add(fileName);
    }

    /**
     * Just for debugging.
     */
    List<String> getFileNameList() {
        return Collections.unmodifiableList(debugFileList);
    }

}