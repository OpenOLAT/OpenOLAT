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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.MyItemsSource;

/**
 * 
 * Initial date: 19.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MyQuestionsTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = 4697595246970837001L;
	
	public static final OLATResourceable ORES = OresHelper.createOLATResourceableType("My");
	private static final String ICON_CSS_CLASS = "o_icon_pool_my_items o_sel_qpool_my_items";
	private static final String ITEM_SOURCE_NAME = "My";
	private static final String TABLE_PREFERENCE_PREFIX = "my";
	private static final String USER_OBJECT = "My";
	
	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;
	
	private final QPoolSecurityCallback securityCallback;
	
	public MyQuestionsTreeNode(TooledStackedPanel stackPanel, QPoolSecurityCallback securityCallback, String title) {
		super();
		this.stackPanel = stackPanel;
		this.securityCallback = securityCallback;
		
		setTitle(title);
		setIconCssClass(ICON_CSS_CLASS);
		
		// The user object is used to findNodeByPersistableUserObject
		setUserObject(USER_OBJECT);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		if(questionsCtrl == null) {
			QuestionItemsSource source = new MyItemsSource(
					ureq.getIdentity(), ureq.getUserSession().getRoles(), ureq.getLocale(),
					ITEM_SOURCE_NAME);
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ORES, null,
					wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, securityCallback,
					TABLE_PREFERENCE_PREFIX, false);
		} else {
			questionsCtrl.updateSource();
		}
		return questionsCtrl;
	}

}
