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
package org.olat.modules.grading.ui.confirmation;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.ui.GradingRepositoryOverviewController;
import org.olat.modules.grading.ui.component.GraderMailTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeactivationGraderController extends FormBasicController {
	
	private final boolean remove;
	private final Identity grader;
	private List<Identity> replacements;
	private final RepositoryEntry referenceEntry;
	
	private SingleSelection replacementEl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	
	/**
	 * Confirm and deactivate the grader for all reference / test entries.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param grader The identity of the grader
	 * @param remove Remove (step more than deactivated)
	 */
	public ConfirmDeactivationGraderController(UserRequest ureq, WindowControl wControl, Identity grader, boolean remove) {
		this(ureq, wControl, null, grader, remove);
	}
	
	/**
	 * Confirm and deactivate the grader for the specified reference / test entry.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param referenceEntry The reference / test entry
	 * @param grader The identity if the grader
	 * @param remove Remove (step more than deactivated)
	 */
	public ConfirmDeactivationGraderController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry referenceEntry, Identity grader, boolean remove) {
		super(ureq, wControl, Util.createPackageTranslator(GradingRepositoryOverviewController.class, ureq.getLocale()));
		this.remove = remove;
		this.grader = grader;
		this.referenceEntry = referenceEntry;
		initForm(ureq);
	}
	
	public Identity getGrader() {
		return grader;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String graderFullname = StringHelper.escapeHtml(userManager.getUserDisplayName(grader));
		String confirmI18nKeyPrefix = remove ? "confirm.remove.grader" : "confirm.deactivate.grader";
		if(referenceEntry == null) {
			replacements = gradingService.getGraders(getIdentity());
			setFormWarning(confirmI18nKeyPrefix.concat(".all"), new String[] {  graderFullname });
		} else {
			List<GraderToIdentity> replacementsRelations = gradingService.getGraders(referenceEntry);
			replacements = replacementsRelations.stream()
					.filter(relation -> relation.getGraderStatus() == GraderStatus.activated)
					.map(GraderToIdentity::getIdentity)
					.distinct()
					.collect(Collectors.toList());
			
			String entryTitle = StringHelper.escapeHtml(referenceEntry.getDisplayname());
			if(StringHelper.containsNonWhitespace(referenceEntry.getExternalRef())) {
				String entryExternalRef = StringHelper.escapeHtml(referenceEntry.getExternalRef());
				setFormWarning(confirmI18nKeyPrefix.concat(".entry.ref"), new String[] {  graderFullname, entryTitle, entryExternalRef });
			} else {
				setFormWarning(confirmI18nKeyPrefix.concat(".entry"), new String[] {  graderFullname, entryTitle });
			}
		}
		replacements.remove(grader);
		
		SelectionValues replacementKeyValues = new SelectionValues();
		replacementKeyValues.add(SelectionValues.entry("-", translate("confirm.deactivate.replacement.none")));
		for(Identity identity:replacements) {
			replacementKeyValues.add(SelectionValues.entry(identity.getKey().toString(), userManager.getUserDisplayName(identity)));
		}
		replacementEl = uifactory.addDropdownSingleselect("replacements", "confirm.deactivate.replacement", formLayout,
				replacementKeyValues.keys(), replacementKeyValues.values());
		replacementEl.setVisible(replacementKeyValues.size() > 1);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		String label = remove ? "tool.remove" : "tool.deactivate";
		uifactory.addFormSubmitButton(label, buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Identity replacement = null;
		if(replacementEl.isVisible() && replacementEl.isOneSelected() && !"-".equals(replacementEl.getSelectedKey())) {
			String identityKey = replacementEl.getSelectedKey();
			for(Identity identity:replacements) {
				if(identityKey.equals(identity.getKey().toString())) {
					replacement = identity;
					break;
				}
			}	
		}
		
		MailerResult result = new MailerResult();
		GraderMailTemplate reassignmentTemplate = null;
		if(replacement != null) {
			RepositoryEntryGradingConfiguration configuration = null;
			if(referenceEntry != null)  {
				configuration = gradingService.getOrCreateConfiguration(referenceEntry);
			}
			reassignmentTemplate = GraderMailTemplate.notification(getTranslator(), null, null, referenceEntry, configuration);
		}
		
		if(remove) {
			if(referenceEntry == null) {
				gradingService.removeGrader(grader, replacement, reassignmentTemplate, result);
			} else {
				gradingService.removeGrader(referenceEntry, grader, replacement, reassignmentTemplate, result);
			}
		} else if(referenceEntry == null) {
			gradingService.deactivateGrader(grader, replacement, reassignmentTemplate, result);
		} else {
			gradingService.deactivateGrader(referenceEntry, grader, replacement, reassignmentTemplate, result);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
