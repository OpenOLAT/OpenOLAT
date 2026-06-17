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
package org.olat.modules.selectus;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.letter.LetterConfiguration;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.model.mail.PositionMailTemplateRef;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MailService {
	
	
	public PositionMailTemplate createTemplate(Position position, String id, String name);
	
	public List<PositionMailTemplate> getTemplates(PositionRef position);
	
	public PositionMailTemplate getTemplate(PositionMailTemplate template);
	
	public PositionMailTemplate updateTemplate(PositionMailTemplate template);
	
	public void deleteTemplate(PositionMailTemplate template);
	
	public List<PositionMailTemplateRef> getMailTemplates(PositionRef position, Locale locale);
	
	public EmailVariables getEmailVariables(Position position, Locale locale);
	
	public EmailVariables getEmailVariables(Position position, ApplicationRef application, Locale locale);
	
	public InvitationVariables getInvitationVariables(Position position, Locale templateLocale);
	
	public SubjectAndBody rejectionTemplate(Position position, String templateName, Identity headOfCommittee, Locale templateLocale);
	
	
	public String getLetterTemplate(Locale locale);
	
	public String toLetter(LetterConfiguration configuration, Locale locale);
	
	public MailAttachment toAttachment(String rawConfiguration, ApplicationShort application, Locale locale);
	
}
