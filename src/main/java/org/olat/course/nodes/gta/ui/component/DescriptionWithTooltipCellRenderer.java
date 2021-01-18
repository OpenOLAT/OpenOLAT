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
package org.olat.course.nodes.gta.ui.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;

/**
 * 
 * Initial date: 06.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DescriptionWithTooltipCellRenderer implements FlexiCellRenderer {
	
	private final AtomicInteger positionCounter = new AtomicInteger(1);

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String) {
			String desc = (String)cellValue;
			if(desc.length() > 50) {
				String truncatedDesc = FilterFactory.getHtmlTagsFilter().filter(desc);
				truncatedDesc = Formatter.truncate(truncatedDesc, 50, "");

				String pos = Integer.toString(positionCounter.incrementAndGet());
				sb.append("<span id='o_sel_desc_").append(pos).append("' href='javascript:void(0); return false;'>")
				  .append(truncatedDesc)
				  .append("\u2026</span>");
				
				sb.append("<div id='o_sel_desc_tooltip_").append(pos).append("' style='display:none;'>")
				  .append(desc)
				  .append("</div>");
				
				sb.append("<script>")
			      .append("/* <![CDATA[ */")
				  .append("jQuery(function() {\n")
				  .append("  jQuery('#o_sel_desc_").append(pos).append("').tooltip({\n")
				  .append("    html: true,\n")
				  .append("    container: 'body',\n")
				  .append("    placement: 'bottom',\n")
				  .append("    title: function(){ return jQuery('#o_sel_desc_tooltip_").append(pos).append("').html(); }\n")
				  .append("  });\n")
				  .append("  jQuery('#o_sel_desc_").append(pos).append("').on('click', function(){\n")
				  .append("    jQuery('#o_sel_desc_").append(pos).append("').tooltip('hide');\n")
				  .append("  });\n")
				  .append("});")
				  .append("/* ]]> */")
				  .append("</script>");
			} else {
				sb.append(desc);
			}
		}
	}
}