/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
