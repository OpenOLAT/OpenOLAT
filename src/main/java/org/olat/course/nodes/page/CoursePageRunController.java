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
package org.olat.course.nodes.page;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.course.nodes.PageCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.tools.CourseToolLinkTreeModel;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.modules.ceditor.Page;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.PageRunController;
import org.olat.modules.portfolio.ui.PageSettings;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursePageRunController extends BasicController {
	
	@Autowired
	private PortfolioService portfolioService;
	
	public CoursePageRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, PageCourseNode courseNode, boolean canEdit) {
		super(ureq, wControl);
		
		Long pageKey = courseNode.getPageReferenceKey();
		Page page = portfolioService.getPageByKey(pageKey);
		if(page == null) {
			String text = translate("page.not.available.message");
			MessageController ctrl = MessageUIFactory.createErrorMessage(ureq, wControl, "", text);
			putInitialPanel(ctrl.getInitialComponent());
		} else {
			VelocityContainer mainVC = createVelocityContainer("run");
	
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCoursePageCallback(canEdit);
			
			CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			CustomLinkTreeModel linkTreeModel = new CourseInternalLinkTreeModel(courseEnv.getRunStructure().getRootNode());
			CustomLinkTreeModel toolLinkTreeModel = new CourseToolLinkTreeModel(courseEnv.getCourseConfig(), courseEntry, getLocale());

			PageSettings settings = canEdit ? PageSettings.reduced(courseEntry, linkTreeModel, toolLinkTreeModel, false, false)
					: PageSettings.noHeader(courseEntry);
			settings.setSubIdent(courseNode.getIdent());
			PageRunController pageCtrl = new PageRunController(ureq, getWindowControl(), null, secCallback, page, settings, false);
			listenTo(pageCtrl);
			mainVC.put("page", pageCtrl.getInitialComponent());
			
			Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), pageCtrl, userCourseEnv, courseNode, "o_page_icon");
			putInitialPanel(ctrl.getInitialComponent());
			pageCtrl.initTools();
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
