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
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 29 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ZIPOtherDocumentsCombinedMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ZIPOtherDocumentsCombinedMediaResource.class);
	
	private final Position position;
	private final Translator translator;
	private final Application application;

	public ZIPOtherDocumentsCombinedMediaResource(Application application, Position position, Translator translator) {
		this.application = application;
		this.translator = translator;
		this.position = position;
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
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String fileName = application.getId() + "_" + application.getPerson().getLastName() 
				+ "_" + application.getPerson().getFirstName();
		fileName = RecruitingHelper.normalizeFilename(fileName) + "_combined.zip";
		String fileDescription = translator.translate("edit.application.document.combined.others");
		hres.setHeader("Content-Disposition","filename=\"" + StringHelper.urlEncodeUTF8(fileName) + "\"");
		hres.setHeader("Content-Description",StringHelper.urlEncodeUTF8(fileDescription));

		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		
		try(OutputStream out = hres.getOutputStream();
			ZipOutputStream zout = new ZipOutputStream(out)) {
			
			Set<DocumentEnum> inCombined = recruitingModule.getDocumentsInCombinedFile(position);
			for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
				DocumentEnum doc = docOption.getDoc();
				if(!inCombined.contains(doc)) {
					Attachment attachment = doc.path(application);
					if(attachment != null) {
						byte[] datas = erFrontendManager.getAttachmentDatas(attachment);
						if(datas != null && datas.length > 0) {
							String filename = position.getDocumentName(doc, translator.getLocale());
							if(!StringHelper.containsNonWhitespace(filename)) {
								filename = translator.translate(doc.i18nKey());
							}
							String name = application.getId() + "_" + filename + "_" + application.getPerson().getLastName() 
									+ "_" + application.getPerson().getFirstName();
							name = RecruitingHelper.normalizeFilename(name);
							if(StringHelper.containsNonWhitespace(attachment.getType())) {
								name += "." + attachment.getType();
							} else {
								name += ".pdf";
							}
							zout.putNextEntry(new ZipEntry(name));
							zout.write(datas);
							zout.closeEntry();
							zout.flush();
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
