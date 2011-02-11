/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 * 
 * @author skoeber
 */
package de.bps.olat.portal.links;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.UserConstants;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;

public class LinksPortletRunController extends BasicController {	
	
	private VelocityContainer portletVC;
	
	protected LinksPortletRunController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		portletVC = this.createVelocityContainer("portlet");
		String lang = I18nManager.getInstance().getLocaleKey(ureq.getLocale());
		if (lang == null) lang = I18nManager.getInstance().getLocaleKey(I18nModule.getDefaultLocale());

		boolean isGuest = !ureq.getUserSession().isAuthenticated();
		String inst = new String();
		if(!isGuest) inst = ureq.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, getLocale());
		
		StringBuffer sb = new StringBuffer();
		
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
		
		// In Template einfügen
		if (sb.length() > 0) {
			String portletContent = "<ul>" + sb.toString() + "</ul>";
			portletVC.contextPut("content", portletContent);
		} else {
			portletVC.contextPut("content", translate("no.content.found"));
		}
		putInitialPanel(this.portletVC);
	}
	
	/**
	 * Add all links to the portlet depending on institution 
	 * @param Map with content
	 * @param Institutional name as String
	 * @param Language as String
	 * @param StringBuffer to append the link
	 */
	private void appendContentFor(Map<String, PortletInstitution> content,
			String inst, String lang, StringBuffer sb) {
		for( PortletLink link : content.get(inst).getLinks() ) {
			if(link.getLanguage().equals(lang) | link.getLanguage().equals(LinksPortlet.LANG_ALL))
				appendContent(link, sb);
		}
	}

	/**
	 * Add one link to the portlet
	 * @param PortletLink
	 * @param StringBuffer to append
	 */
	private void appendContent(PortletLink link, StringBuffer sb) {
		sb.append("<li>" + buildContentLine(link.getTitle(), link.getUrl(), link.getDescription(), link.getTarget(), link.getLanguage()) + "</li>");
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
	private String buildContentLine(String title, String URL, String descr, String target, String lang) {
		StringBuffer sb = new StringBuffer();
		sb.append("<a href=\"");
		sb.append(URL);
		sb.append("\" title=\"");
		sb.append(title);
		sb.append("\" target=\"_");
		sb.append(target);
		sb.append("\">");
		sb.append(title);
		sb.append("</a> - ");
		sb.append(descr);
		return sb.toString();
	}
	
	/**
	 * @see org.olat.gui.control.DefaultController#event(org.olat.gui.UserRequest, org.olat.gui.components.Component, org.olat.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

	/**
	 * @see org.olat.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if(portletVC != null) portletVC = null;
	}

}
