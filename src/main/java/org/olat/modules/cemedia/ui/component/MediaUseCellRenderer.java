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
package org.olat.modules.cemedia.ui.component;

import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.cemedia.ui.MediaUsageRow;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUseCellRenderer extends StaticFlexiCellRenderer {
	
	public MediaUseCellRenderer(String action) {
		super("", action);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof MediaUsageRow usageRow) {
			if(usageRow.isAccess()) {
				FlexiTableElementImpl ftE = source.getFormItem();
				NameValuePair pair = new NameValuePair(getAction(), Integer.toString(row));
				String jsCode = FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), source.getFormDispatchId(), 1, false, true, false, pair);
				String href = usageRow.getPageUrl() == null ? "javascript:;" : usageRow.getPageUrl();
				target.append("<a  href=\"").append(href).append("\" onclick=\"").append(jsCode).append("; return false;\">");
				render(target, usageRow);
				target.append("</a>");
			} else {
				render(target, usageRow);
			}
		}
	}
	
	private void render(StringOutput target, MediaUsageRow usageRow) {
		target.append("<span><i class=\"o_icon ").append(usageRow.getPageIconCssClass()).append("\"> </i> ").append(usageRow.getPage()).append("</span>");
	}
}
