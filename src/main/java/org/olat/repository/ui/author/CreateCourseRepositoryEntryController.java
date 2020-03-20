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
package org.olat.repository.ui.author;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.repository.handlers.RepositoryHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateCourseRepositoryEntryController extends CreateRepositoryEntryController {

	private static final Logger log = Tracing.createLoggerFor(CreateCourseRepositoryEntryController.class);
	
	private SingleSelection nodeAccessEl;
	
	@Autowired
	private NodeAccessService nodeAccessService;

	public CreateCourseRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryHandler handler) {
		super(ureq, wControl, handler);
		updateUI();
	}

	@Override
	protected void initAdditionalFormElements(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		KeyValues nodeAccessKV = new KeyValues();
		for (NodeAccessProviderIdentifier identifier : nodeAccessService.getNodeAccessProviderIdentifer()) {
			nodeAccessKV.add(KeyValues.entry(identifier.getType(), identifier.getDisplayName(getLocale())));
		}
		nodeAccessEl = uifactory.addDropdownSingleselect("cif.node.access", "cif.node.access", formLayout,
				nodeAccessKV.keys(), nodeAccessKV.values());
		nodeAccessEl.select(CourseConfig.NODE_ACCESS_TYPE_DEFAULT, true);
		nodeAccessEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void updateUI() {
		boolean hasWizard = nodeAccessEl.isOneSelected() && ConditionNodeAccessProvider.TYPE.equals(nodeAccessEl.getSelectedKey());
		wizardButton.setVisible(hasWizard);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == nodeAccessEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void afterEntryCreated() {
		String type = nodeAccessEl.getSelectedKey();
		CourseFactory.initNodeAccessType(getAddedEntry(), NodeAccessType.of(type));
	}
	
}
