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
package org.olat.modules.docpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTaxonomyLevelDocumentTypeController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private SingleSelection teachCanReadParentLevelsEl;
	private MultipleSelectionElement visibleEl, manageCanEl, teachCanReadEl, teachCanWriteEl,
		haveCanReadEl, targetCanReadEl, docsEnabledEl;
	
	private TaxonomyLevelType levelType;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public EditTaxonomyLevelDocumentTypeController(UserRequest ureq, WindowControl wControl, TaxonomyLevelType levelType, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "type_permissions", rootForm);
		this.levelType = levelType;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("displayName", levelType.getDisplayName());
			layoutCont.contextPut("identifier", levelType.getIdentifier());
		}

		FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		layoutCont.setRootForm(mainForm);
		formLayout.add("settings", layoutCont);

		visibleEl = uifactory.addCheckboxesHorizontal("level.visible", "level.visible", layoutCont,
				onKeys, new String[] { translate("on") });
		visibleEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.visibility));
		if(levelType != null && levelType.isVisible()) {
			visibleEl.select(onKeys[0], true);
		}

		docsEnabledEl = uifactory.addCheckboxesHorizontal("level.type.docs.enabled", "level.type.docs.enabled", layoutCont,
				onKeys, new String[] { translate("on") });
		docsEnabledEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		if(levelType != null && levelType.isDocumentsLibraryEnabled()) {
			docsEnabledEl.select(onKeys[0], true);
		}
		
		manageCanEl = uifactory.addCheckboxesHorizontal("manage.competence", "manage.competence", layoutCont,
				onKeys, new String[] { translate("manage.can.manage.on") });
		manageCanEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		if(levelType != null && levelType.isDocumentsLibraryManageCompetenceEnabled()) {
			manageCanEl.select(onKeys[0], true);
		}
	
		teachCanReadEl = uifactory.addCheckboxesHorizontal("teach.can.read", "teach.competence", layoutCont,
				onKeys, new String[] { translate("teach.can.read") });
		teachCanReadEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		if(levelType != null && levelType.isDocumentsLibraryTeachCompetenceReadEnabled()) {
			teachCanReadEl.select(onKeys[0], true);
		}
		
		String[] levelKeys = new String[10];
		String[] levelValues = new String[10];
		for(int i=10; i-->0; ) {
			levelKeys[i] = levelValues[i] = Integer.toString(i);
			
		}
		teachCanReadParentLevelsEl = uifactory.addDropdownSingleselect("teach.can.read.parent.levels", null, layoutCont,
				levelKeys, levelValues, null);
		teachCanReadParentLevelsEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		boolean levelFound = false;
		if(levelType != null && levelType.getDocumentsLibraryTeachCompetenceReadParentLevels() >= 0) {
			String selectedLevel = Integer.toString(levelType.getDocumentsLibraryTeachCompetenceReadParentLevels());
			for(String levelKey:levelKeys) {
				if(levelKey.equals(selectedLevel)) {
					teachCanReadParentLevelsEl.select(levelKey, true);
					levelFound = true;
					break;
				}
			}
		}
		if(!levelFound) {
			teachCanReadParentLevelsEl.select(levelKeys[0], true);
		}
		
		teachCanWriteEl = uifactory.addCheckboxesHorizontal("teach.can.write", null, layoutCont,
				onKeys, new String[] { translate("teach.can.write") });
		teachCanWriteEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		if(levelType != null && levelType.isDocumentsLibraryTeachCompetenceWriteEnabled()) {
			teachCanWriteEl.select(onKeys[0], true);
		}
		
		haveCanReadEl = uifactory.addCheckboxesHorizontal("have.competence", "have.competence", layoutCont,
				onKeys, new String[] { translate("have.can.read") });
		haveCanReadEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		if(levelType != null && levelType.isDocumentsLibraryHaveCompetenceReadEnabled()) {
			haveCanReadEl.select(onKeys[0], true);
		}
		
		targetCanReadEl = uifactory.addCheckboxesHorizontal("target.competence", "target.competence", layoutCont,
				onKeys, new String[] { translate("target.can.read") });
		targetCanReadEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.librarySettings));
		if(levelType != null && levelType.isDocumentsLibraryTargetCompetenceReadEnabled()) {
			targetCanReadEl.select(onKeys[0], true);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		levelType.setVisible(visibleEl.isAtLeastSelected(1));
		levelType.setDocumentsLibraryEnabled(docsEnabledEl.isAtLeastSelected(1));
		levelType.setDocumentsLibraryManageCompetenceEnabled(manageCanEl.isAtLeastSelected(1));
		levelType.setDocumentsLibraryTeachCompetenceReadEnabled(teachCanReadEl.isAtLeastSelected(1));
		String selectedParentLevels = teachCanReadParentLevelsEl.getSelectedKey();
		if(StringHelper.isLong(selectedParentLevels)) {
			int parentLevels = Integer.parseInt(selectedParentLevels);
			levelType.setDocumentsLibraryTeachCompetenceReadParentLevels(parentLevels);
		} else {
			levelType.setDocumentsLibraryTeachCompetenceReadParentLevels(-1);
		}
		levelType.setDocumentsLibraryTeachCompetenceWriteEnabled(teachCanWriteEl.isAtLeastSelected(1));
		levelType.setDocumentsLibraryHaveCompetenceReadEnabled(haveCanReadEl.isAtLeastSelected(1));
		levelType.setDocumentsLibraryTargetCompetenceReadEnabled(targetCanReadEl.isAtLeastSelected(1));
		levelType = taxonomyService.updateTaxonomyLevelType(levelType, null);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
