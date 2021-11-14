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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.portal.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.UserConstants;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author skoeber
 */
public class LinksPortletRunController extends BasicController {	
	
	private static final String LINKADD = "linkadd";
	private static final String LINKID = "linkid";
	private static final String LINKDEL = "linkdel";
	private VelocityContainer portletVC;
	private Link editButton;
	private Panel viewPanel;
	private LinksPortletEditController editorCtrl;
	private CloseableModalController cmc;
	private Link backLink;
	private DialogBoxController delLinkCtrl;
	
	@Autowired
	private I18nModule i18nModule;
	
	protected LinksPortletRunController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		portletVC = this.createVelocityContainer("portlet");
		initOrUpdatePortletView(ureq);
		
		//edit link
		if (ureq.getUserSession().getRoles().isAdministrator()){
			editButton = LinkFactory.createButtonXSmall("editor.button", portletVC, this);
			editButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}

		viewPanel = new Panel("view");
		viewPanel.setContent(portletVC);
		putInitialPanel(viewPanel);
	}
	
	private void initOrUpdatePortletView(UserRequest ureq){
		String lang = i18nModule.getLocaleKey(ureq.getLocale());
		if (lang == null) {
			lang = i18nModule.getLocaleKey(I18nModule.getDefaultLocale());
		}
		// fxdiff: compare with language-base not with variant...
		int underlinePos = lang.indexOf("_");
		if (underlinePos != -1){
			lang = lang.substring(0,underlinePos);
		}
		
		boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		String inst = new String();
		if(!isGuest) inst = ureq.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, getLocale());
		
		StringBuilder sb = new StringBuilder();
		
		// Inhalt verarbeiten
		Map<String, PortletInstitution> content = LinksPortlet.getContent();
		if (content != null && !content.isEmpty() ) {
			if(isGuest) {
				// get content intended for guests
				if(content.keySet().contains(LinksPortlet.ACCESS_GUEST))
					appendContentFor(content, LinksPortlet.ACCESS_GUEST, lang, sb);
			} else {
				// get content for the users institution
				if(content.keySet().contains(inst))
					appendContentFor(content, inst, lang, sb);
				// get content intended for registered users
				if(content.keySet().contains(LinksPortlet.ACCESS_REG))
					appendContentFor(content, LinksPortlet.ACCESS_REG, lang, sb);
				// get content intended for all users
				if(content.keySet().contains(LinksPortlet.ACCESS_ALL))
					appendContentFor(content, LinksPortlet.ACCESS_ALL, lang, sb);
			}
		}
		
		if (sb.length() > 0) {
			String portletContent = "<ul class='list-unstyled'>" + sb.toString() + "</ul>";
			portletVC.contextPut("content", portletContent);
		} else {
			portletVC.contextPut("content", translate("no.content.found"));
		}
	}
	
	/**
	 * Add all links to the portlet depending on institution 
	 * @param Map with content
	 * @param Institutional name as String
	 * @param Language as String
	 * @param StringBuffer to append the link
	 */
	private void appendContentFor(Map<String, PortletInstitution> content,
			String inst, String lang, StringBuilder sb) {
		String linkLang = "";
		int underlinePos = -1;
		for( PortletLink link : content.get(inst).getLinks() ) {
			linkLang = link.getLanguage();
			underlinePos = linkLang.indexOf("_");
			if (underlinePos != -1){
				linkLang= linkLang.substring(0,underlinePos);
			}
			if(linkLang.equals(lang) || linkLang.equals(LinksPortlet.LANG_ALL))
				appendContent(link, sb);
		}
	}

	/**
	 * Add one link to the portlet
	 * @param PortletLink
	 * @param StringBuffer to append
	 */
	private void appendContent(PortletLink link, StringBuilder sb) {
		sb.append("<li>").append(buildContentLine(link.getTitle(), link.getUrl(), link.getDescription(), link.getTarget())).append("</li>");
	}
	
	/**
	 * Format the link
	 * @param String title
	 * @param String URL
	 * @param String descr
	 * @param String target
	 * @param String lang
	 * @return
	 */
	private String buildContentLine(String title, String url, String descr, String target) {
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append(url);
		sb.append("\" title=\"");
		sb.append(title);
		sb.append("\" target=\"_");
		sb.append(target);
		sb.append("\">");
		sb.append(title);
		sb.append("</a>");
		sb.append(descr);
		return sb.toString();
	}
	
	/**
	 * @see org.olat.gui.control.DefaultController#event(org.olat.gui.UserRequest, org.olat.gui.components.Component, org.olat.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == editButton){
			buildEditorPanel();
		} else if (source == backLink) {
			LinksPortlet.reInit(ureq);
			initOrUpdatePortletView(ureq);
			viewPanel.setContent(portletVC);				
		}	else if (source instanceof Link) {
			// clicked on a link in editor-mode -> open editor in callout
			Link link = (Link) source;
			String linkName = link.getComponentName();
			if (linkName.contains(LINKID)){
				String identifier = linkName.substring(LINKID.length());
				PortletLink portLink = LinksPortlet.getLinkByIdentifier(identifier);
				if (portLink != null) {
					popupLinkEditor(ureq, portLink);
				} else {
					showError("error.link.not.found");
				}
			} else if (linkName.contains(LINKADD)){
				// add a link to institution:
				PortletLink newLink = new PortletLink("", "", "", i18nModule.getLocaleKey(ureq.getLocale()), "", null);
				// find institution and port in link!
				String institution = link.getCommand().substring(LINKADD.length());
				PortletInstitution inst = LinksPortlet.getContent().get(institution);
				newLink.setInstitution(inst);
				popupLinkEditor(ureq, newLink);
			} else if (linkName.contains(LINKDEL)){
				String identifier = linkName.substring(LINKDEL.length());
				PortletLink portLink = LinksPortlet.getLinkByIdentifier(identifier);
				delLinkCtrl = activateYesNoDialog(ureq, translate("del.link.title"), translate("del.link.text", portLink.getTitle()), delLinkCtrl);
				delLinkCtrl.setUserObject(portLink);
			}
		}
	}
	
	private void popupLinkEditor(UserRequest ureq, PortletLink portLink) {
		String title = translate("link.editor.title");				
		removeAsListenerAndDispose(editorCtrl);
		editorCtrl = new LinksPortletEditController(ureq, getWindowControl(), portLink);
		listenTo(editorCtrl);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editorCtrl.getInitialComponent(),  true, title);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorCtrl && event == Event.DONE_EVENT) {
			LinksPortlet.reInit(ureq);
			cmc.deactivate();
			buildEditorPanel();
		} else if (source == delLinkCtrl && DialogBoxUIFactory.isYesEvent(event) ){
			LinksPortlet.removeLink( (PortletLink) delLinkCtrl.getUserObject() );
			showInfo("del.link.success");
			buildEditorPanel();
		}
	}
	
	private void buildEditorPanel(){
		VelocityContainer editorVC = this.createVelocityContainer("editorLinkOverview");
		Map<String, PortletInstitution> content = LinksPortlet.getContent();
		if (content != null && !content.isEmpty() ) {
			ArrayList<String> allInst = new ArrayList<>();
			ArrayList<String> allInstTranslated = new ArrayList<>();
			HashMap<Integer, ArrayList<String>> allInstWithLinkIds = new HashMap<>();
			int instCount = 1;
			for (Iterator<String> iterator = content.keySet().iterator(); iterator.hasNext();) {
				String inst = iterator.next();
				allInst.add(inst);
				String instTranslated = inst;
				if (inst.equals(LinksPortlet.ACCESS_ALL)) instTranslated = translate("access.all");
				if (inst.equals(LinksPortlet.ACCESS_REG)) instTranslated = translate("access.registered.users");
				if (inst.equals(LinksPortlet.ACCESS_GUEST)) instTranslated = translate("access.guests");
				allInstTranslated.add(instTranslated);		
				
				PortletInstitution portletsForInst = content.get(inst);
				// collect identifiers to find them in VC.
				ArrayList<String> instLinksIdentifiers = new ArrayList<>();
				
				// add add-link per institution
				LinkFactory.createCustomLink(LINKADD + inst, LINKADD + inst, "add.link", Link.BUTTON_XSMALL, editorVC, this);
				
				for (PortletLink link : portletsForInst.getLinks()) {
					String linkID = link.getIdentifier();
					LinkFactory.createCustomLink(LINKID + linkID, "inst" + inst, link.getTitle(), Link.LINK | Link.NONTRANSLATED, editorVC, this);
					// add remove-links
					LinkFactory.createCustomLink(LINKDEL + linkID, "inst" + inst, "-", Link.BUTTON_XSMALL | Link.NONTRANSLATED, editorVC, this);					
					instLinksIdentifiers.add(linkID);
				}
				allInstWithLinkIds.put(instCount, instLinksIdentifiers);
				instCount++;
			}
			editorVC.contextPut("allInst", allInst);
			editorVC.contextPut("allInstTranslated", allInstTranslated);
			editorVC.contextPut("allInstWithLinkIds", allInstWithLinkIds);
		}
		
		backLink = LinkFactory.createButtonXSmall("back", editorVC, this);		
		backLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		backLink.setPrimary(true);

		viewPanel.setContent(editorVC);
	}

	@Override
	protected void doDispose() {
		if(portletVC != null) portletVC = null;
        super.doDispose();
	}

}
