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
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.CollectionOfItemsSource;

/**
 * 
 * Initial date: 13.10.2017<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CollectionTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = -684153725340722364L;

	private static final String ICON_CSS_CLASS = "o_icon_pool_collection o_sel_qpool_collection";
	private static final String TABLE_PREFERENCE_PREFIX = "coll-";

	private final QPoolSecurityCallback securityCallback;
	private final QuestionItemCollection questionItemCollection;

	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;

	public CollectionTreeNode(TooledStackedPanel stackPanel, QPoolSecurityCallback securityCallback, QuestionItemCollection questionItemCollection) {
		this.questionItemCollection = questionItemCollection;
		this.securityCallback = securityCallback;
		this.stackPanel = stackPanel;

		this.setTitle(questionItemCollection.getName());
		this.setIconCssClass(ICON_CSS_CLASS);
		
		// The user object is used to findNodeByPersistableUserObject
		this.setUserObject(questionItemCollection);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		QuestionItemsSource source = new CollectionOfItemsSource(
				questionItemCollection,
				ureq.getIdentity(),
				ureq.getUserSession().getRoles(), ureq.getLocale());
		if (questionsCtrl == null) {
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, questionItemCollection, null, wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, securityCallback,
					TABLE_PREFERENCE_PREFIX + questionItemCollection.getKey(), false);
			questionsCtrl.setQuestionItemCollection(questionItemCollection);
		} else {
			questionsCtrl.updateSource(source);
		}
		return questionsCtrl;
	}

}
