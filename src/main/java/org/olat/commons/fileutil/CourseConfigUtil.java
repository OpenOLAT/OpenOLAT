/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.commons.fileutil;

import org.olat.core.util.FileUtils;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.util.vfs.QuotaManager;

import java.io.File;

/**
 * Course configuration utility. <br>
 *
 * Initial Date: 09.07.2015 <br>
 *
 * @author lavinia
 */
public class CourseConfigUtil {

    private final static int DEFAULT_EXPORT_MAX_SIZE_MB = 5000;
    private static long EXPORT_MAX_SIZE_BYTE = 0;

    /**
     * This is the configured max size for course export/copy.
     */
    private static long getExportMaxSize() {
        if (EXPORT_MAX_SIZE_BYTE == 0) {
            long exportMaxSizeMB = FolderConfig.getMaxCourseExportSizeMB();
            if (exportMaxSizeMB == 0) {
                exportMaxSizeMB = DEFAULT_EXPORT_MAX_SIZE_MB;
            }
            long maxSizeB = exportMaxSizeMB * 1024 * 1024;
            EXPORT_MAX_SIZE_BYTE = maxSizeB;
        }

        return EXPORT_MAX_SIZE_BYTE;
    }

    public static void checkAgainstCustomQuotas(File exportDirectory) throws CustomQuotaDetectedException {
        String path = exportDirectory.getPath();
        String pathPrefix = path.substring(path.indexOf("/course/"));
        QuotaManager qm = QuotaManager.getInstance();
        if (qm.hasCustomQuotas(pathPrefix)) {
            throw new CustomQuotaDetectedException("Course has custom quotas for its nodes");
        }
    }

    /**
     * Throws FileSizeLimitExceededException if the exportDirectory size exceeds the configured size (course.export.or.copy.max.size.mb).
     */
    public static void checkAgainstConfiguredMaxSize(File exportDirectory) throws FileSizeLimitExceededException {
        long maxSize = getExportMaxSize();
        if (FileUtils.getDirSize(exportDirectory) > maxSize) {
            throw new FileSizeLimitExceededException("Export size exceeds the configured maxSize: " + maxSize);
        }
    }
}
