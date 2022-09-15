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
package org.olat.course.member.wizard;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationMailValidationController extends StepFormBasicController {
	
	private static final String KEY_EMAIL = "email";
	private static final String KEY_BATCH = "batch";
	
	private TextElement emailEl;
	private SingleSelection importTypeEl;
	private SingleSelection existingInviteeEl;
	private TextAreaElement namesEl;
	
	private final InvitationContext context;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityManager securityManager;
	
	public InvitationMailValidationController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, InvitationContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.context = context;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues importTypeKeysValues = new SelectionValues();
		importTypeKeysValues.add(SelectionValues.entry(KEY_EMAIL, translate("input.email"), null, "o_icon o_icon_user", null, true));
		importTypeKeysValues.add(SelectionValues.entry(KEY_BATCH, translate("input.batch"), null, "o_icon o_icon_group", null, true));
		importTypeEl = uifactory.addCardSingleSelectHorizontal("import.type", null, formLayout, importTypeKeysValues);
		importTypeEl.setElementCssClass("o_sel_import_type o_radio_cards_vcenter");
		importTypeEl.addActionListener(FormEvent.ONCHANGE);
		importTypeEl.select("email", true);
		
		emailEl = uifactory.addTextElement("invitation.mail", 255, "", formLayout);
		emailEl.setFocus(true);
		emailEl.setMandatory(true);
		
		namesEl = uifactory.addTextAreaElement("addusers", "input.addusers", -1, 15, 40, false, false, context.getRawNames(), formLayout);
		namesEl.setExampleKey("input.addusers.example", null);
		namesEl.setMandatory(true);
		namesEl.setLineNumbersEnbaled(true);
		namesEl.setStripedBackgroundEnabled(true);
		namesEl.setFixedFontWidth(true);
		namesEl.setOriginalLineBreaks(true);
		namesEl.setVisible(false);
		
		existingInviteeEl = uifactory.addDropdownSingleselect("invitee", "existing.invitee.selection", formLayout, new String[0], new String[0]);
		existingInviteeEl.enableNoneSelection();
		existingInviteeEl.setVisible(false);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		emailEl.clearError();
		namesEl.clearError();
		existingInviteeEl.clearError();
		if(importTypeEl.isKeySelected(KEY_EMAIL)) {
			allOk &= validateEmailFormLogic();
		} else {
			allOk &= validateBatchFormLogic();
		}
		
		return allOk;
	}
	
	protected boolean validateEmailFormLogic() {
		boolean allOk = true;
		
		String mail = emailEl.getValue();
		if(StringHelper.containsNonWhitespace(mail)) {
			if (!MailHelper.isValidEmailAddress(mail)) {
				emailEl.setErrorKey("error.mail.invalid", null);
				allOk &= false;
			}
		} else {
			emailEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(existingInviteeEl.isVisible() && !existingInviteeEl.isOneSelected()) {
			existingInviteeEl.setErrorKey("error.existing.invitee.selection", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	protected boolean validateBatchFormLogic() {
		boolean allOk = true;
		
		String data = namesEl.getValue();
		if(!StringHelper.containsNonWhitespace(data)) {
			namesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			List<String[]> lines = getLines(data);
			List<Integer> errors = new ArrayList<>();
			
			boolean emailError = false;
			boolean missingData = false;
			for(int i=0; i<lines.size(); i++) {
				String[] line = lines.get(i);
				if(line.length < 3
						|| !StringHelper.containsNonWhitespace(line[1])
						|| !StringHelper.containsNonWhitespace(line[2])) {
					missingData |= true;
					errors.add(Integer.valueOf(i));
				} else if(!MailHelper.isValidEmailAddress(line[0])) {
					emailError |= true;
					errors.add(Integer.valueOf(i));
				}
			}
			
			if(!errors.isEmpty()) {
				if(missingData) {
					namesEl.setErrorKey("form.legende.mandatory", null);
				} else if(emailError) {
					namesEl.setErrorKey("error.mail.invalid", null);
				}
				namesEl.setErrors(errors);
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(importTypeEl == source) {
			boolean singleUser = importTypeEl.isKeySelected(KEY_EMAIL);
			emailEl.setVisible(singleUser);
			namesEl.setVisible(!singleUser);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		if(validateFormLogic(ureq)) {
			boolean byEmail = importTypeEl.isKeySelected(KEY_EMAIL);
			if(byEmail && !Objects.equals(context.getRawEmail(), emailEl.getValue())) {
				doImportSingleEmail(ureq);
			} else if(!byEmail && !Objects.equals(context.getRawNames(), namesEl.getValue())) {
				doImportUsersData(ureq);
			}
		}
	}
	
	private void doImportSingleEmail(UserRequest ureq) {
		String mail = emailEl.getValue();
		if(existingInviteeEl.isVisible() && existingInviteeEl.isOneSelected()) {
			String inviteeKey = existingInviteeEl.getSelectedKey();
			Identity invitee = securityManager.loadIdentityByKey(Long.valueOf(inviteeKey));
			boolean inviteeOnly = securityManager.getRoles(invitee).isInviteeOnly();
			context.setRawEmail(mail);
			context.setInvitation(mail, invitee, inviteeOnly);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(mail));
			if(shareWithIdentities.isEmpty()) {
				context.setRawEmail(mail);
				context.setInvitation(mail, null, true);
				existingInviteeEl.setVisible(false);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} else if(shareWithIdentities.size() == 1) {
				Identity invitee = shareWithIdentities.get(0);
				boolean inviteeOnly = securityManager.getRoles(invitee).isInviteeOnly();
				context.setRawEmail(mail);
				context.setInvitation(mail, invitee, inviteeOnly);
				existingInviteeEl.setVisible(false);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} else {
				SelectionValues inviteeKeyValues = new SelectionValues();
				for(Identity id:shareWithIdentities) {
					String fullname = userManager.getUserDisplayName(id);
					inviteeKeyValues.add(SelectionValues.entry(id.getKey().toString(), fullname));
				}
				existingInviteeEl.setKeysAndValues(inviteeKeyValues.keys(), inviteeKeyValues.values(), null);
				existingInviteeEl.setErrorKey("error.existing.invitee.selection", null);
				existingInviteeEl.setVisible(true);
			}
		}
	}
	
	private void doImportUsersData(UserRequest ureq) {
		context.clearInvitations();
		
		Set<String> deduplicatesEmail = new HashSet<>();
		List<String[]> lines = getLines(namesEl.getValue());
		// E-mails of the map keys is lower case
		Map<String,List<Identity>> emailToIdentities = getSharedIdentities(lines);
		for(String[] line:lines) {
			if(line.length <= 2 || !StringHelper.containsNonWhitespace(line[0])) {
				continue;
			}
			
			String mail = line[0];
			String lcMail = mail.toLowerCase();
			if(!deduplicatesEmail.contains(lcMail)) {
				deduplicatesEmail.add(lcMail);
				List<Identity> identities = emailToIdentities.get(lcMail);
				if(identities == null || identities.isEmpty()) {
					context.addInvitation(mail, line[1], line[2]);
				} else {
					for(Identity invitee:identities) {
						boolean inviteeOnly = securityManager.getRoles(invitee).isInviteeOnly();
						context.addInvitation(mail, invitee, inviteeOnly);
					}
				}
			}
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	/**
	 * @param lines The lines
	 * @return Map with the E-mail lowered cased and the identities found if any
	 */
	private Map<String,List<Identity>> getSharedIdentities(List<String[]> lines) {
		List<String> emails = new ArrayList<>(lines.size());
		for(String[] line:lines) {
			if(line.length >= 2) {
				emails.add(line[0]);
			}
		}
		
		Map<String,List<Identity>> emailToIdentities = new HashMap<>();
		List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(emails);
		for(Identity shareWithIdentity:shareWithIdentities) {
			String email = shareWithIdentity.getUser().getEmail();
			if(StringHelper.containsNonWhitespace(email)) {
				emailToIdentities
					.computeIfAbsent(email.toLowerCase(), m -> new ArrayList<>())
					.add(shareWithIdentity);
			}
			
			String institutionalEmail = shareWithIdentity.getUser().getInstitutionalEmail();
			if(StringHelper.containsNonWhitespace(institutionalEmail)) {
				emailToIdentities
					.computeIfAbsent(institutionalEmail.toLowerCase(), m -> new ArrayList<>())
					.add(shareWithIdentity);
			}
		}
		return emailToIdentities;
	}
	
	private List<String[]> getLines(String input) {
		CSVParser parser = new CSVParserBuilder()
				.withSeparator(',')
				.build();
		
		List<String[]> lines = new ArrayList<>();
		try(CSVReader reader = new CSVReaderBuilder(new StringReader(input))
					.withCSVParser(parser)
					.build()) {
			
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine.length > 0) {
					lines.add(nextLine);
				}
			}
		} catch (IOException | CsvValidationException e) {
			logError("", e);
		}
		return lines;
	}
}
