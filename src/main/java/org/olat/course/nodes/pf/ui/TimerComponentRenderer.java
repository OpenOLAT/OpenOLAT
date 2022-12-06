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
package org.olat.course.nodes.pf.ui;

import java.util.Date;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 18 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TimerComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TimerComponent cmp = (TimerComponent)source;
		Date endDate = cmp.getEndDate();

		sb.append("<strong id='o_c").append(cmp.getDispatchID()).append("'>");
		if(endDate != null && cmp.isVisible()) {
			long remaingTime = endDate.getTime() - new Date().getTime();
			if(remaingTime > 0) {
				double hours = remaingTime / (60d * 60d * 1000d);
				long lhours = (long)hours;
				double minutes = (hours - lhours) * 60d;
				String shours = toString(lhours);
				String sminutes = toString(Math.round(Math.ceil(minutes)));
				
				String url = ubu.getJavascriptURI();
	
				sb.append("<span class='o_timer_hours'>").append(shours).append("</span> ")
				  .append(translator.translate("msg.period.hours.short"))
				  .append(" <span class='o_timer_minutes'>").append(sminutes).append("</span> ")
				  .append(translator.translate("msg.period.minutes.short"));
				sb.append("<script>")
				  .append("jQuery(function() {\n")
				  .append("  jQuery('#o_c").append(cmp.getDispatchID()).append("').ooTimer({\n")
				  .append("    remainingTime: ").append(remaingTime).append(",\n")
				  .append("    endUrl: '").append(url).append("',")
				  .append("    csrfToken: '").append(renderer.getCsrfToken()).append("'")
				  .append("  })\n")
				  .append("});\n")
				  .append("</script>");
			}
		}
		sb.append("</strong>");
	}
	
	private String toString(long time) {
		return (time < 10 ? "0" : "") + time;
		
	}
}
