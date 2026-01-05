/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.certificate.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.course.certificate.model.PreviewCertificate;

/**
 * 
 * Initial date: 5 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PreviewMediaResource implements MediaResource {
	private static final Logger log = Tracing.createLoggerFor(PreviewMediaResource.class);
	private PreviewCertificate preview;
	
	public PreviewMediaResource(PreviewCertificate preview) {
		this.preview = preview;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return true;
	}
	
	@Override
	public String getContentType() {
		return "application/type";
	}

	@Override
	public Long getSize() {
		return preview.getCertificate().length();
	}

	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(preview.getCertificate());
		} catch (FileNotFoundException e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		hres.setHeader("Content-Disposition", "filename*=UTF-8''Certificate_preview.pdf");
	}

	@Override
	public void release() {
		FileUtils.deleteDirsAndFiles(preview.getTmpDirectory(), true, true);
	}
}
