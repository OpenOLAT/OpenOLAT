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
package org.olat.course.certificate;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 22.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateDispatcher implements Dispatcher {
	
	private CertificatesManager certificatesManager;
	
	/**
	 * [used by Spring]
	 * @param certificatesManager
	 */
	public void setCertificatesManager(CertificatesManager certificatesManager) {
		this.certificatesManager = certificatesManager;
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String uuid = origUri.substring(uriPrefix.length());
		int indexSuffix = uuid.indexOf('/');
		if(indexSuffix > 0) {
			uuid = uuid.substring(0, indexSuffix);
		}
		MediaResource resource;
		Certificate certificate = certificatesManager.getCertificateByUuid(uuid);
		if(certificate == null) {
			resource = new NotFoundMediaResource();
		} else {
			VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
			resource = new VFSMediaResource(certificateFile);
		}
		ServletUtil.serveResource(request, response, resource);
	}
}
