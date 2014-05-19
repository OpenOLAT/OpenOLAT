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
package org.olat.core.commons.services.notifications.model;

import java.util.Date;
import java.util.Locale;

import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * Description:<br>
 * represents a news-item in SubscriptionInfo
 * 
 * <P>
 * Initial Date: 07.12.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class SubscriptionListItem {

	private final String link;
	private final String businessPath; 
	private final Date date;
	private final String description;
	private final String descriptionTooltip;
	private final String iconCssClass;
	private Object userObject;
	
	public SubscriptionListItem(String desc, String url, String businessPath, Date dateInfo, String iconCssClass) {
		this(desc, null, url, businessPath, dateInfo, iconCssClass);
	}
	
	public SubscriptionListItem(String desc, String tooltip, String url, String businessPath, Date dateInfo, String iconCssClass) {	
		this.description = desc;
		this.descriptionTooltip = tooltip;
		this.link = url;
		this.businessPath = businessPath;
		this.date = dateInfo;
		this.iconCssClass = iconCssClass;
	}
	
	public String getLink() {
		return link;
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public Object getUserObject() {
		return this.userObject;
	}

	public void setUserObject(Object usrObj) {
		this.userObject = usrObj;
	}
	
	public String getDescriptionTooltip() {
		return descriptionTooltip;
	}

	public String getIconCssClass() {
		return iconCssClass;
	}

	/**
	 * compose list item representation depending on mimeType
	 * @param mimeType
	 * @param locale
	 * @return formated list-item
	 */
	public String getContent(String mimeType, Locale locale) {		
		if (mimeType.equals(SubscriptionInfo.MIME_HTML)){
			return getHTMLContent(locale);
		} else {
			return getPlaintextContent(locale);
		}
	}


	private String getPlaintextContent(Locale locale) {
		Translator trans = Util.createPackageTranslator(ContextualSubscriptionController.class, locale);
		Formatter form = Formatter.getInstance(locale);
		StringBuilder sb = new StringBuilder();
		String datePart = trans.translate("subscription.listitem.dateprefix", new String [] { form.formatDateAndTime(date) } ) ;
		sb.append("- ");
		sb.append(description.trim());
		sb.append(" ").append(datePart.trim());
		if (StringHelper.containsNonWhitespace(link)) sb.append("\n").append("  ").append(link);
		return sb.toString();
	}

	private String getHTMLContent(Locale locale) {
		StringBuilder sb = new StringBuilder();
		Translator trans = Util.createPackageTranslator(ContextualSubscriptionController.class, locale);
		Formatter form = Formatter.getInstance(locale);
		String datePart = trans.translate("subscription.listitem.dateprefix", new String [] { form.formatDateAndTime(date) } ) ; 
		sb.append("<li>");			
		if (iconCssClass != null) {
			sb.append("<i class=\"o_icon o_icon-fw ");
			sb.append(iconCssClass);
			sb.append("\"></i>");
		}
		if (StringHelper.containsNonWhitespace(link)) {
			sb.append("<a href=\""); 
			sb.append(link);
			sb.append("\">");
		}
		if (StringHelper.containsNonWhitespace(description)) {
			sb.append(new OWASPAntiSamyXSSFilter().filter(description.trim()));
		}
		if (StringHelper.containsNonWhitespace(link)) sb.append("</a>");
		sb.append(" <span class='o_nowrap o_date'>").append(datePart.trim()).append("</span>");
		sb.append("</li>");
		return sb.toString();
	}

}
