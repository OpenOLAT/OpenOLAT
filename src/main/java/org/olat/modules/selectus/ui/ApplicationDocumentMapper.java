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
package org.olat.modules.selectus.ui;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.resources.AttachmentMediaResource;
import org.olat.modules.selectus.ui.resources.PDFCombinedMediaResource;
import org.olat.modules.selectus.ui.resources.ZIPOtherDocumentsCombinedMediaResource;

/**
 * 
 * Description:<br>
 * Implements a cacheable mapper for appplication's document
 * 
 * <P>
 * Initial Date:  3 mar. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationDocumentMapper implements Mapper {
	
	private final RecruitingPositionSecurityCallback secCallback;
	
	private final RecruitingService recruitingService;
	private final Translator translator = Util.createPackageTranslator(ApplicationDocumentMapper.class, Locale.ENGLISH);
	
	public ApplicationDocumentMapper(RecruitingPositionSecurityCallback secCallback) {
		this.secCallback = secCallback;
		recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		int appIdEndIndex = relPath.indexOf('/');
		if(appIdEndIndex < 0) return new NotFoundMediaResource();
		String appIdStr = relPath.substring(0, appIdEndIndex);
		Long appId;
		try {
			appId = Long.valueOf(appIdStr);
		} catch (NumberFormatException e) {
			return new NotFoundMediaResource();
		}

		Application application = recruitingService.getApplicationByKey(appId);
		if(application == null) return new NotFoundMediaResource();
		
		String documentType = relPath.substring(appIdEndIndex);
		if(documentType.startsWith("/")) {
			documentType = documentType.substring(1, documentType.length());
		}
		int nextToken = documentType.indexOf('/');
		if(nextToken > 0) {
			documentType = documentType.substring(0, nextToken);
		}
		if(documentType.endsWith(".pdf") || documentType.endsWith(".doc") || documentType.endsWith(".xls") || documentType.endsWith(".jpg")) {
			documentType = documentType.substring(0, documentType.length() - 4);
		} else if(documentType.endsWith(".docx") || documentType.endsWith(".xlsx") || documentType.endsWith(".jpeg")) {
			documentType = documentType.substring(0, documentType.length() - 5);
		}

		MediaResource resource;
		if(relPath.endsWith("_combined.pdf")) {
			Attachment attachment = DocumentEnum.combined.path(application);
			if(attachment == null) {
				resource = getCombinedResource(application, application.getPosition());
			} else {
				resource = getResource(application, DocumentEnum.combined);
			}
		} else if(relPath.endsWith("_combined.zip")) {
			resource = getOtherDocumentsCombinedResource(application, application.getPosition());
		} else {
			DocumentEnum doc = DocumentEnum.valueOf(documentType);
			resource = getResource(application, doc);
		}
		
		if(resource == null) {
			return new NotFoundMediaResource();
		}
		return resource;
	}
	
	private MediaResource getCombinedResource(Application application, Position position) {
		Application appWithAttributes = recruitingService.getApplicationWithAttributes(application);
		return new PDFCombinedMediaResource(appWithAttributes, position, secCallback, translator);
	}
	
	private MediaResource getOtherDocumentsCombinedResource(Application application, Position position) {
		return new ZIPOtherDocumentsCombinedMediaResource(application, position, translator);
	}
	
	private MediaResource getResource(Application application, DocumentEnum doc) {
		Attachment attachment = doc.path(application);
		if(attachment == null) {
			return null;
		}

		String documentName = application.getPosition().getDocumentName(doc, translator.getLocale());
		if(!StringHelper.containsNonWhitespace(documentName)) {
			documentName = translator.translate(doc.i18nKey());
		}

		String appId = application.getId() == null ? "" : application.getId() + "_";
		String name = appId + application.getPerson().getLastName() 
			+ "_" + application.getPerson().getFirstName() + "_"
			+ documentName;
		name = RecruitingHelper.normalizeFilename(name);
		
		if(StringHelper.containsNonWhitespace(attachment.getType())) {
			name += "." + attachment.getType();
		} else {
			name += ".pdf";
		}
		return new AttachmentMediaResource(name, attachment);
	}
}
