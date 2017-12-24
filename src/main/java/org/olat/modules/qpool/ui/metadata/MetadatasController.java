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
package org.olat.modules.qpool.ui.metadata;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadatasController extends BasicController {

	private static final String GUIPREFS_SEPARATOR = "::";
	private static final String GUIPREF_KEY_OPEN_PANEL = "open.panel";

	private final VelocityContainer mainVC;
	private GeneralMetadataEditController generalEditCtrl;
	private QuestionMetadataEditController questionEditCtrl;
	private RightsMetadataEditController rightsEditCtrl;
	private TechnicalMetadataEditController technicalEditCtrl;
	private PoolsMetadataController poolsCtrl;
	private SharesMetadataController sharesController;
	
	private QuestionItem item;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	
	public MetadatasController(UserRequest ureq, WindowControl wControl, QuestionItem item,
			QuestionItemSecurityCallback securityCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;

		mainVC = createVelocityContainer("item_metadatas");
		generalEditCtrl = new GeneralMetadataEditController(ureq, wControl, item, securityCallback);
		listenTo(generalEditCtrl);
		mainVC.put("details_general", generalEditCtrl.getInitialComponent());

		questionEditCtrl = new QuestionMetadataEditController(ureq, wControl, item, securityCallback);
		listenTo(questionEditCtrl);
		mainVC.put("details_question", questionEditCtrl.getInitialComponent());
		
		rightsEditCtrl = new RightsMetadataEditController(ureq, wControl, item, securityCallback);
		listenTo(rightsEditCtrl);
		mainVC.put("details_rights", rightsEditCtrl.getInitialComponent());

		technicalEditCtrl = new TechnicalMetadataEditController(ureq, wControl, item, securityCallback);
		listenTo(technicalEditCtrl);
		mainVC.put("details_technical", technicalEditCtrl.getInitialComponent());

		if (qpoolModule.isPoolsEnabled()) {
			poolsCtrl = new PoolsMetadataController(ureq, wControl, item);
			mainVC.put("details_pools", poolsCtrl.getInitialComponent());
		}
		
		if (qpoolModule.isSharesEnabled()) {
			sharesController = new SharesMetadataController(ureq, wControl, item);
			mainVC.put("details_shares", sharesController.getInitialComponent());
		}

		putInitialPanel(mainVC);
		openPanel(ureq);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(generalEditCtrl);
		removeAsListenerAndDispose(questionEditCtrl);
		removeAsListenerAndDispose(rightsEditCtrl);
		removeAsListenerAndDispose(technicalEditCtrl);
		removeAsListenerAndDispose(poolsCtrl);
		removeAsListenerAndDispose(sharesController);
		generalEditCtrl = null;
		questionEditCtrl = null;
		rightsEditCtrl = null;
		technicalEditCtrl = null;
		poolsCtrl = null;
		sharesController = null;
	}
	
	public QuestionItem getItem() {
		return item;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"));
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof QItemEdited) {
			fireEvent(ureq, event);
		}
		openPanel(ureq);
	}
	
	public void updateShares() {
		if (poolsCtrl != null) {
			poolsCtrl.setItem(getItem());
		}
		if (sharesController != null) {
			sharesController.setItem(getItem());
		}
	}
	
	public void setItem(QuestionItem item, QuestionItemSecurityCallback securityCallback) {
		this.item = item;
		generalEditCtrl.setItem(item, securityCallback);
		questionEditCtrl.setItem(item, securityCallback);
		rightsEditCtrl.setItem(item, securityCallback);
		technicalEditCtrl.setItem(item, securityCallback);
		updateShares();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(MetadatasController.class, GUIPREF_KEY_OPEN_PANEL, panelToGuiPref(panelId));
		}
	}

	private String panelToGuiPref(String panelId) {
		return Long.toString(getIdentity().getKey()) + GUIPREFS_SEPARATOR +  panelId;
	}
	
	private void openPanel(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		String guiPref = (String) guiPrefs.get(MetadatasController.class, GUIPREF_KEY_OPEN_PANEL);
		String openPanel = guiPref != null? guiPrefToPanel(guiPref): "general";
		mainVC.contextRemove("in-general");
		mainVC.contextRemove("in-question");
		mainVC.contextRemove("in-rights");
		mainVC.contextRemove("in-technical");
		mainVC.contextRemove("in-pools");
		mainVC.contextRemove("in-shares");
		mainVC.contextPut("in-" + openPanel, "in");
	}
	
	private String guiPrefToPanel(String guiPref) {
		int from = Long.toString(getIdentity().getKey()).length() + GUIPREFS_SEPARATOR.length();
		return guiPref.substring(from);
	}
	
}