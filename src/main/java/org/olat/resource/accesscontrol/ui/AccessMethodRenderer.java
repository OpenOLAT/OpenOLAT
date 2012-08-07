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
package org.olat.resource.accesscontrol.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessTransaction;

/**
 * 
 * Description:<br>
 * Render the type of an access method
 * 
 * <P>
 * Initial Date:  27 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessMethodRenderer implements CustomCellRenderer {
	
	private final AccessControlModule acModule;
	
	public AccessMethodRenderer(AccessControlModule acModule) {
		this.acModule = acModule;
	}
	
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		
		if(val instanceof AccessTransaction) {
			AccessTransaction transaction = (AccessTransaction)val;
			Set<String> uniqueType = new HashSet<String>(3);
			render(sb, transaction, uniqueType, locale);
		} else if (val instanceof List) {
			@SuppressWarnings("unchecked")
			List<AccessTransaction> transactions = (List<AccessTransaction>)val;
			Set<String> uniqueType = new HashSet<String>((transactions.size() * 2) + 1);
			for(AccessTransaction transaction : transactions) {
				render(sb, transaction, uniqueType, locale);
			}
		}
	}
	
	private void render(StringOutput sb, AccessTransaction transaction, Set<String> uniqueType, Locale locale) {
		String type = transaction.getMethod().getType();
		if(uniqueType.contains(type)) return;
		uniqueType.add(type);
		
		AccessMethodHandler handler = acModule.getAccessMethodHandler(type);
		sb.append("<span class='b_with_small_icon_left ");
		sb.append(transaction.getMethod().getMethodCssClass());
		sb.append("_icon'>");
		sb.append(handler.getMethodName(locale));
		sb.append("</span>");
	}
}
