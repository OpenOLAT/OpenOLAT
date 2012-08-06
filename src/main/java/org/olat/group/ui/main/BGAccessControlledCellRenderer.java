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

package org.olat.group.ui.main;

import java.util.Collection;
import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.resource.accesscontrol.model.Price;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for BGAccessControlledCellRenderer
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-1,2: access control of resources
public class BGAccessControlledCellRenderer implements CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof Collection) {
			Collection<?> accessTypes = (Collection<?>)val;
			for(Object accessType:accessTypes) {
				if(accessType instanceof String) {
					String type = (String)accessType;
					sb.append("<span class='b_small_icon ").append(type).append("_icon b_access_method'>").append("</span>");
				} else if (accessType instanceof PriceMethodBundle) {
					PriceMethodBundle bundle = (PriceMethodBundle)accessType;
					Price price = bundle.getPrice();
					String type = bundle.getMethod().getMethodCssClass();
					if(price == null || price.isEmpty()) {
						sb.append("<span class='b_small_icon ").append(type).append("_icon b_access_method'>").append("</span>");
					} else {
						String p = PriceFormat.fullFormat(price);
						sb.append("<span class='b_with_small_icon_left ").append(type).append("_icon b_access_method'>").append(p).append("</span>");
					}
				}
			}
		} else if(val instanceof Boolean) {
			boolean acessControlled = ((Boolean)val).booleanValue();
			if(acessControlled) {
				sb.append("<span class='b_small_icon b_group_accesscontrolled b_access_method'>").append("</span>");
			}
		}
	}
}
