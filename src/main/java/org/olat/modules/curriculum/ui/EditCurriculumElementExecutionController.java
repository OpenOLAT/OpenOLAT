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
package org.olat.modules.curriculum.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 4, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementExecutionController extends FormBasicController {
	
	private DateChooser periodEl;
	private TextElement locationEl;
	private FormLayoutContainer participantsCont;
	private TextElement minParticipantsEl;
	private TextElement maxParticipantsEl;
	
	private CurriculumElement element;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;

	public EditCurriculumElementExecutionController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.element = element;
		this.secCallback = secCallback;
		initForm(ureq);
	}

	public CurriculumElement getCurriculumElement() {
		return element;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("curriculum.element.execution");
		
		boolean canEdit = element == null || secCallback.canEditCurriculumElement(element);
		
		periodEl = uifactory.addDateChooser("cif.dates", "cif.dates", null, formLayout);
		periodEl.setHelpText(translate("curriculum.element.period.help"));
		periodEl.setSecondDate(true);
		periodEl.setSeparator("to.separator");
		periodEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.dates));
		periodEl.setDefaultValue(periodEl);
		if (element != null) {
			periodEl.setDate(element.getBeginDate());
			periodEl.setSecondDate(element.getEndDate());
		}
		
		locationEl = uifactory.addTextElement("cif.location", "cif.location", 150, element.getLocation(), formLayout);
		locationEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.location));
		
		if (element != null && element.getParent() == null) {
			uifactory.addSpacerElement("spacer", formLayout, false);
		
			participantsCont = FormLayoutContainer.createCustomFormLayout("participants", getTranslator(), velocity_root + "/participants_edit.html");
			participantsCont.setLabel("curriculum.element.participants.num", null);
			participantsCont.setHelpText(translate("curriculum.element.participants.help"));
			participantsCont.setRootForm(mainForm);
			formLayout.add(participantsCont);
			
			String minParticipants = element != null && element.getMinParticipants() != null
					? element.getMinParticipants().toString()
					: null;
			minParticipantsEl = uifactory.addTextElement("ce.participants.min", null, 20, minParticipants, participantsCont);
			minParticipantsEl.setDisplaySize(100);
			minParticipantsEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.numParticipants));
			
			String maxParticipants = element != null && element.getMaxParticipants() != null
					? element.getMaxParticipants().toString()
					: null;
			maxParticipantsEl = uifactory.addTextElement("ce.participants.max", null, 20, maxParticipants, participantsCont);
			maxParticipantsEl.setDisplaySize(100);
			maxParticipantsEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.numParticipants));
		}
		
		if (canEdit) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= CurriculumHelper.validateTextElement(locationEl, false, 200);
		
		periodEl.clearError();
		if (periodEl.getDate() != null && periodEl.getSecondDate() != null && periodEl.getDate().after(periodEl.getSecondDate())) {
			periodEl.setErrorKey("error.first.date.after.second.date");
			allOk &= false;
		}
		
		if (participantsCont != null) {
			boolean minParticipantsOk = true;
			int minParticipants = -1;
			participantsCont.clearError();
			if (StringHelper.containsNonWhitespace(minParticipantsEl.getValue())) {
				try {
					minParticipants = Integer.parseInt(minParticipantsEl.getValue());
					if (minParticipants < 0) {
						participantsCont.setErrorKey("form.error.positive.integer");
						allOk &= false;
						minParticipantsOk &= false;
					}
				} catch (NumberFormatException e) {
					participantsCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
					minParticipantsOk &= false;
				}
			}
			if (StringHelper.containsNonWhitespace(maxParticipantsEl.getValue())) {
				try {
					int maxParticipants = Integer.parseInt(maxParticipantsEl.getValue());
					if (maxParticipants < 1) {
						participantsCont.setErrorKey("form.error.positive.integer");
						allOk &= false;
					} else if (minParticipantsOk && minParticipants >= 0 && minParticipants > maxParticipants) {
						participantsCont.setErrorKey("error.participants.min.greater.max");
						allOk &= false;
					}
				} catch (NumberFormatException e) {
					participantsCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String currentLocation = element.getLocation();
		Date currentBeginDate = element.getBeginDate();
		Date currentEndDate = element.getEndDate();
		
		element = curriculumService.getCurriculumElement(element);
		element.setBeginDate(periodEl.getDate());
		element.setEndDate(periodEl.getSecondDate());
		element.setLocation(locationEl.getValue());
		
		if (minParticipantsEl != null) {
			Long minParticipants = StringHelper.containsNonWhitespace(minParticipantsEl.getValue())
					? Long.parseLong(minParticipantsEl.getValue())
					: null;
			element.setMinParticipants(minParticipants);
			Long maxParticipants = StringHelper.containsNonWhitespace(maxParticipantsEl.getValue())
					? Long.parseLong(maxParticipantsEl.getValue())
					: null;
			element.setMaxParticipants(maxParticipants);
		}
		
		element = curriculumService.updateCurriculumElement(element);
		element = curriculumService.getCurriculumElement(element);
		
		syncCurricularCourses(currentLocation, currentBeginDate, currentEndDate);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void syncCurricularCourses(String currentLocation, Date currentBeginDate, Date currentEndDate) {
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
		for(RepositoryEntry entry:entries) {
			if(entry.getRuntimeType() != RepositoryEntryRuntimeType.curricular) continue;
			// empty string are null

			String synchedLocation = equalsString(currentLocation, entry.getLocation())
					? element.getLocation() : entry.getLocation();
			RepositoryEntryLifecycle synchedLifecycle = entry.getLifecycle();
			if(synchedLifecycle != null && synchedLifecycle.isPrivateCycle()
					&& equalsDate(currentBeginDate, synchedLifecycle.getValidFrom())
					&& equalsDate(currentEndDate, synchedLifecycle.getValidTo())) {
				synchedLifecycle.setValidFrom(element.getBeginDate());
				synchedLifecycle.setValidTo(element.getEndDate());
			}
			repositoryManager.setLocationAndLifecycle(entry, synchedLocation, synchedLifecycle);
		}
	}
	
	private boolean equalsString(String d1, String d2) {
		if(!StringHelper.containsNonWhitespace(d1) && !StringHelper.containsNonWhitespace(d2)) return true;
		return d1 != null && d2 != null && d1.equals(d2);
	}
	
	private boolean equalsDate(Date d1, Date d2) {
		if(d1 == null && d2 == null) return true;
		return d1 != null && d2 != null && DateUtils.isSameDay(d1, d2);
	}
}
