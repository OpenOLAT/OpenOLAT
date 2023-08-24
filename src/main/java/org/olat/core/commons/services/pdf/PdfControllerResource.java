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
package org.olat.core.commons.services.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfControllerResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(PdfControllerResource.class);
	
	private final String label;
	private final Identity identity;
	private final ControllerCreator creator;
	private final WindowControl windowControl;
	private final PdfOutputOptions options;
	
	@Autowired
	private PdfService pdfService;
	
	public PdfControllerResource(String label, Identity identity, ControllerCreator creator, WindowControl windowControl,
			PdfOutputOptions options) {
		CoreSpringFactory.autowireObject(this);
		this.label = label;
		this.identity = identity;
		this.creator = creator;
		this.windowControl = windowControl;
		this.options = options;
	}

	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/pdf";
	}

	@Override
	public Long getSize() {
		return null;
	}
	
	@Override
	public Long getLastModified() {
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
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

		String file = secureLabel + ".pdf";
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
		hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
		
		try(OutputStream out = hres.getOutputStream()) {
			pdfService.convert(identity, creator, windowControl, options, out);
		} catch(IOException e) {
			log.error("", e);
		}
	}
}
