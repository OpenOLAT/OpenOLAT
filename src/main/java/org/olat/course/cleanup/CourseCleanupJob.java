package org.olat.course.cleanup;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.repository.handlers.CourseHandler;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.olat.core.util.vfs.VFSManager;

import java.io.File;

public class CourseCleanupJob extends QuartzJobBean {

    public static String CLEANUP_ROOT_DIR_NAME = "cleanup";
    private static final Logger log = Tracing.createLoggerFor(CourseHandler.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String relPath = File.separator + CLEANUP_ROOT_DIR_NAME;
        LocalFolderImpl rootFolder = VFSManager.olatRootContainer(relPath, null);
        // LMSUZH-436: make sure that rootFoler is not null before calling getBasefile()
        if (rootFolder.exists()) {
            File cleanupBaseDirectory = rootFolder.getBasefile();
            if (cleanupBaseDirectory.exists()) {
                try {
                    FileUtils.deleteDirsAndFiles(cleanupBaseDirectory, true, false);
                } catch (Exception e) {
                    log.error("Failed to empty olatdata/cleanup folder");
                }
            }
        }
    }
}
