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
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.DefaultItemsSource;
import org.olat.modules.qpool.ui.datasource.PoolItemsSource;

/**
 * 
 * Initial date: 13.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PoolTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = -1259214122412317978L;
	
	private static final String ICON_CSS_CLASS = "o_icon_pool_pool o_sel_qpool_pool";
	private static final String TABLE_PREFERENCE_PREFIX = "poll-";
	
	private final QPoolService qpoolService;
	private final Pool pool;
	
	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;

	public PoolTreeNode(Pool pool, TooledStackedPanel stackPanel) {
		this.pool = pool;
		this.stackPanel = stackPanel;
		this.qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		this.setTitle(pool.getName());
		this.setIconCssClass(ICON_CSS_CLASS);

		// The user object is used to findNodeByPersistableUserObject
		this.setUserObject(pool);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		DefaultItemsSource source = new PoolItemsSource(
				ureq.getIdentity(),
				ureq.getUserSession().getRoles(),
				pool);
		source.setRemoveEnabled(isPoolAdmin(ureq, pool));
		if(questionsCtrl == null) {
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, pool, null,
					wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, TABLE_PREFERENCE_PREFIX + pool.getKey());
		} else {
			questionsCtrl.updateSource(source);
		}
		return questionsCtrl;
	}
	
	private boolean isPoolAdmin(UserRequest ureq, Pool pool) {
		Identity identity = ureq.getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		return roles != null &&
				(  roles.isOLATAdmin()
				|| roles.isPoolAdmin()
				|| pool.isPublicPool()
				//TODO uh Muss dieses statemant innerhalb der Klammern sein? dh role haben
				|| qpoolService.isOwner(identity, pool)
				);
	}

}
