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
import org.olat.core.util.Util;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.ui.resources.AttachmentMediaResource;
import org.olat.modules.selectus.ui.resources.PDFCombinedExpertOpinionsMediaResource;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceDocumentMapper implements Mapper {

	private final RecruitingService recruitingService;
	private final Translator translator = Util.createPackageTranslator(ApplicationDocumentMapper.class, Locale.ENGLISH);
	
	private final Position position;
	private final Application application;
	private final RecruitingPositionSecurityCallback secCallback;
	
	public ReferenceDocumentMapper(Application application, Position position, RecruitingPositionSecurityCallback secCallback) {
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}

		MediaResource resource;
		if(relPath.endsWith("expert_opinions_combined.pdf")) {
			resource =  getCombinedExpertOpinions();
		} else {
			int idEndIndex = relPath.indexOf('/');
			if(idEndIndex < 0) return new NotFoundMediaResource();
			String idStr = relPath.substring(0, idEndIndex);
			
			Long id;
			try {
				id = Long.valueOf(idStr);
			} catch (NumberFormatException e) {
				return new NotFoundMediaResource();
			}
	
			Reference reference = recruitingService.getReferenceById(id);
			if(reference == null) return new NotFoundMediaResource();
			
			resource = getResource(reference);
			if(resource == null) {
				resource = new NotFoundMediaResource();
			}
		}
		return resource;
	}
	
	private MediaResource getCombinedExpertOpinions() {
		Application appWithAttributes = recruitingService.getApplicationWithAttributes(application);
		return new PDFCombinedExpertOpinionsMediaResource(appWithAttributes, position, secCallback, translator);
	}
	
	private MediaResource getResource(Reference reference) {
		Attachment attachment = reference.getLetter();
		if(attachment != null) {
			return new AttachmentMediaResource(reference, attachment);
		}
		return null;
	}
}
