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
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.ui.inspection.CompensationListModel.CompensationCols;
import org.olat.course.assessment.ui.inspection.CreateInspectionContext.InspectionCompensation;
import org.olat.course.assessment.ui.inspection.elements.ExtraTimeCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompensationController extends StepFormBasicController {

	private FlexiTableElement tableEl;
	private CompensationListModel tableModel;
	
	private int counter = 0;
	private final CreateInspectionContext context;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public CompensationController(UserRequest ureq, WindowControl wControl,
			CreateInspectionContext context, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_compensation");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentInspectionOverviewController.class, getLocale(), getTranslator()));
		
		this.context = context;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.duration));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.extraTime, new ExtraTimeCellRenderer()));
		
		tableModel = new CompensationListModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "compensations", tableModel, 25, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("info.no.disadvantage.compensation");
	}
	
	private void loadModel() {
		Set<Long> selectedIdentities = new HashSet<>();
		List<CompensationRow> rows = new ArrayList<>();
		List<DisadvantageCompensation> compensations;
		if(context.getEditedInspection() != null) {
			Identity assessedIdentity = context.getEditedInspection().getIdentity();
			DisadvantageCompensation compensation = context.getEditedCompensation();
			compensations = compensation == null ? List.of() : List.of(compensation);
			selectedIdentities.add(assessedIdentity.getKey());
		} else {
			compensations = context.getParticipantsCompensations();
			List<IdentityRef> participants = context.getParticipants();
			for(IdentityRef participant:participants) {
				selectedIdentities.add(participant.getKey());
			}
		}

		Set<Identity> duplicates = new HashSet<>();
		for(DisadvantageCompensation compensation:compensations) {
			Identity identity = compensation.getIdentity();
			if(duplicates.contains(identity) || !selectedIdentities.contains(identity.getKey())) {
				continue;
			}
			final int extraTime = compensation.getExtraTime().intValue();
			CompensationRow row = new CompensationRow(identity, extraTime, userPropertyHandlers, getLocale());
			rows.add(row);

			final int duration = context.getInspectionConfiguration().getDuration();
			int val = (extraTime + duration) / 60;
			TextElement durationEl = uifactory.addTextElement("duration_" + (++counter), null, 10, Integer.toString(val), flc);
			row.setDurationEl(durationEl);
			
			if(context.getEditedInspection() != null
					&& context.getEditedInspection().getExtraTime() != null
					&& context.getEditedInspection().getIdentity().equals(identity)) {
				int totalDuration = (context.getEditedInspection().getExtraTime().intValue() + context.getInspectionConfiguration().getDuration()) / 60;
				durationEl.setValue(Integer.toString(totalDuration));
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		List<CompensationRow> rows = tableModel.getObjects();
		List<InspectionCompensation> compensations = new ArrayList<>();
		int defaultDuration = context.getInspectionConfiguration().getDuration();
		
		for(CompensationRow row:rows) {
			Long identityKey = row.getIdentityKey();
			String duration = row.getDurationEl().getValue();
			int extraTimeInSecondes = 0;
			if(StringHelper.isLong(duration)) {
				extraTimeInSecondes = (60 * Integer.parseInt(duration)) - defaultDuration;
			}
			if(extraTimeInSecondes >= 0) {
				compensations.add(new InspectionCompensation(new IdentityRefImpl(identityKey), extraTimeInSecondes));
			}
		}
		context.setInspectionCompensations(compensations);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
