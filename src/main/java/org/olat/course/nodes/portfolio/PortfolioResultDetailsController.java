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

package org.olat.course.nodes.portfolio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.BinderController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Assessment details controller.
 * 
 * <P>
 * Initial Date:  11 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioResultDetailsController extends FormBasicController {

	private Identity assessedIdentity;
	
	private Binder templateBinder;
	private List<Binder> binders;
	
	private Map<Binder, MapElements> binderToElements = new HashMap<>();
	
	private final BreadcrumbPanel stackPanel;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PortfolioResultDetailsController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		this.stackPanel = stackPanel;
		assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		
		RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
		if(mapEntry != null) {
			if(BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
				templateBinder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
				RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				binders = portfolioService.getBinders(assessedIdentity, courseEntry, courseNode.getIdent());
			}
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(binders == null || binders.isEmpty()) {
			uifactory.addStaticTextElement("no.map", "", formLayout);
		} else if(binders != null && binders.size() > 0) {
			initBindersForm(formLayout);
		}
	}
	
	protected void initBindersForm(FormItemContainer formLayout) {
		Formatter formatter = Formatter.getInstance(getLocale());
		
		int count = 0;
		for(Binder binder:binders) {
			MapElements mapElements = new MapElements();
	
			if(binders.size() > 1 || !binder.getTemplate().equals(templateBinder)) {
				String templateTitle = binder.getTemplate().getTitle();
				uifactory.addStaticTextElement("map.template." + count, "map.template", templateTitle, formLayout);
			}
			
			String copyDate = "";
			if(binder.getCopyDate() != null) {
				copyDate = formatter.formatDateAndTime(binder.getCopyDate());
			}
			uifactory.addStaticTextElement("map.copyDate." + count, "map.copyDate", copyDate, formLayout);
			
			String returnDate = "";
			if(binder.getReturnDate() != null) {
				returnDate = formatter.formatDateAndTime(binder.getReturnDate());
			}
			uifactory.addStaticTextElement("map.returnDate." + count, "map.returnDate", returnDate, formLayout);
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons." + count, getTranslator());
			buttonsCont.setRootForm(mainForm);
			formLayout.add(buttonsCont);

			mapElements.openMapLink = uifactory.addFormLink("open.binder." + count, "open.binder", "open.map", null, buttonsCont, Link.BUTTON);
			mapElements.openMapLink.setUserObject(binder);
			
			count++;
			if(count != binders.size()) {
				uifactory.addSpacerElement("spacer-" + count, formLayout, false);
			}
			
			binderToElements.put(binder, mapElements);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link.getName().startsWith("open.binder")) {
				Binder binder = (Binder)link.getUserObject();
				doOpenBinder(ureq, binder);
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenBinder(UserRequest ureq, Binder binder) {
		if(stackPanel instanceof TooledStackedPanel) {
			binder = portfolioService.getBinderByKey(binder.getKey());
			portfolioService.updateBinderUserInformations(binder, getIdentity());
			List<AccessRights> rights = portfolioService.getAccessRights(binder, getIdentity());
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCourseCoach(binder, rights);
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			BinderController binderCtrl = new BinderController(ureq, getWindowControl(), (TooledStackedPanel)stackPanel, secCallback, binder, config);
			String displayName = StringHelper.escapeHtml(binder.getTitle());
			stackPanel.pushController(displayName, binderCtrl);
			binderCtrl.activate(ureq, null, null);
		}
	}
	
	private static class MapElements {
		private FormLink openMapLink;
	}
}