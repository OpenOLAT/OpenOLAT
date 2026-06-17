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
package org.olat.modules.selectus.ui.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.document.PDFApplicationCombinedHelper;

/**
 * 
 * Initial date: 6 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PDFCombinedMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(PDFCombinedMediaResource.class);
	
	private final Position position;
	private final Application application;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private final Translator translator;

	public PDFCombinedMediaResource(Application application, Position position, RecruitingPositionSecurityCallback secCallback, Translator translator) {
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		this.translator = translator;
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
	public void prepare(HttpServletResponse hres) {
		PDFApplicationCombinedHelper helper = new PDFApplicationCombinedHelper(application, position, secCallback, translator);
		String fileName = helper.getPDFFilename();
		String fileDescription = translator.translate("edit.application.document.combined");
		hres.setHeader("Content-Disposition","filename=\"" + StringHelper.urlEncodeUTF8(fileName) + "\"");
		hres.setHeader("Content-Description",StringHelper.urlEncodeUTF8(fileDescription));
		
		try(OutputStream out = hres.getOutputStream()) {
			helper.combineDocumentsStreamed(out);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
