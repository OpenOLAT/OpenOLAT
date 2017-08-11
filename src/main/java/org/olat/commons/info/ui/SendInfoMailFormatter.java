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


package org.olat.commons.info.ui;

import java.text.DateFormat;
import java.util.List;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.manager.MailFormatter;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;

/**
 * 
 * Description:<br>
 * Format the email send after the creation of an info message in a course
 * 
 * <P>
 * Initial Date:  24 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendInfoMailFormatter implements MailFormatter {
	
	private final String title;
	private final String businessPath;
	private final Translator translator;
	
	public SendInfoMailFormatter(String title, String businessPath, Translator translator) {
		this.title = title;
		this.translator = translator;
		this.businessPath = businessPath;
	}
	
	@Override
	public String getBusinessPath() {
		return businessPath;
	}

	@Override
	public String getSubject(InfoMessage msg) {
		return msg.getTitle();
	}

	@Override
	public String getBody(InfoMessage msg) {
		BusinessControlFactory bCF = BusinessControlFactory.getInstance(); 
		List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
		String busPath = BusinessControlFactory.getInstance().getBusinessPathAsURIFromCEList(ceList); 

		String author =	msg.getAuthor().getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + msg.getAuthor().getUser().getProperty(UserConstants.LASTNAME, null);
		String date = DateFormat.getDateInstance(DateFormat.MEDIUM, translator.getLocale()).format(msg.getCreationDate());
		String link =	Settings.getServerContextPathURI() + "/url/" + busPath;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='background: #FAFAFA; border: 1px solid #eee; border-radius: 5px; padding: 0 0.5em 0.5em 0.5em; margin: 1em 0 1em 0;' class='o_m_h'>");		
		sb.append("<h3>").append(translator.translate("mail.body.title", new String[]{title})).append("</h3>");
		sb.append("<div style='font-size: 90%; color: #888' class='o_m_a'>").append(translator.translate("mail.body.from", new String[]{author, date})).append("</div>");
		sb.append("</div>");
		
		sb.append("<div style='background: #FAFAFA; padding: 5px 5px; margin: 10px 0;' class='o_m_c'>");		
		sb.append(msg.getMessage());
		sb.append("<div style='margin: 2em 0 1em 0;' class='o_m_m'>").append("<a href='").append(link).append("'>");		
		sb.append(translator.translate("mail.body.more"));		
		sb.append(" &raquo;</a></div>");
		
		sb.append("</div>");
		
		return sb.toString();
	}
}
