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
package org.olat.repository.ui.author;

import java.util.Collection;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.Price;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ACRenderer implements FlexiCellRenderer {

	@Override
	public void render(StringOutput sb, Object val, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator)  {
		if(val instanceof Collection) {
			Collection<?> accessTypes = (Collection<?>)val;
			for(Object accessType:accessTypes) {
				if(accessType instanceof String) {
					String type = (String)accessType;
					sb.append("<i class='o_icon ").append(type).append("_icon o_icon-lg'></i>");
				}
			}
		} else if(val instanceof Boolean) {
			boolean acessControlled = ((Boolean)val).booleanValue();
			if(acessControlled) {
				sb.append("<i class='o_icon o_ac_group_icon o_icon-lg'></i>");
			}
		} else if (val instanceof OLATResourceAccess) {
			OLATResourceAccess access = (OLATResourceAccess)val;
			for(PriceMethodBundle bundle:access.getMethods()) {
				Price price = bundle.getPrice();
				String type = bundle.getMethod().getMethodCssClass();
				if(price == null || price.isEmpty()) {
					sb.append("<i class='o_icon ").append(type).append("_icon o_icon-lg'></i>");
				} else {
					String p = PriceFormat.fullFormat(price);
					sb.append("<span class='o_nowrap'><i class='o_icon ").append(type).append("_icon o_icon-lg'></i> ").append(p).append("</span>");
				}
			}
		}
	}
}
