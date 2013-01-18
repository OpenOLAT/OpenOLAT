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
package org.olat.catalog.ui;

import org.olat.catalog.CatalogModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogAdminController extends FormBasicController {

	private MultipleSelectionElement myCoursesEl;
	private MultipleSelectionElement siteEl;
	private MultipleSelectionElement repoEl;
	
	private final CatalogModule catalogModule;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public CatalogAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin");
		
		catalogModule = CoreSpringFactory.getImpl(CatalogModule.class);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//server informations
		FormLayoutContainer serverCont = FormLayoutContainer.createDefaultFormLayout("functions", getTranslator());
		formLayout.add(serverCont);
		formLayout.add("functions", serverCont);

		myCoursesEl = uifactory.addCheckboxesHorizontal("my.courses", "catalog.courses", serverCont, new String[]{"xx"}, new String[]{""}, null);
		myCoursesEl.select("xx", catalogModule.isMyCoursesEnabled());
		myCoursesEl.addActionListener(this, FormEvent.ONCLICK);
		
		siteEl = uifactory.addCheckboxesHorizontal("catalog.new", "catalog.new", serverCont, new String[]{"xx"}, new String[]{""}, null);
		siteEl.select("xx", catalogModule.isCatalogSiteEnabled());
		siteEl.addActionListener(this, FormEvent.ONCLICK);
		
		repoEl = uifactory.addCheckboxesHorizontal("catalog.classic", "catalog.classic", serverCont, new String[]{"xx"}, new String[]{""}, null);
		repoEl.select("xx", catalogModule.isCatalogRepoEnabled());
		repoEl.addActionListener(this, FormEvent.ONCLICK);
	}
	
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == myCoursesEl) {
			catalogModule.setMyCoursesEnabled(myCoursesEl.isSelected(0));
		} else if(source == siteEl) {
			catalogModule.setCatalogSiteEnabled(siteEl.isSelected(0));
		} else if(source == repoEl) {
			catalogModule.setCatalogRepoEnabled(repoEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}