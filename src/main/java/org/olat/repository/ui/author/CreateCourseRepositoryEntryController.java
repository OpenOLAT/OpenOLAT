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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
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

	private SingleSelection nodeAccessEl;
	
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private CourseModule courseModule;

	public CreateCourseRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryHandler handler,
			boolean wizardsEnabled) {
		super(ureq, wControl, handler, wizardsEnabled);
	}

	@Override
	protected void initAdditionalFormElements(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues nodeAccessKV = new SelectionValues();
		String helpText = "";
		for (NodeAccessProviderIdentifier identifier : nodeAccessService.getNodeAccessProviderIdentifer()) {
			String title = identifier.getDisplayName(getLocale());
			nodeAccessKV.add(SelectionValues.entry(identifier.getType(), title));
			helpText += "<strong>" + title + "</strong><br />" + identifier.getToolTipHelpText(getLocale()) + "<br /><br />";
		}
		nodeAccessEl = uifactory.addRadiosVertical("cif.node.access", "cif.node.access", formLayout,
				nodeAccessKV.keys(), nodeAccessKV.values());
		
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!StringHelper.containsNonWhitespace(defaultCourseType) || !nodeAccessKV.containsKey(defaultCourseType)) {
			defaultCourseType = CourseConfig.NODE_ACCESS_TYPE_DEFAULT;
		}
		nodeAccessEl.select(defaultCourseType, true);
		nodeAccessEl.addActionListener(FormEvent.ONCHANGE);
		
		nodeAccessEl.setHelpText(helpText);
		nodeAccessEl.setHelpUrlForManualPage("manual_user/course_create/Learning_path_course/");
	}

	@Override
	protected void afterEntryCreated() {
		String type = nodeAccessEl.getSelectedKey();
		CourseFactory.initNodeAccessType(getAddedEntry(), NodeAccessType.of(type));
		addedEntry = repositoryManager.setTechnicalType(addedEntry, type);
	}
	
}
