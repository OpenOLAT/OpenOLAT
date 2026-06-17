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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.pdf.PDFDataProvider;
import org.olat.modules.selectus.ui.document.PDFApplicationCombinedHelper;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PDFExpertOpinionsCachedCombinedResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(PDFExpertOpinionsCachedCombinedResource.class);
	
	private final Position position;
	private final List<Reference> references;
	private final RecruitingPositionSecurityCallback secCallback;
	private final String fileName;
	private final String fileDescription;
	private final Translator translator;
	
	public PDFExpertOpinionsCachedCombinedResource(Position position, List<Reference> references,
			RecruitingPositionSecurityCallback secCallback, String fileName, String fileDescription, Translator translator) {
		this.position = position;
		this.references = references;
		this.secCallback = secCallback;
		this.fileName = fileName;
		this.fileDescription = fileDescription;
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
	public void prepare(HttpServletResponse response) {
		response.setHeader("Content-Disposition","attachment; filename=\"" + StringHelper.urlEncodeUTF8(fileName) + "\"");
		response.setHeader("Content-Description",StringHelper.urlEncodeUTF8(fileDescription));

		try(OutputStream httpOut = response.getOutputStream()) {
			RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
			PositionDataProvider provider = new PositionDataProvider();
			erFrontendManager.streamExpertOpinions(position, provider, httpOut);
		} catch (IOException e) {
			log.error("Cannot generate batch combined files for: " + position, e);
		}
	}

	@Override
	public void release() {
		//
	}
	
	private class PositionDataProvider implements PDFDataProvider {
		
		@Autowired
		private DB dbInstance;
		@Autowired
		private RecruitingService recruitingService;
		
		public PositionDataProvider() {
			CoreSpringFactory.autowireObject(this);
		}

		@Override
		public Long sizeNeeded() {
			RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
			Long size = erFrontendManager.getEstimatedSizeOfAttachment(position);
			dbInstance.commitAndCloseSession();
			if(size == null) {
				return Long.valueOf(references.size() * 1l * 1024l * 1024l);
			}
			return size;
		}

		@Override
		public Date getLastModified() {
			RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
			Date lastModified = erFrontendManager.getLastApplicationModification(position);
			dbInstance.commitAndCloseSession();
			return lastModified;
		}

		@Override
		public void createBigData(ZipOutputStream zipOut) throws IOException {
			Set<Application> duplicates = new HashSet<>();
			
			for(Reference reference:references) {
				Application app = reference.getApplication();
				if(duplicates.contains(app)) {
					continue;
				} else {
					duplicates.add(app);
				}
				app = recruitingService.getApplicationWithAttributes(app);
				PDFApplicationCombinedHelper helper = new PDFApplicationCombinedHelper(app, position, secCallback, translator);
				String filename = helper.getExpertOpinionsFilename(reference);
				zipOut.putNextEntry(new ZipEntry(filename));
				helper.combineExpertOpinionsStreamed(zipOut);
				zipOut.closeEntry();
				
				dbInstance.commitAndCloseSession();
			}
		}
	}
}
