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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.portfolio.ui.artefacts.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm00;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm03;
import org.olat.portfolio.ui.artefacts.collect.EPReflexionChangeEvent;
import org.olat.resource.OLATResource;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Shows an Artefact itself 
 * 
 * <P>
 * Initial Date: 09.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class EPArtefactViewController extends FormBasicController {

	private AbstractArtefact artefact;
	private TextElement title;
	@Autowired
	private EPFrontendManager ePFMgr;
	private FormLink deleteBtn;
	private DialogBoxController delYesNoDialog;
	private Map<String, Boolean> artAttribConfig;
	private boolean artefactChooseMode;
	private FormLink chooseBtn;
	private TextBoxListElement tblE;
	private boolean viewOnlyMode;
	private boolean artefactInClosedMap;
	@Autowired
	private PortfolioModule portfolioModule;
	private final boolean detailsLinkEnabled;
	
	private CloseableModalController artefactBox;
	private FormLink detailsLink;
	private FormLink reflexionBtn;
	private EPCollectStepForm03 reflexionCtrl;
	private FormLink descriptionBtn;
	private EPCollectStepForm00 descriptionCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	public EPArtefactViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, Map<String, Boolean> artAttribConfig, boolean artefactChooseMode, boolean viewOnlyMode, boolean detailsLink) {
		super(ureq, wControl, "singleArtefact");
		
		this.artefact = artefact;
		this.artefactChooseMode = artefactChooseMode;
		this.artefactInClosedMap = ePFMgr.isArtefactClosed(artefact);
		this.viewOnlyMode = viewOnlyMode;
		this.detailsLinkEnabled = detailsLink;
		
		if (viewOnlyMode){
			artAttribConfig = ePFMgr.getArtefactAttributeConfig(null); //get a default config
			// only enable a minimal set, not users settings
			artAttribConfig.put("artefact.author", true);
			artAttribConfig.put("artefact.description", true);
			artAttribConfig.put("artefact.reflexion", true);
			artAttribConfig.put("artefact.source", true);
			artAttribConfig.put("artefact.sourcelink", true);
			artAttribConfig.put("artefact.title", false);
			artAttribConfig.put("artefact.date", true);
			artAttribConfig.put("artefact.tags", true);
			artAttribConfig.put("artefact.used.in.maps", true);
			artAttribConfig.put("artefact.handlerdetails", true);
		}
		if (artefactChooseMode){
			artAttribConfig = ePFMgr.getArtefactAttributeConfig(null); //get a default config
			// only enable a minimal set, not users settings
			artAttribConfig.put("artefact.author", false);
			artAttribConfig.put("artefact.description", true);
			artAttribConfig.put("artefact.reflexion", false);
			artAttribConfig.put("artefact.source", true);
			artAttribConfig.put("artefact.sourcelink", false);
			artAttribConfig.put("artefact.title", false);
			artAttribConfig.put("artefact.date", true);
			artAttribConfig.put("artefact.tags", false);
			artAttribConfig.put("artefact.used.in.maps", false);
		}
		if (artAttribConfig==null){
			artAttribConfig = ePFMgr.getArtefactAttributeConfig(getIdentity());
		}
		this.artAttribConfig = artAttribConfig;

		initForm(ureq);
	}
	
	/**
	 * load without a config will display artefact with default displayconfig / or users preferences
	 * use this, when no ArtefactAttributeSettingsController is in the view to make settings!
	 * @param ureq
	 * @param wControl
	 */
	public EPArtefactViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact){
		this(ureq, wControl, artefact, null, false, false, false);		
	}
	
	/**
	 * load without a config in view-only mode (mostly in popups)
	 * @param ureq
	 * @param wControl
	 * @param artefact
	 * @param viewOnlyMode
	 */
	public EPArtefactViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean viewOnlyMode){
		this(ureq, wControl, artefact, null, false, viewOnlyMode, false);		
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(detailsLinkEnabled && !artefactChooseMode) {
			detailsLink = uifactory.addFormLink("details.link", formLayout, Link.BUTTON);
			detailsLink.setElementCssClass("o_sel_artefact_details");
		}

		title = uifactory.addInlineTextElement("title", artefact.getTitle(), formLayout, this);
		
		flc.contextPut("cssClosed", artefactInClosedMap ? "o_artefact_closed" : "");
		flc.contextPut("viewOnly", viewOnlyMode);
		
		if (viewOnlyMode || artefactInClosedMap) title.setEnabled(false);
		// get tags and prepare textboxlist-component 
		List<String> tagL = ePFMgr.getArtefactTags(artefact);	
		Map<String, String> tagLM = new HashMap<>();
		for (String tag : tagL) {
			tagLM.put(tag, tag);
		}	
		tblE = uifactory.addTextBoxListElement("tagTextbox", null, "tag.textboxlist.hint", tagLM, formLayout, getTranslator());
		if (viewOnlyMode || artefactInClosedMap) {
			tblE.setEnabled(false);
		} else {
			Map<String, String> allUsersTags = ePFMgr.getUsersMostUsedTags(getIdentity(), -1);
			tblE.setAutoCompleteContent(allUsersTags);
		}

		// get maps wherein this artefact is linked and create links to them
		List<PortfolioStructure> linkedMaps = ePFMgr.getReferencedMapsForArtefact(artefact);
		if (linkedMaps != null && linkedMaps.size() != 0) {
			List<FormLink> selectMapNames = new ArrayList<>(linkedMaps.size());
			int count = 0;
			for (PortfolioStructure ePMap : linkedMaps) {
				String mapTitle = StringHelper.escapeHtml(ePMap.getTitle());
				FormLink selectMap = uifactory.addFormLink("map-" + count++, "map", mapTitle, null, formLayout, Link.NONTRANSLATED);
				selectMap.setUserObject(ePMap.getOlatResource());
				selectMap.setEnabled(!viewOnlyMode && !artefactChooseMode);
				selectMapNames.add(selectMap);
			}
			flc.contextPut("maps", selectMapNames);
		}

		// build link to original source
		if (StringHelper.containsNonWhitespace(artefact.getBusinessPath())) {
			String sourceLink = createLinkToArtefactSource(ureq, artefact.getBusinessPath());
			flc.contextPut("artefactSourceLink", sourceLink);			
		}
		
		// create a delete button
		deleteBtn = uifactory.addFormLink("delete.artefact", formLayout, Link.BUTTON);
		deleteBtn.setIconLeftCSS("o_icon o_icon_delete");
		deleteBtn.addActionListener(FormEvent.ONCLICK);
		if (viewOnlyMode || artefactChooseMode || artefactInClosedMap) deleteBtn.setVisible(false);
		
		// let the artefact-handler paint what is special for this kind of artefact
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
		Controller detCtrl = artHandler.createDetailsController(ureq, getWindowControl(), artefact, viewOnlyMode || artefactInClosedMap);
		if (detCtrl != null) {
			flc.put("detailsController", detCtrl.getInitialComponent());
		}
				
		// create edit buttons the adapt meta-data
		if (!(viewOnlyMode || artefactChooseMode || artefactInClosedMap)){
			String reflexion = artefact.getReflexion();
			reflexion = FilterFactory.getHtmlTagAndDescapingFilter().filter(reflexion);
			reflexion = StringHelper.xssScan(reflexion);
			reflexion = Formatter.truncate(reflexion, 50);
			if (!StringHelper.containsNonWhitespace(reflexion)) {
				reflexion = "&nbsp; "; // show a link even if empty
			}
			reflexionBtn = uifactory.addFormLink("reflexionBtn", reflexion, null, formLayout, Link.NONTRANSLATED);
			reflexionBtn.setIconLeftCSS("o_icon o_icon_inline_editable");
			
			String description = artefact.getDescription();
			description = FilterFactory.getHtmlTagAndDescapingFilter().filter(description);
			description = Formatter.truncate(description, 50);
			description = StringHelper.xssScan(description);
			if (!StringHelper.containsNonWhitespace(description)) {
				description = "&nbsp; "; // show a link even if empty
			}
			descriptionBtn = uifactory.addFormLink("descriptionBtn", description, null, formLayout, Link.NONTRANSLATED);
			descriptionBtn.setIconLeftCSS("o_icon o_icon_inline_editable");
		}
		
		// if in artefactChooseMode, add an "choose this" button
		if(artefactChooseMode) {
			chooseBtn = uifactory.addFormLink("choose.artefact", formLayout, Link.BUTTON);
			chooseBtn.addActionListener(FormEvent.ONCLICK);
		}
		
		flc.contextPut("artefact", artefact);
		
		setArtAttribConfig(artAttribConfig);		
	}

	protected void setArtAttribConfig(Map<String, Boolean> attribConfig) {
		flc.contextRemove("artAttribConfig");
		flc.contextPut("artAttribConfig", attribConfig);
	}

	private void doOpenLinkToMap(UserRequest ureq, OLATResource mapResource) {
		String businessPath = "[" + mapResource.getResourceableTypeName() + ":"
				+ mapResource.getResourceableId() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private String createLinkToArtefactSource(UserRequest ureq, String businessPath){
		BusinessControlFactory bCF = BusinessControlFactory.getInstance(); 
		List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
		boolean valid = (ceList.size() > 0) && NewControllerFactory.getInstance().validateCEWithContextControllerCreator(ureq, getWindowControl(), ceList.get(0));
		String busLink = bCF.getAsURIString(ceList, true); 
		if (valid && StringHelper.containsNonWhitespace(busLink)){
			return "<a href=\"" + busLink + "\">" + translate("artefact.open.source") + "</a>";
		} else return translate("artefact.no.source");
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == deleteBtn) {
			String text = translate("delete.artefact.text", StringHelper.escapeHtml(artefact.getTitle()));
			delYesNoDialog = activateYesNoDialog(ureq, translate("delete.artefact"), text, delYesNoDialog);
		} else if (source == chooseBtn){
			fireEvent(ureq, new EPArtefactChoosenEvent(artefact));
		} else if (source == detailsLink) {
			popupArtefact(ureq);
		} else if (source == reflexionBtn) {
			popupReflexionCallout(ureq);
		} else if (source == descriptionBtn){
			popupDescriptionCallout(ureq);
		} else if(source == tblE){
			List<String> actualTags = tblE.getValueList();
			ePFMgr.setArtefactTags(getIdentity(), artefact, actualTags);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("map".equals(link.getCmd())) {
				OLATResource map = (OLATResource)link.getUserObject();
				doOpenLinkToMap(ureq, map);
			}
		}
	}
	
	private void popupDescriptionCallout(UserRequest ureq) {
		descriptionCtrl = new EPCollectStepForm00(ureq, getWindowControl(), artefact);
		listenTo(descriptionCtrl);				
		instantiateCalloutController(ureq, descriptionCtrl.getInitialComponent(), descriptionBtn);
	}
	

	private void popupReflexionCallout(UserRequest ureq) {
		reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact);
		listenTo(reflexionCtrl);		
		instantiateCalloutController(ureq, reflexionCtrl.getInitialComponent(), reflexionBtn);
	}

	/**
	 * re-use the same callout-controller, as there can be only one anyway
	 * @param ureq
	 * @param content
	 * @param button
	 */
	private void instantiateCalloutController(UserRequest ureq, Component content, FormLink button){
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), content, button, artefact.getTitle(), true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();		
	}

	protected void popupArtefact(UserRequest ureq) {
		String boxTitle = translate("view.artefact.header");
		artefactBox = EPUIFactory.getAndActivatePopupArtefactController(artefact, ureq, getWindowControl(), boxTitle);
		listenTo(artefactBox);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == delYesNoDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				ePFMgr.deleteArtefact(artefact);
				flc.setVisible(false);
				fireEvent(ureq, new EPArtefactDeletedEvent(artefact));
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(artefact));
				ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_ARTEFACT_REMOVED, getClass());
			}
		} else if (source == reflexionCtrl && event instanceof EPReflexionChangeEvent){
			EPReflexionChangeEvent refEv = (EPReflexionChangeEvent) event;
			artefact.setReflexion(refEv.getReflexion());
			ePFMgr.updateArtefact(artefact);
			calloutCtrl.deactivate();
			closeCalloutController();
			initForm(ureq);
		} else if (source == calloutCtrl){
			closeCalloutController();
		} else if (source == descriptionCtrl) {
			ePFMgr.updateArtefact(artefact);
			calloutCtrl.deactivate();
			closeCalloutController();
			initForm(ureq);
		}
	}
	
	private void closeCalloutController(){
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		String newTitle = title.getValue();
		artefact.setTitle(newTitle);
		ePFMgr.updateArtefact(artefact);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
