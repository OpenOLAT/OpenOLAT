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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.commons.info.ui;

import java.text.DateFormat;

import org.olat.commons.info.model.InfoMessage;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * Small helper class to format some datas of the InfoMessage for display.
 * 
 * <P>
 * Initial Date:  28 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoDisplayHelper {
	
	private final DateFormat formatter;
	private final Translator translator;
	
	public InfoDisplayHelper(Translator translator) {
		this.formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, translator.getLocale());
		this.translator = translator;
	}

	public String getAuthor(InfoMessage info) {
		User user = info.getAuthor().getUser();
		String formattedName = user.getProperty(UserConstants.FIRSTNAME, null)
			+ " " + user.getProperty(UserConstants.LASTNAME, null);
		return formattedName;
	}
	
	
	public String getInfos(InfoMessage info) {
		String formattedName = getAuthor(info);
		String creationDate = formatter.format(info.getCreationDate());
		String msgAddInfos = translator.translate("display.info", new String[]{formattedName, creationDate});
		return msgAddInfos;
	}
	
	public boolean isModified(InfoMessage info) {
		return info.getModifier() != null;
	}
	
	public String getModifier(InfoMessage info) {
		if(info.getModifier() == null) return "";//return empty string for velocity
		
		User user = info.getModifier().getUser();
		String formattedName = user.getProperty(UserConstants.FIRSTNAME, null)
			+ " " + user.getProperty(UserConstants.LASTNAME, null);
		String creationDate = formatter.format(info.getModificationDate());
		String msgAddInfos = translator.translate("display.modifier", new String[]{formattedName, creationDate});
		return msgAddInfos;
	}
	
	public String getMessage(InfoMessage info) {
		String message = info.getMessage();
		if(StringHelper.containsNonWhitespace(message)) {
			return Formatter.escWithBR(message).toString();
		}
		return "";
	}
}
