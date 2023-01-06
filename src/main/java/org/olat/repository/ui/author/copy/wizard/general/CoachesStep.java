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
package org.olat.repository.ui.author.copy.wizard.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.admin.user.UserSearchFlexiTableModel;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 21.04.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CoachesStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditCoaches()) {
			return new CoachesStep(ureq, stepCollection, steps);
		} else {
			return GroupStep.create(ureq, stepCollection, steps);
		}
	}
	
	public CoachesStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.coaches.title", null);
		
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "members.management");
		}
		setStepCollection(stepCollection);
		
		setNextStep(GroupStep.create(ureq, stepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new CoachesStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private static class CoachesStepController extends StepFormBasicController {

		private final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
		
		private CopyCourseContext context;
		
		private SingleSelection coachesCopyModeEl;
		
		private FormLink addCoachesLink;
		private FlexiTableElement tableEl;
		private UserSearchFlexiTableModel tableModel;
		
		private UserSearchFlexiController userSearchController;
		private CloseableModalController cmc;
		
		@Autowired
		private UserManager userManager;
				
		public CoachesStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);		
			loadData();
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			coachesCopyModeEl.clearError();
			
			if (!coachesCopyModeEl.isOneSelected()) {
				allOk &= false;
				coachesCopyModeEl.setErrorKey("error.select");
			}
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setCustomCoachesCopyType(CopyType.valueOf(coachesCopyModeEl.getSelectedKey()));
			context.setNewCoaches(tableModel.getObjects());
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			SelectionValue copy = new SelectionValue(CopyType.copy.name(), translate("options.copy"));
			SelectionValue replace = new SelectionValue(CopyType.ignore.name(), translate("options.ignore"));
			
			SelectionValues copyCoachesModes = new SelectionValues(copy, replace);
			
			coachesCopyModeEl = uifactory.addRadiosHorizontal("coaches", formLayout, copyCoachesModes.keys(), copyCoachesModes.values());
			coachesCopyModeEl.addActionListener(FormEvent.ONCHANGE);
			coachesCopyModeEl.setAllowNoSelection(false);
			
			addCoachesLink = uifactory.addFormLink("coaches.add.new", formLayout, Link.BUTTON_XSMALL);
			
			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colPos = 0;
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, false);
			List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<>();
			// followed by the users fields
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
				boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
				if(visible) {
					resultingPropertyHandlers.add(userPropertyHandler);
					tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos++, true, userPropertyHandler.getName()));
				}
			}
			
			// Remove column
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", translate("remove"), "remove"));
			
			Translator myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
			tableModel = new UserSearchFlexiTableModel(Collections.<Identity>emptyList(), resultingPropertyHandlers, getLocale(), tableColumnModel);
			tableEl = uifactory.addTableElement(getWindowControl(), "coaches.new", tableModel, 15, false, myTrans, formLayout);
			tableEl.setCustomizeColumns(true);
		}
		
		private void loadData() {
			if (context.getCustomCoachesCopyType() != null) {
				coachesCopyModeEl.select(context.getCustomCoachesCopyType().name(), true);
			} else {
				coachesCopyModeEl.select(CopyType.copy.name(), true);
			}
			
			if (context.getNewCoaches() != null) {
				reloadModel(context.getNewCoaches());
			}	
		}
		
		private void reloadModel(List<Identity> identities) {
			tableModel.setObjects(identities);
			tableEl.reset();
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == addCoachesLink) {
				showUserSelector(ureq);
			} else if (source ==  coachesCopyModeEl) {
				if (coachesCopyModeEl.getSelectedKey().equals(CopyType.ignore.name())) {
					tableEl.setVisible(false);
					addCoachesLink.setVisible(false);
				} else {
					tableEl.setVisible(true);
					addCoachesLink.setVisible(true);
				}
			} else if (source == tableEl) {
				if (event.getCommand().equals("remove")) {
					SelectionEvent se = (SelectionEvent) event;
					List<Identity> identities = tableModel.getObjects();
					identities.remove(se.getIndex());
					tableModel.setObjects(identities);
					tableEl.reset();
				}
			}
			
			super.formInnerEvent(ureq, source, event);
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if (source == userSearchController) {
				saveSelectedUsers(event);
			} 
			
			cleanUp();
		}
		
		private void showUserSelector(UserRequest ureq) {
			userSearchController = new UserSearchFlexiController(ureq, getWindowControl(), (GroupRoles)null, true);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchController.getInitialComponent(), true);
			
			listenTo(userSearchController);
			listenTo(cmc);
			
			cmc.activate();
		}
		
		private void saveSelectedUsers(Event event) {
			cmc.deactivate();
			
			List<Identity> newCoaches = new ArrayList<>();
			newCoaches.addAll(tableModel.getObjects());
			
			if (event instanceof SingleIdentityChosenEvent) {
				Identity newCoach = ((SingleIdentityChosenEvent) event).getChosenIdentity();
				
				if (!newCoaches.contains(newCoach)) {
					newCoaches.add(newCoach);
				}
				
				reloadModel(newCoaches);
			} else if (event instanceof MultiIdentityChosenEvent) {
				for (Identity newCoach : userSearchController.getSelectedIdentities()) {
					if (!newCoaches.contains(newCoach)) {
						newCoaches.add(newCoach);
					}
				}
				
				reloadModel(newCoaches);
			}
		}
		
		private void cleanUp() {	
			removeAsListenerAndDispose(userSearchController);
			removeAsListenerAndDispose(cmc);
			userSearchController = null;
			cmc = null;
		}
 	}
}
