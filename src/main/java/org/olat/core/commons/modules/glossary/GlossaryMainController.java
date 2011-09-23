/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.modules.glossary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * Displays a List of all glossary-entries. 
 * If the user is author or administrator, he will get Links to add, edit or delete Items.
 * The list is sortable by an alphabetical register.
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryMainController extends BasicController implements Activateable {

	private VelocityContainer glistVC;
	private Link addButton;
	private LockResult lockEntry = null;
	private boolean editModeEnabled;
	private DialogBoxController deleteDialogCtr;
	private Controller glossEditCtrl;
	private ArrayList<GlossaryItem> glossaryItemList;
	private GlossaryItem currentDeleteItem;
	private String filterIndex = "";
	private VFSContainer glossaryFolder;
	private CloseableModalController cmc;
	private OLATResourceable resourceable;
	private static final String CMD_EDIT = "cmd.edit.";
	private static final String CMD_DELETE = "cmd.delete.";
	private static final String REGISTER_LINK = "register.link.";

	public GlossaryMainController(WindowControl control, UserRequest ureq, VFSContainer glossaryFolder, OLATResourceable res, boolean allowGlossaryEditing) {
		super(ureq, control);
		this.editModeEnabled = allowGlossaryEditing;
		this.glossaryFolder = glossaryFolder;
		this.resourceable = res;
		addLoggingResourceable(CoreLoggingResourceable.wrap(res, OlatResourceableType.genRepoEntry));
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
		glistVC = createVelocityContainer("glossarylist");

		addButton = LinkFactory.createButtonSmall("cmd.add", glistVC, this);
		initEditView(ureq, allowGlossaryEditing);

		glossaryItemList = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);
		Properties glossProps = GlossaryItemManager.getInstance().getGlossaryConfig(glossaryFolder);
		Boolean registerEnabled = Boolean.valueOf(glossProps.getProperty(GlossaryItemManager.REGISTER_ONOFF));
		glistVC.contextPut("registerEnabled", registerEnabled);
		if (!registerEnabled) {
			filterIndex = "all";
		}		
		updateRegisterAndGlossaryItems();
		
		Link showAllLink = LinkFactory.createCustomLink(REGISTER_LINK + "all", REGISTER_LINK + "all", "glossary.list.showall", Link.LINK,
				glistVC, this);
		glistVC.contextPut("showAllLink", showAllLink);

		// add javascript and css file
		JSAndCSSComponent tmJs = new JSAndCSSComponent("glossaryJS", this.getClass(), null, "glossary.css", true);
		glistVC.put("glossaryJS", tmJs);

		putInitialPanel(glistVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());

		// controllers get disposed itself
		// release edit lock
		if (lockEntry != null){
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}	
	}
	

	/**
	 * create a List with all Indexes in this Glossary
	 * 
	 * @param gIList
	 * @return List containing the Links.
	 */
	protected List<Link> getIndexLinkList(ArrayList<GlossaryItem> gIList) {
		List<Link> indexLinkList = new ArrayList<Link>(gIList.size());
		Set<String> addedKeys = new HashSet<String>();
		//get existing indexes
		for (GlossaryItem gi : gIList) {
			String indexChar = gi.getIndex();
			if (!addedKeys.contains(indexChar)) {
				addedKeys.add(indexChar);
			}
		}
		//build register, first found should be used later on
		char alpha;
		boolean firstIndexFound = false;
		for (alpha='A'; alpha <= 'Z'; alpha++){
			String indexChar = String.valueOf(alpha);
			Link indexLink = LinkFactory.createCustomLink(REGISTER_LINK + indexChar, REGISTER_LINK + indexChar, indexChar, Link.NONTRANSLATED,	glistVC, this);
			if (!addedKeys.contains(indexChar)){
				indexLink.setEnabled(false);
			} else if (!filterIndex.equals("all") && !firstIndexFound && !addedKeys.contains(filterIndex)){
				filterIndex = indexChar;
				firstIndexFound = true;
			}
			indexLinkList.add(indexLink);
		}
		
		return indexLinkList;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == addButton) {
			removeAsListenerAndDispose(glossEditCtrl);
			glossEditCtrl = new GlossaryItemEditorController(ureq, getWindowControl(), glossaryFolder, glossaryItemList, null);
			listenTo(glossEditCtrl);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", glossEditCtrl.getInitialComponent());
			cmc.activate();
			listenTo(cmc);
		} else if (source instanceof Link) {
			Link button = (Link) source;
			String cmd = button.getCommand();
			if (button.getUserObject() instanceof GlossaryItem){
				GlossaryItem currentGlossaryItem = (GlossaryItem) button.getUserObject();
				if (cmd.startsWith(CMD_EDIT)) {
					removeAsListenerAndDispose(glossEditCtrl);
					glossEditCtrl = new GlossaryItemEditorController(ureq, getWindowControl(), glossaryFolder, glossaryItemList, currentGlossaryItem);
					listenTo(glossEditCtrl);
					removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), "close", glossEditCtrl.getInitialComponent());
					cmc.activate();
					listenTo(cmc);
				} else if (button.getCommand().startsWith(CMD_DELETE)) {
					currentDeleteItem = currentGlossaryItem;
					if (deleteDialogCtr != null) {
						deleteDialogCtr.dispose();
					}
					deleteDialogCtr = activateYesNoDialog(ureq, null, translate("glossary.delete.dialog", currentGlossaryItem.getGlossTerm()),
							deleteDialogCtr);
				} 
			}
			else if (button.getCommand().startsWith(REGISTER_LINK)) {
				filterIndex = cmd.substring(cmd.lastIndexOf(".") + 1);

				updateRegisterAndGlossaryItems();				
			}
		}

	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == cmc){
			// modal dialog closed -> persist changes on glossaryitem
			GlossaryItemManager.getInstance().saveGlossaryItemList(glossaryFolder, glossaryItemList);
			glossaryItemList = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);
			updateRegisterAndGlossaryItems();
		}	else if (source == deleteDialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				glossaryItemList.remove(currentDeleteItem);
				GlossaryItemManager.getInstance().saveGlossaryItemList(glossaryFolder, glossaryItemList);
				// back to glossary view
				updateRegisterAndGlossaryItems();
			}
		}

	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest,
	 *      java.lang.String)
	 */
	public void activate(UserRequest ureq, String viewIdentifier) {
		// if already open from LR and tab gets activated from course:
		if (viewIdentifier != null){
			boolean allowEdit = Boolean.parseBoolean(viewIdentifier);
			initEditView(ureq, allowEdit);
		}
	}

	private void updateRegisterAndGlossaryItems(){
		glistVC.contextPut("registerLinkList", getIndexLinkList(glossaryItemList));
		glistVC.contextPut("editAndDelButtonList", updateView(glossaryItemList, filterIndex));
	}
	
	
	/**
	 * 
	 * @param List with GlossaryItems
	 * @return a list (same size as GlossaryItems) which contains again lists with
	 *         one editButton and one deleteButton
	 */
	private List<List<Link>> updateView(ArrayList<GlossaryItem> gIList, String choosenFilterIndex) {
		List<List<Link>> editAndDelButtonList = new ArrayList<List<Link>>(gIList.size());
		int linkNum = 1;
		Set<String> keys = new HashSet<String>();
		StringBuilder bufDublicates = new StringBuilder();
		Collections.sort(gIList);
		
		glistVC.contextPut("filterIndex", choosenFilterIndex);		
		if (!filterIndex.equals("all")) {
			// highlight filtered index		
			Link indexLink = (Link) glistVC.getComponent(REGISTER_LINK + choosenFilterIndex);
			if (indexLink!=null){
				indexLink.setCustomEnabledLinkCSS("o_glossary_register_active");
			}
		}
		
		for (GlossaryItem gi : gIList) {
			Link tmpEditButton = LinkFactory.createCustomLink(CMD_EDIT + linkNum, CMD_EDIT + linkNum, "cmd.edit", Link.BUTTON_SMALL, glistVC,
					this);
			tmpEditButton.setUserObject(gi);
			Link tmpDelButton = LinkFactory.createCustomLink(CMD_DELETE + linkNum, CMD_DELETE + linkNum, "cmd.delete", Link.BUTTON_SMALL,
					glistVC, this);
			tmpDelButton.setUserObject(gi);
			List<Link> tmpList = new ArrayList<Link>(2);
			tmpList.add(tmpEditButton);
			tmpList.add(tmpDelButton);

			if (keys.contains(gi.getGlossTerm()) && (bufDublicates.indexOf(gi.getGlossTerm()) == -1)) {
				bufDublicates.append(gi.getGlossTerm());
				bufDublicates.append(" ");
			} else {
				keys.add(gi.getGlossTerm());
			}
			editAndDelButtonList.add(tmpList);
			linkNum++;
		}
		
		if ((bufDublicates.length() > 0) && editModeEnabled) {
			showWarning("warning.contains.dublicates", bufDublicates.toString());
		}
		return editAndDelButtonList;
	}

	/**
	 * show edit buttons only if there is not yet a lock on this glossary
	 * 
	 * @param ureq
	 * @param allowGlossaryEditing
	 */
	private void initEditView(UserRequest ureq, boolean allowGlossaryEditing) {

		glistVC.contextPut("editModeEnabled", Boolean.valueOf(allowGlossaryEditing));
		if (allowGlossaryEditing) {
			// try to get lock for this glossary
			lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(resourceable, ureq.getIdentity(), "GlossaryEdit");
			if (!lockEntry.isSuccess()) {
				showInfo("glossary.locked", lockEntry.getOwner().getName());
				glistVC.contextPut("editModeEnabled", Boolean.FALSE);
			}
		}
	}

}
