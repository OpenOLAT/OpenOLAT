/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class TaxonomyExportZipTemplateStep1 implements MediaResource {

    private static final Logger log = Tracing.createLoggerFor(TaxonomyExportZipTemplateStep1.class);

    private final String encoding;

    private final Translator translator;


    public TaxonomyExportZipTemplateStep1(String encoding, Translator translator) {
        this.encoding = encoding;
        this.translator = translator;
    }

    @Override
    public boolean acceptRanges() {
        return false;
    }

    @Override
    public String getContentType() {
        return "application/zip";
    }

    @Override
    public Long getSize() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public Long getLastModified() {
        return null;
    }

    @Override
    public long getCacheControlDuration() {
        return 0;
    }

    @Override
    public void prepare(HttpServletResponse hres) {
        try {
            hres.setCharacterEncoding(encoding);
        } catch (Exception e) {
            log.error("", e);
        }

        String label = translator.translate("taxonomy.export.template.label");
        String file = StringHelper.transformDisplayNameToFileSystemName(label) + ".zip";
        String encodedFileName = StringHelper.urlEncodeUTF8(file);
        hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        hres.setHeader("Content-Description", encodedFileName);

        try (ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
            zout.setLevel(9);
            zout.putNextEntry(new ZipEntry(label + "/media/MATH/background/"));
            zout.putNextEntry(new ZipEntry(label + "/media/MATH/teaser/"));
            zout.putNextEntry(new ZipEntry(label + "/media/MATH/GEO/background/"));
            zout.putNextEntry(new ZipEntry(label + "/media/MATH/GEO/teaser/"));
            zout.putNextEntry(new ZipEntry(label + "/media/BIO/background/"));
            zout.putNextEntry(new ZipEntry(label + "/media/BIO/teaser/"));
            zout.flush();
        } catch (IOException e) {
            String className = e.getClass().getSimpleName();
            if ("ClientAbortException".equals(className)) {
                log.debug("client browser probably abort when downloading zipped files", e);
            } else {
                log.error("client browser probably abort when downloading zipped files", e);
            }
        } catch (Exception e) {
            log.error("", e);
        }

    }

    @Override
    public void release() {
        // No need
    }
}
