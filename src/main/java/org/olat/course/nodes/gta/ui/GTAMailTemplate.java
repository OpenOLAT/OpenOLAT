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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 26.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAMailTemplate extends MailTemplate {
	
	private static final String NUMBER_OF_FILES = "numberOfFiles";
	private static final String FILENAME = "filename";
	private static final String DATE = "date";
	private static final String TIME = "time";
	private static final String COURSE_URL = "courseURL";
	private static final String COURSE_NAME = "courseName";
	private static final String COURSE_ELEMENT_NAME = "courseElementName";
	private static final String COURSE_ELEMENT_TITLE = "courseElementTitle";
	private static final String COURSE_ELEMENT_SHORT_TITLE = "courseElementShortTitle";

	private static final Set<String> VARIABLE_NAMES = Set.of(NUMBER_OF_FILES, FILENAME, DATE, TIME,
			COURSE_URL, COURSE_NAME, COURSE_ELEMENT_NAME, COURSE_ELEMENT_TITLE, COURSE_ELEMENT_SHORT_TITLE);
	
	private final File[] files;
	private final Identity identity;
	private final Translator translator;
	private final CourseNode courseNode;
	private final RepositoryEntry courseEntry;
	
	public GTAMailTemplate(String subject, String body, File[] files,
			RepositoryEntry courseEntry, CourseNode courseNode,
			Identity identity, Translator translator) {
		super(subject, body, null);
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.translator = translator;
		this.identity = identity;
		this.files = files;
	}
	
	public static final Collection<String> variableNames() {
		return VARIABLE_NAMES;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
		Locale locale = translator.getLocale();
		//compatibility with the old TA
		fillContextWithStandardIdentityValues(context, identity, translator.getLocale());
		putVariablesInMailContext(context, NUMBER_OF_FILES, files == null ? "0" : Integer.toString(files.length));

		if(files != null && files.length > 0) {
			StringBuilder sb = new StringBuilder();
			for(File file:files) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(file.getName());
			}
			context.put(FILENAME, sb.toString());
		} else {
			context.put(FILENAME, translator.translate("submission.nofile"));
		}
		
		Date now = new Date();
		Formatter f = Formatter.getInstance(locale);
		context.put(DATE, f.formatDate(now));
		context.put(TIME, f.formatTime(now));
		
		if(courseEntry != null) {
			putVariablesInMailContext(context, COURSE_NAME, courseEntry.getDisplayname());
			String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey();
			if(courseNode != null) {
				url += "/CourseNode/" + courseNode.getIdent();
			}
			String link = "<a href='" + url + "'>" + url + "</a>";
			putVariablesInMailContext(context, COURSE_URL, link);
			putVariablesInMailContext(context, "courseUrl", link);
		}
		
		if(courseNode != null) {
			String title = courseNode.getLongTitle();
			if(!StringHelper.containsNonWhitespace(title)) {
				title = courseNode.getShortTitle();
			}
			putVariablesInMailContext(context, COURSE_ELEMENT_NAME, title);
			putVariablesInMailContext(context, COURSE_ELEMENT_TITLE, title);
			putVariablesInMailContext(context, COURSE_ELEMENT_SHORT_TITLE, courseNode.getShortTitle());
		}
	}
}
