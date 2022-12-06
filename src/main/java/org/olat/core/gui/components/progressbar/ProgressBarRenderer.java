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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.progressbar;

import static org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment.left;
import static org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment.right;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Initial Date: Feb 2, 2004 A <b>ChoiceRenderer </b> is
 * 
 * @author Andreas Ch. Kapp
 */
public class ProgressBarRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput target, Component source, URLBuilder urlBuilder, Translator translator,
			RenderResult renderResult, String[] args) {

		ProgressBar ubar = (ProgressBar) source;
		if(ubar.getProgressCallback() != null) {
			ProgressBarCallback callback = ubar.getProgressCallback();
			ubar.setActual(callback.getActual());
			ubar.setMax(callback.getMax());
		}
		
		boolean renderLabels = (args == null);
		float percent = 100;
		if (!ubar.getIsNoMax()) {
			percent = 100 * ubar.getActual() / ubar.getMax();
		}
		if (percent < 0) {
			percent = 0;
		}
		if (percent > 100) {
			percent = 100;
		}
		
		String compId = "o_c" + ubar.getDispatchID();	
		target.append("<span class='o_progress ")
			.append("o_progress_inline ", ProgressBar.RenderSize.inline.equals(ubar.getRenderSize()))
			.append(ubar.getCssClass(), StringHelper.containsNonWhitespace(ubar.getCssClass()))
			.append("' id='").append(compId).append("' ")
			.append(">");
		
		if (ProgressBar.RenderStyle.horizontal.equals(ubar.getRenderStyle())) {
			renderHorizontal(target, ubar, renderLabels, percent);			
		} else {
			renderRadial(target, ubar, renderLabels, percent);
		}
		
		if (right.equals(ubar.getLabelAlignment()) ) {
			target.append("<span class='o_progress_label_wrapper'>");
			renderLabel(target, ubar);
			target.append("</span>");
		}

		target.append("</span>");

	}

	private void renderHorizontal(StringOutput target, ProgressBar ubar, boolean renderLabels, float percent) {
		target.append("<span class='progress");
		target.append(ubar.getCssClass(), StringHelper.containsNonWhitespace(ubar.getCssClass()));
		
		// medium is default style
		if (ProgressBar.RenderSize.inline.equals(ubar.getRenderSize())) {
			target.append(" o_progress-inline");
		} else if (ProgressBar.RenderSize.small.equals(ubar.getRenderSize())) {
			target.append(" o_progress-sm");
		} else if (ProgressBar.RenderSize.large.equals(ubar.getRenderSize())) {
			target.append(" o_progress-lg");
		}
		
		target.append("' style=\"width:")
			.append(ubar.getWidth())
			.append("%", "px", ubar.isWidthInPercent())
			.append(";\"><span class='progress-bar");
		
		// primary is default color
		if (ProgressBar.BarColor.info.equals(ubar.getBarColor())) {
			target.append(" progress-bar-info");
		} else if (ProgressBar.BarColor.success.equals(ubar.getBarColor())) {
			target.append(" progress-bar-success");
		} else if (ProgressBar.BarColor.warning.equals(ubar.getBarColor())) {
			target.append(" progress-bar-warning");
		} else if (ProgressBar.BarColor.danger.equals(ubar.getBarColor())) {
			target.append(" progress-bar-danger");
		}
		// animation works only with striped bars. 
		if (ubar.isProgressAnimationEnabled()) {
			target.append(" progress-bar-striped active");			
		}
		
		target.append("' style=\"width:")
			.append(Math.round(percent * ubar.getWidth() / 100))
			.append("%", "px", ubar.isWidthInPercent()).append("\" title=\"")
			.append(Math.round(percent))
			.append("%\"></span>");
		
				
		if (renderLabels && !ProgressBar.RenderSize.small.equals(ubar.getRenderSize())) {
			target.append("<span class='o_progress_label_wrapper'>");
			if (ubar.isPercentagesEnabled()) {
				target.append(Math.round(percent));
				target.append("%");				
			}
			if(left.equals(ubar.getLabelAlignment())) {
				target.append(" (", ubar.isPercentagesEnabled());
				renderLabel(target, ubar);
				target.append(")", ubar.isPercentagesEnabled());
			}
			target.append("</span>");
		}
		
		target.append("</span>"); // End progress bar
		
		if (renderLabels && ProgressBar.RenderSize.small.equals(ubar.getRenderSize())) {
			target.append("<span class='o_progress_label_wrapper'>");
			if (ubar.isPercentagesEnabled()) {
				target.append(Math.round(percent));
				target.append("%");				
			}
			if(left.equals(ubar.getLabelAlignment())) {
				target.append(" (", ubar.isPercentagesEnabled());
				renderLabel(target, ubar);
				target.append(")", ubar.isPercentagesEnabled());
			}
			target.append("</span>");
		}
	}
	
	
	private void renderLabel(StringOutput target, ProgressBar ubar) {
		target.append("<span class='o_progress_label'>");
		target.append(Math.round(ubar.getActual()));
		target.append("/");
		if (ubar.getIsNoMax()) {
			target.append("-");
		} else {
			target.append(Math.round(ubar.getMax()));
		}
		if (StringHelper.containsNonWhitespace(ubar.getUnitLabel())) {
			target.append(" ");
			target.append(ubar.getUnitLabel());
		}
		target.append("</span>");
		
		String info = ubar.getInfo();
		if(StringHelper.containsNonWhitespace(info)) {
			target.append("<span class='o_progress_info'>").append(info).append("</span>");
		}
	}

	private void renderRadial(StringOutput target, ProgressBar ubar, boolean renderLabels, float percent) {
		// 1) Wrapper
		target.append("<span class='radial-progress ");
		// Pie style
		if (ProgressBar.RenderStyle.pie.equals(ubar.getRenderStyle())) {
			target.append("radial-progress-pie ");
		}
		// medium is default style
		if (ProgressBar.RenderSize.inline.equals(ubar.getRenderSize())) {
			target.append("radial-progress-inline");
		} else if (ProgressBar.RenderSize.small.equals(ubar.getRenderSize())) {
			target.append("radial-progress-sm");
		} else if (ProgressBar.RenderSize.large.equals(ubar.getRenderSize())) {
			target.append("radial-progress-lg");
		}

		// primary is default color
		if (ProgressBar.BarColor.info.equals(ubar.getBarColor())) {
			target.append(" radial-progress-info");
		} else if (ProgressBar.BarColor.success.equals(ubar.getBarColor())) {
			target.append(" radial-progress-success");
		} else if (ProgressBar.BarColor.warning.equals(ubar.getBarColor())) {
			target.append(" radial-progress-warning");
		} else if (ProgressBar.BarColor.danger.equals(ubar.getBarColor())) {
			target.append(" radial-progress-danger");
		}
		// Current progress
		target.append("' data-progress='").append((ubar.isProgressAnimationEnabled() ? Math.round(0): Math.round(percent))).append("'");
		// Title for usability, in case to small or something
		target.append(" title='")
			.append(Math.round(percent))
			.append("% (").append(Math.round(ubar.getActual())).append("/");
		if (ubar.getIsNoMax()) {
			target.append("-");			
		} else {
			target.append(Math.round(ubar.getMax()));
		}
		if (StringHelper.containsNonWhitespace(ubar.getUnitLabel())) {
			target.append(" ").append(ubar.getUnitLabel());
		}
		target.append(")'>");
		
		// 2) Circle
		target.append("<svg viewBox='0 0 32 32'>")
			.append("<circle r='16' cx='16' cy='16' class='radial-bg'/>")
			.append("<circle r='16' cx='16' cy='16' class='radial-bar'/>")
			.append("</svg>");
		
		// 3) Text
		if (renderLabels && !ProgressBar.RenderSize.inline.equals(ubar.getRenderSize())) {
			target.append("<span class='percentage'><span class='centeredWrapper'>");
			if (ubar.isPercentagesEnabled()) {
				target.append("<span class='number'><span>");
				target.append(Math.round(percent));
				target.append("%</span></span>");
			}
			if (left.equals(ubar.getLabelAlignment())) {
				// Includes label and progress info
				target.append("<span class='addon text-muted'>");
				renderLabel(target, ubar);				
				target.append("</span>");
			} else {
				// Show progress info in circle anyway if available
				String info = ubar.getInfo();
				if(StringHelper.containsNonWhitespace(info)) {
					target.append("<span class='addon text-muted'><span class='o_progress_info'>").append(info).append("</span></span>");
				}				
			}
			target.append("</span></span>");
		}
		
		// Animation is done via JS, no stripes supported 
		if (ubar.isProgressAnimationEnabled()) {
			target.append("<script>")
				.append("setTimeout(function() {")
				.append("	o_animateRadialProgress('#o_c").append(ubar.getDispatchID()).append(" .radial-progress', ").append(Math.round(percent)).append(");")
				.append("});")
				.append("</script>");
		}
		
		// 4) End wrapper
		target.append("</span>");

	}
	
}