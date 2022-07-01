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
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.MyTaxonomyLevelItemsSource;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * 
 * Initial date: 28.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MyTaxonomyLevelTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = 6968774478547770505L;
	
	private static final String MY_TAX_LEVEL = "my-tax-level-";

	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;
	
	private final QPoolSecurityCallback securityCallback;
	private final TaxonomyLevel taxonomyLevel;
	private final String displayName;

	public MyTaxonomyLevelTreeNode(TooledStackedPanel stackPanel, QPoolSecurityCallback securityCallback,
			TaxonomyLevel taxonomyLevel, String displayName) {
		super();
		this.stackPanel = stackPanel;
		this.securityCallback = securityCallback;
		this.taxonomyLevel = taxonomyLevel;
		this.displayName = displayName;
		
		setTitle(displayName);
		TaxonomyLevelType type = taxonomyLevel.getType();
		if (type != null && StringHelper.containsNonWhitespace(type.getCssClass())) {
			setIconCssClass(type.getCssClass());
		}
		
		setUserObject(taxonomyLevel);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		if (questionsCtrl == null) {
			QuestionItemsSource source = new MyTaxonomyLevelItemsSource(
					ureq.getIdentity(), ureq.getUserSession().getRoles(), ureq.getLocale(),
					taxonomyLevel, displayName);
			OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(MY_TAX_LEVEL + "_" + taxonomyLevel.getIdentifier(), taxonomyLevel.getKey());
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, null, wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, securityCallback,
					MY_TAX_LEVEL + taxonomyLevel.getKey(), false);
		} else {
			questionsCtrl.updateSource();
		}
		return questionsCtrl;
	}

}
