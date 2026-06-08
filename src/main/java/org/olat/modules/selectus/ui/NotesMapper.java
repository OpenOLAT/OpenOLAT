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

import static org.olat.modules.selectus.ui.RecruitingHelper.normalizeFilename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.resources.FOPMediaResource;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 mar. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotesMapper implements Mapper  {
	
	private static final Logger log = Tracing.createLoggerFor(NotesMapper.class);
	
	private final Locale locale;
	private Position position;
	private final Identity identity;
	private final Translator translator;
	
	private RecruitingService erFrontendManager;

	public NotesMapper(Identity identity, Position position, Translator translator, Locale locale) {
		this.locale = locale;
		this.identity = identity;
		this.position = position;
		this.translator = translator;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		int positionKeyIndex = relPath.indexOf('/');
		if(positionKeyIndex < 0) return new NotFoundMediaResource();
		String positionKeyStr = relPath.substring(0, positionKeyIndex);
		Long positionKey;
		try {
			positionKey = Long.valueOf(positionKeyStr);
		} catch (NumberFormatException e) {
			return new NotFoundMediaResource();
		}
		if(position == null || !position.getKey().equals(positionKey)) {
			return new NotFoundMediaResource();
		}
		
		if(erFrontendManager == null) {
			erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		}
		
		if(relPath.endsWith("_notes.pdf")) {
			// Notes (PDF)
			try {
				position = erFrontendManager.getPosition(position.getKey());
				List<ApplicationLight> applicationRows = erFrontendManager.getApplications(position);
				List<Notes> notes = erFrontendManager.getNotes(position, identity);
				List<ApplicationLight> applications = filterApplications(applicationRows, notes);
				List<UserRating> ratings = erFrontendManager.getRatings(position, Collections.singletonList(identity));
				NotesPDFDataModel model = new NotesPDFDataModel(identity, applications, notes, ratings, translator);
				FOPMediaResource resource =  new FOPTableExport().exportNotes(identity, position, model, locale);
				
				String derivedName = RecruitingHelper.getPositionDerivedFilename(position, locale);
				resource.setFilename(normalizeFilename(derivedName) + "_notes.pdf");
				return resource;
			} catch (Exception e) {
				log.error("Cannot export table in PDF (FOP)", e);
			}
		}
		return null;
	}
	
	private List<ApplicationLight> filterApplications(List<ApplicationLight> applicationRows, List<Notes> notes) {
		Set<Long> appKey = new HashSet<>((notes.size() * 2) + 1);
		for(Notes note:notes) {
			if(StringHelper.containsNonWhitespace(note.getContent())) {
				appKey.add(note.getApplicationKey());
			}
		}
		
		List<ApplicationLight> app = new ArrayList<>(appKey.size());
		for(Iterator<ApplicationLight> it=applicationRows.iterator(); it.hasNext(); ) {
			ApplicationLight application = it.next();
			if(appKey.contains(application.getKey())) {
				app.add(application);
			}
		}
		return app;
	}
}