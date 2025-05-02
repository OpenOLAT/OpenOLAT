/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.ui.wizard;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.forms.EvaluationFormEmailExecutor;
import org.olat.modules.quality.ui.ParticipationListController;
import org.olat.modules.quality.ui.wizard.AddEmailContext.EmailIdentity;
import org.springframework.beans.factory.annotation.Autowired;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;


/**
 * 
 * Initial date: Apr 16, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AddEmailAddController extends StepFormBasicController {
	
	private TextAreaElement namesEl;
	
	private final AddEmailContext context;
	private final List<Organisation> searchableOrganisations;
	
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private OrganisationService organisationService;

	public AddEmailAddController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(ParticipationListController.class, getLocale(), getTranslator()));
		context = (AddEmailContext)getOrCreateFromRunContext("context", AddEmailContext::new);
		searchableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
				OrganisationRoles.valuesWithoutGuestAndInvitee());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("participation.user.email.participants");
		setFormInfo("participation.user.email.desc");
		
		namesEl = uifactory.addTextAreaElement("addusers", "participation.user.email.email", -1, 15, 40, false, false, context.getRawEmail(), formLayout);
		namesEl.setExampleKey("participation.user.email.email.example", null);
		namesEl.setMandatory(true);
		namesEl.setLineNumbersEnbaled(true);
		namesEl.setStripedBackgroundEnabled(true);
		namesEl.setFixedFontWidth(true);
		namesEl.setOriginalLineBreaks(true);
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		if (validateFormLogic(ureq)) {
			addToContext();
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
	
	private void addToContext() {
		context.getEmailToIdentity().clear();
		
		List<String[]> lines = getLines(namesEl.getValue());
		
		List<String> emailAddresses = lines.stream()
				.filter(line -> line.length > 0)
				.map(line -> line[0])
				.filter(Objects::nonNull)
				.toList();
		Map<String, Identity> emailToIdentity = securityManager.findAndCollectIdentitiesBy(
				emailAddresses, Identity.STATUS_VISIBLE_LIMIT,searchableOrganisations)
				.getUnique()
				.stream()
				.collect(Collectors.toMap(identity -> identity.getUser().getEmail().toLowerCase(), Function.identity()));
				
		
		for (String[] line:lines) {
			if (!StringHelper.containsNonWhitespace(line[0])) {
				continue;
			}
			
			String mail = line[0];
			String lcMail = mail.toLowerCase();
			
			String firstName = null;
			if (line.length > 1 && StringHelper.containsNonWhitespace(line[1])) {
				firstName = line[1];
			}
			
			String lastName = null;
			if (line.length > 2 && StringHelper.containsNonWhitespace(line[2])) {
				lastName = line[2];
			}
			
			Identity identity = emailToIdentity.get(lcMail);
			
			context.getEmailToIdentity().put(lcMail, new EmailIdentity(new EvaluationFormEmailExecutor(lcMail, firstName, lastName), identity));
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		namesEl.clearError();
		allOk &= validateBatchFormLogic();
		
		return allOk;
	}
	
	protected boolean validateBatchFormLogic() {
		boolean allOk = true;
		
		String data = namesEl.getValue();
		if(!StringHelper.containsNonWhitespace(data)) {
			namesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			List<String[]> lines = getLines(data);
			Set<String> emailaddresses = new HashSet<>(lines.size());
			List<Integer> errors = new ArrayList<>();
			
			boolean missingData = false;
			boolean emailError = false;
			boolean emailDuplicate = false;
			for(int i=0; i<lines.size(); i++) {
				String[] line = lines.get(i);
				if(line.length < 1) {
					missingData |= true;
					errors.add(Integer.valueOf(i));
				} else if(!MailHelper.isValidEmailAddress(line[0])) {
					emailError |= true;
					errors.add(Integer.valueOf(i));
				} else if (emailaddresses.contains(line[0].toLowerCase())) {
					emailDuplicate |= true;
					errors.add(Integer.valueOf(i));
				} else {
					emailaddresses.add(line[0].toLowerCase());
				}
			}
			
			if(!errors.isEmpty()) {
				if(missingData) {
					namesEl.setErrorKey("form.legende.mandatory");
				} else if(emailError) {
					namesEl.setErrorKey("error.email.invalid");
				} else if(emailDuplicate) {
					namesEl.setErrorKey("error.email.duplicate");
				}
				namesEl.setErrors(errors);
				allOk &= false;
			}
		}

		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
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
