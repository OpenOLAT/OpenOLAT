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
package org.olat.modules.selectus.ui.mail;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Initial date: 13 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LetterMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(LetterMediaResource.class);
	
	private final String letter;
	private final String filename;

	@Autowired
	private PdfService pdfService;
	
	public LetterMediaResource(String letter) {
		this.letter = letter;
		this.filename = null;
		CoreSpringFactory.autowireObject(this);
	}
	
	public LetterMediaResource(String letter, String filename) {
		this.letter = letter;
		this.filename = filename;
		CoreSpringFactory.autowireObject(this);
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
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
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
		
		String label = filename;
		if(label == null) {
			label = "Letter";
		}
		String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);
		if(!secureLabel.endsWith(".pdf")) {
			secureLabel += ".pdf";
		}
		hres.setHeader("Content-Disposition", "filename*=UTF-8''" + StringHelper.urlEncodeUTF8(secureLabel));

		try(OutputStream out = hres.getOutputStream()) {
			pdfService.convert(letter, PdfOutputOptions.defaultOptions(), out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
}