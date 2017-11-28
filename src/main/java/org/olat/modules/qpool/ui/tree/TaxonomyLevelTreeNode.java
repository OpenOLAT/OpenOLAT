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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.TaxonomyLeveltemsSource;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 28.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = 6968774478547770505L;
	
	private static final String ORES_TYPE = "Subject";
	private static final String ICON_CSS_CLASS = "o_icon_pool_taxonomy o_sel_qpool_taxonomy";
	private static final String TABLE_PREFERENCE_PREFIX = "taxlevel-";
	
	private final TaxonomyLevel taxonomyLevel;
	private final QuestionStatus questionStatus;
	private final Identity author;
	
	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;

	public TaxonomyLevelTreeNode(TooledStackedPanel stackPanel, TaxonomyLevel taxonomyLevel, QuestionStatus questionStatus,
			Identity author) {
		super();
		this.stackPanel = stackPanel;
		this.taxonomyLevel = taxonomyLevel;
		this.questionStatus = questionStatus;
		this.author = author;
		
		this.setTitle(taxonomyLevel.getDisplayName());
		this.setIconCssClass(ICON_CSS_CLASS);
		
		this.setUserObject(taxonomyLevel);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		TaxonomyLeveltemsSource source = new TaxonomyLeveltemsSource(
				ureq.getIdentity(),
				ureq.getUserSession().getRoles(),
				taxonomyLevel,
				questionStatus,
				author);
		if (questionsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_TYPE + "_" + questionStatus, taxonomyLevel.getKey());
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, null, wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, TABLE_PREFERENCE_PREFIX + questionStatus + taxonomyLevel.getKey());
		} else {
			questionsCtrl.updateSource(source);
		}
		return questionsCtrl;
	}

}
