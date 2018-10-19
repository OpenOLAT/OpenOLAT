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
package org.olat.modules.qpool.ui.tree;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.SharedItemsSource;

/**
 * 
 * Initial date: 13.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = -2051659076013177941L;
	
	private static final String ICON_CSS_CLASS = "o_icon_pool_share o_sel_qpool_share";
	private static final String TABLE_PREFERENCE_PREFIX = "share-";  
	
	private final BusinessGroupService businessGroupService;
	private final QPoolSecurityCallback securityCallback;
	private final BusinessGroup group;

	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;
	
	public BusinessGroupTreeNode(TooledStackedPanel stackPanel, QPoolSecurityCallback securityCallback, BusinessGroup group) {
		this.group = group;
		this.securityCallback = securityCallback;
		this.stackPanel = stackPanel;
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);

		this.setTitle(group.getName());
		this.setIconCssClass(ICON_CSS_CLASS);
		
		// The user object is used to findNodeByPersistableUserObject
		this.setUserObject(group);
	}
	
	public BusinessGroup getBusinessGroup() {
		return group;
	}
	
	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		if(questionsCtrl == null) {
			boolean shareAdmin = isShareAdmin(ureq, group);
			QuestionItemsSource source = new SharedItemsSource(
					group,
					ureq.getIdentity(), ureq.getUserSession().getRoles(), ureq.getLocale(),
					shareAdmin);
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, group, null,
					wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, securityCallback,
					TABLE_PREFERENCE_PREFIX + group.getKey(), false);
		} else {
			questionsCtrl.updateSource();
		}
		return questionsCtrl;
	}
	
	private boolean isShareAdmin(UserRequest ureq, BusinessGroup businessGroup) {
		Identity identity = ureq.getIdentity();
		return businessGroupService.isIdentityInBusinessGroup(identity, businessGroup.getKey(), true, false, null);
	}
}
