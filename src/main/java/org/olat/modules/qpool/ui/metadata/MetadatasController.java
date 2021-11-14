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
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;

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
	private RatingMetadataController ratingMetadataCtrl;
	private PoolsMetadataController poolsCtrl;
	private SharesMetadataController sharesController;
	
	private QuestionItem item;
	
	public MetadatasController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback qPoolSecurityCallback,
			QuestionItem item, MetadataSecurityCallback metadataScurityCallback, boolean ignoreCompetences, boolean wideLayout) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;

		mainVC = createVelocityContainer("item_metadatas");
		generalEditCtrl = new GeneralMetadataEditController(ureq, wControl, qPoolSecurityCallback, item,
				metadataScurityCallback, ignoreCompetences, wideLayout);
		listenTo(generalEditCtrl);
		mainVC.put("details_general", generalEditCtrl.getInitialComponent());

		questionEditCtrl = new QuestionMetadataEditController(ureq, wControl, item, metadataScurityCallback, wideLayout);
		listenTo(questionEditCtrl);
		mainVC.put("details_question", questionEditCtrl.getInitialComponent());
		
		if(item.getResourceableId() != null) {
			rightsEditCtrl = new RightsMetadataEditController(ureq, wControl, item, metadataScurityCallback, wideLayout);
			listenTo(rightsEditCtrl);
			mainVC.put("details_rights", rightsEditCtrl.getInitialComponent());
		}

		technicalEditCtrl = new TechnicalMetadataEditController(ureq, wControl, item, metadataScurityCallback, wideLayout);
		listenTo(technicalEditCtrl);
		mainVC.put("details_technical", technicalEditCtrl.getInitialComponent());

		if (metadataScurityCallback.canViewReviews()) {
			ratingMetadataCtrl = new RatingMetadataController(ureq, wControl, item);
			mainVC.put("details_ratings", ratingMetadataCtrl.getInitialComponent());
		}
		
		if (qPoolSecurityCallback.canUsePools()) {
			poolsCtrl = new PoolsMetadataController(ureq, wControl, item);
			listenTo(poolsCtrl);
			mainVC.put("details_pools", poolsCtrl.getInitialComponent());
		}
		
		if (qPoolSecurityCallback.canUseGroups()) {
			sharesController = new SharesMetadataController(ureq, wControl, item);
			listenTo(sharesController);
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
        super.doDispose();
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
	
	public void setItem(QuestionItem item, MetadataSecurityCallback metadataSecurityCallback) {
		this.item = item;
		generalEditCtrl.setItem(item, metadataSecurityCallback);
		questionEditCtrl.setItem(item, metadataSecurityCallback);
		if(rightsEditCtrl != null) {
			rightsEditCtrl.setItem(item, metadataSecurityCallback);
		}
		technicalEditCtrl.setItem(item, metadataSecurityCallback);
		if (ratingMetadataCtrl != null) {
			ratingMetadataCtrl.setItem(item);
		}
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
		mainVC.contextRemove("ingeneral");
		mainVC.contextRemove("inquestion");
		mainVC.contextRemove("inrights");
		mainVC.contextRemove("intechnical");
		mainVC.contextRemove("inratings");
		mainVC.contextRemove("inpools");
		mainVC.contextRemove("inshares");
		mainVC.contextPut("in" + openPanel.toLowerCase(), "in");
	}
	
	private String guiPrefToPanel(String guiPref) {
		int from = Long.toString(getIdentity().getKey()).length() + GUIPREFS_SEPARATOR.length();
		return guiPref.substring(from);
	}
	
}