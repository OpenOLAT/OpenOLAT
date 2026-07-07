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
package org.olat.modules.certificationprogram.ui.component;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSAllItemsFilter;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificatesMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(CertificatesMediaResource.class);
	
	private final boolean print;
	private final List<Certificate> certificates;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;

	public CertificatesMediaResource(List<Certificate> certificates, boolean print) {
		CoreSpringFactory.autowireObject(this);
		this.certificates = List.copyOf(certificates);
		this.print = print;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
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
	public void release() {
		//
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(StringHelper.transformDisplayNameToFileSystemName("certificates") + ".zip");
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			for(Certificate certificate:certificates) {
				if(certificate.getStatus() == CertificateStatus.pending) {
					certificate = certificatesManager.getCertificateById(certificate.getKey());
					dbInstance.commit();
				}
				VFSLeaf certificateFile = print
						? certificatesManager.getPrintCertificateLeaf(certificate)
						: certificatesManager.getCertificateLeaf(certificate);
				if(certificateFile != null) {
					ZipUtil.addToZip(certificateFile, "", zout, VFSAllItemsFilter.ACCEPT_ALL, false);
				}
			}
			dbInstance.commitAndCloseSession();
		} catch(Exception e) {
			log.error("", e);
		}
	}
}
