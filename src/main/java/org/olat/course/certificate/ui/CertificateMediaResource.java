/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.certificate.ui;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 18.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateMediaResource implements MediaResource {

	private VFSLeaf certificate;
	private String filename;
	private boolean forceDownload;
	
	public CertificateMediaResource(String filename, VFSLeaf certificate, boolean forceDownload) {
		this.certificate = certificate;
		this.filename = filename;
		this.forceDownload = forceDownload;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_DAY;
	}
	
	@Override
	public boolean acceptRanges() {
		return true;
	}
	
	@Override
	public String getContentType() {
		return forceDownload ? "application/octet-stream" : "application/pdf";
	}

	@Override
	public Long getSize() {
		return certificate.getSize();
	}

	@Override
	public InputStream getInputStream() {
		return certificate.getInputStream();
	}

	@Override
	public Long getLastModified() {
		return certificate.getLastModified();
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		if(forceDownload) {
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
		} else {
			hres.setHeader("Content-Disposition", "filename*=UTF-8''" + filename);
		}
	}

	@Override
	public void release() {
		//
	}
}
