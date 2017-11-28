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
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
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
	private final BusinessGroup group;

	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;
	
	public BusinessGroupTreeNode(BusinessGroup group, TooledStackedPanel stackPanel) {
		this.group = group;
		this.stackPanel = stackPanel;
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);

		this.setTitle(group.getName());
		this.setIconCssClass(ICON_CSS_CLASS);
		
		// The user object is used to findNodeByPersistableUserObject
		this.setUserObject(group);
	}
	
	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		boolean shareAdmin = isShareAdmin(ureq, group);
		SharedItemsSource source = new SharedItemsSource(
				group,
				ureq.getIdentity(),
				ureq.getUserSession().getRoles(),
				shareAdmin);
		if(questionsCtrl == null) {
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, group, null,
					wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, TABLE_PREFERENCE_PREFIX + group.getKey());
		} else {
			questionsCtrl.updateSource(source);
		}
		return questionsCtrl;
	}
	
	private boolean isShareAdmin(UserRequest ureq, BusinessGroup group) {
		Identity identity = ureq.getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		return roles != null &&
				(  roles.isOLATAdmin()
				|| roles.isPoolAdmin()
				//TODO uh Muss dieses statemant innerhalb der Klammern sein? dh role haben
				|| businessGroupService.isIdentityInBusinessGroup(identity, group.getKey(), true, false, null)
				);
	}
}
