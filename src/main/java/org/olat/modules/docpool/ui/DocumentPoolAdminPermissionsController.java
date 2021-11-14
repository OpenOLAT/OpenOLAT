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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolAdminPermissionsController extends FormBasicController {
	
	private List<EditTaxonomyLevelDocumentTypeController> typeCtrlList = new ArrayList<>();
	
	private int counter = 0;
	private List<TaxonomyLevelType> levelTypes;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public DocumentPoolAdminPermissionsController(UserRequest ureq, WindowControl wControl, TaxonomyRef taxonomy) {
		super(ureq, wControl, "document_pool_permissions");
		
		levelTypes = taxonomyService.getTaxonomyLevelTypes(taxonomy);
		if(levelTypes.size() > 1) {
			try {
				Collections.sort(levelTypes, new TaxonomyLevelComparator());
			} catch (Exception e) {
				logError("Cannot sort taxonomy level types", e);
			}
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> names = new ArrayList<>(levelTypes.size());
		for(TaxonomyLevelType levelType:levelTypes) {
			EditTaxonomyLevelDocumentTypeController levelTypeCtrl
				= new EditTaxonomyLevelDocumentTypeController(ureq, getWindowControl(), levelType, mainForm);
			listenTo(levelTypeCtrl);
			
			String name = "perm_" + (++counter);
			formLayout.add(name, levelTypeCtrl.getInitialFormItem());
			typeCtrlList.add(levelTypeCtrl);
			names.add(name);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("permissions", names);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(EditTaxonomyLevelDocumentTypeController typeCtrl:typeCtrlList) {
			allOk &= typeCtrl.validateFormLogic(ureq);
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(EditTaxonomyLevelDocumentTypeController typeCtrl:typeCtrlList) {
			typeCtrl.formOK(ureq);
		}
	}
	
	private class TaxonomyLevelComparator implements Comparator<TaxonomyLevelType> {

		@Override
		public int compare(TaxonomyLevelType t1, TaxonomyLevelType t2) {
			Set<TaxonomyLevelTypeToType> s1 = t1.getAllowedTaxonomyLevelSubTypes();
			Set<TaxonomyLevelTypeToType> s2 = t2.getAllowedTaxonomyLevelSubTypes();

			boolean o1_s2 = contains(t1, s2);
			boolean o2_s1 = contains(t2, s1);
			
			int c;
			if(o1_s2 && o2_s1) {
				c = 0; 
			} else if(o1_s2) {
				c = 1; 
			} else if(o2_s1) {
				c = -1;
			} else {
				c = 0;
			}
			return c;
		}
		
		private boolean contains(TaxonomyLevelType type, Set<TaxonomyLevelTypeToType> otherSet) {
			for(TaxonomyLevelTypeToType typeToType:otherSet) {
				if(typeToType.getAllowedSubTaxonomyLevelType().equals(type)) {
					return true;
				}
			}
			return false;
		}
	}
}