/**
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
package org.olat.core.configuration.gui;


import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.olat.core.configuration.model.ConfigurationPropertiesContentRow;
import org.olat.core.configuration.model.OlatPropertiesTableContentRow;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 27.08.2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class PropertyValueCellRenderer implements FlexiCellRenderer{
	
	private static final String[] protectedKeys = {
		"azure.adfs.client.secret",
		"db.pass",
		"instanceIdentifyer",
		"ldap.trustStorePwd",
		"mediasite.enterprise.secret",				
		"minimalhome.ext.password",	
		"oauth.datenlotsen.client.secret",				
		"oauth.keycloak.client.secret",		
		"onlyoffice.jwt.secret",
		"opencast.api.password",
		"opencast.lti.secret",				
		"paypal.security.password",
		"paypal.checkout.v2.client.secret",				
		"performx.server.password",
		"secret_key",
		"smtp.pwd",
		"switch.eduid.client.secret",		
		"vc.adobe.adminpassword",
		"vc.bigbluebutton.shared.secret",			
		"vc.bigbluebutton.secret",			
		"vc.openmeetings.adminpassword",
		"vc.teams.api.secret",		
		"vc.vitero.adminpassword",
		"websms.password"
	};
	
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
		URLBuilder ubu, Translator translator) {
		Object content = source.getFormItem().getTableDataModel().getObject(row);
		
		if (content instanceof OlatPropertiesTableContentRow) {
			if (cellValue instanceof String) {	
				if (renderer != null) {
					OlatPropertiesTableContentRow contentRow = (OlatPropertiesTableContentRow) content;
					
					if (StringHelper.containsNonWhitespace((String)cellValue)) {
						if(Arrays.asList(protectedKeys).stream().anyMatch(contentRow.getPropertyKey()::contains)) {
							renderSecret(target, (String)cellValue);
						} else {
							renderRegulat(target, (String)cellValue);
						}
					}
				} else {
					target.append((String) cellValue);
				}
			}
		} else if (content instanceof ConfigurationPropertiesContentRow) {
			if (cellValue instanceof String) {	
				if (renderer != null) {
					ConfigurationPropertiesContentRow contentRow = (ConfigurationPropertiesContentRow) content;
					
					if (StringHelper.containsNonWhitespace((String)cellValue)) {
						if(Arrays.asList(protectedKeys).stream().anyMatch(contentRow.getKey()::contains)) {
							renderSecret(target, (String)cellValue);
						} else {
							renderRegulat(target, (String)cellValue);
						}
					}
				} else {
					target.append((String) cellValue);
				}
			}
		}
	}
	
	private void renderSecret(StringOutput target, String secret) {
		// give the smallest possible hint. For security extremists this might already be too much..
		target.append(StringUtils.left(secret, 1)).append("************");		
	}
	private void renderRegulat(StringOutput target, String secret) {
		target.append("<div class=\"o_admin_property_table_column\">").append(secret).append("</div>");
	}
}


