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
import java.util.Date;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailTemplate;

/**
 * 
 * Initial date: 26.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAMailTemplate extends MailTemplate {
	
	private final Identity identity;
	private final File[] files;
	private final Translator translator;
	
	public GTAMailTemplate(String subject, String body, File[] files, Identity identity, Translator translator) {
		super(subject, body, null);
		this.translator = translator;
		this.identity = identity;
		this.files = files;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
		Locale locale = translator.getLocale();
		//compatibility with the old TA
		fillContextWithStandardIdentityValues(context, identity, translator.getLocale());
		context.put("numberOfFiles", files == null ? "0" : Integer.toString(files.length));

		if(files != null && files.length > 0) {
			StringBuilder sb = new StringBuilder();
			for(File file:files) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(file.getName());
			}
			context.put("filename", sb.toString());
		} else {
			context.put("filename", translator.translate("submission.nofile"));
		}
		
		Date now = new Date();
		Formatter f = Formatter.getInstance(locale);
		context.put("date", f.formatDate(now));
		context.put("time", f.formatTime(now));
	}
}
