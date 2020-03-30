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
package org.olat.modules.coach.ui;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ProgressRenderer extends CustomCssCellRenderer {
	
	private final boolean neutral;
	private final Translator translator;
	
	public ProgressRenderer(boolean neutral, Translator translator) {
		this.neutral = neutral;
		this.translator = translator;
	}

	@Override
	protected String getCssClass(Object val) {
		return neutral ? "o_eff_statement_progress_neutral" : "o_eff_statement_rg";
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(renderer == null) {
			ProgressValue progress = (ProgressValue)val;
			int green = progress.getGreen();
			int total = progress.getTotal();
			sb.append(green).append(" / ").append(total);
		} else {
			super.render(sb, renderer, val, locale, alignment, action);
		}
	}

	@Override
	protected String getCellValue(Object val) {
		StringBuilder sb = new StringBuilder();
		
		if(val instanceof ProgressValue) {
			ProgressValue progress = (ProgressValue)val;
			int green = Math.round(100.0f * ((float)progress.getGreen() / (float)progress.getTotal()));
			String[] values = new String[]{ Integer.toString(progress.getGreen()), Integer.toString(progress.getTotal()) };
			String tooltip = translator.translate("tooltip.of", values);
			sb.append("<div class='progress' title='").append(tooltip).append("'>")
			  .append("<div class='progress-bar' role='progressbar' aria-valuenow='").append(progress.getGreen())
			  .append("' aria-valuemin='0' aria-valuemax='").append(progress.getTotal())
			  .append("' style='width: ").append(green).append("%;'/>")
			  .append("<span class='sr-only'>").append(green).append("%</span></div></div>");
			
			/*
			<div class="progress-bar" role="progressbar" aria-valuenow="${used}" aria-valuemin="0" aria-valuemax="100" style="width:${used};">
	    		<span class="sr-only">${used}%</span>
	    	</div>
	    	*/
		}
		return sb.toString();
	}

	@Override
	protected String getHoverText(Object val) {
		return null;
	}
}
