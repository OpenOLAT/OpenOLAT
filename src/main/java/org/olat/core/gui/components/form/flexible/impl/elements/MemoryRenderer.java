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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 16.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemoryRenderer extends DefaultComponentRenderer {
	
	private static final String[] bars = new String[]{ "progress-bar-success", "progress-bar-warning", "progress-bar-info" };

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		MemoryComponent cmp = (MemoryComponent)source;
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		
		sb.append("<div class='progress' ");
		if(!cmp.isDomReplacementWrapperRequired()) {
			sb.append(" id='o_c").append(cmp.getDispatchID()).append("'");
		}
		
		if(cmp.getMemoryType() == MemoryType.HEAP) {
			renderDetails(sb, memoryBean.getHeapMemoryUsage().getMax(), MemoryType.HEAP);
		} else if(cmp.getMemoryType() == MemoryType.NON_HEAP) {
			renderDetails(sb, memoryBean.getNonHeapMemoryUsage().getMax(), MemoryType.NON_HEAP);
		}
		sb.append("</div>");
	}
	
	private void renderDetails(StringOutput sb, long max, MemoryType type) {
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		
		long totalUsed = 0l;
		List<MemoryPoolMXBean> typedPools = new ArrayList<>(4);
		for(MemoryPoolMXBean pool:pools) {
			if(pool.getType() == type) {
				typedPools.add(pool);
				totalUsed += pool.getUsage().getUsed();
			}
		}
		
		String tooltip;
		if(max == -1) {
			tooltip = toMB(totalUsed) + "MB";
		} else {
			tooltip = toMB(totalUsed) + "MB / " + toMB(max) + "MB";
			totalUsed = max;
		}
		
		sb.append(" title=\"").append(tooltip).append("\">");
		int count = 0;
		long totalUsedPercent = 0l;
		for(MemoryPoolMXBean pool:typedPools) {
			String name = pool.getName();
			long used = pool.getUsage().getUsed();
			long usedMB = toMB(used);
			long usedPercent = toPercent(used, totalUsed);
			totalUsedPercent += usedPercent;
			if(totalUsedPercent > 100l) {
				//never more than 100%
				usedPercent = usedPercent - (totalUsedPercent - 100l);
			}
			
			sb.append("<div class='progress-bar ").append(bars[count++ % bars.length]).append("' role='progressbar' ")
			  .append(" aria-valuenow='").append(usedMB).append("'")
			  .append(" aria-valuemin='0' style='width:").append(usedPercent).append("%;'><span")
			  .append(" title=\"").append(usedMB).append("MB (").append(name).append(")\"")
			  .append(">").append(usedMB).append("MB (").append(name).append(")</span></div>");
		}
	}
	
	private final long toMB(long val) {
		return val / (1024 * 1024);
	}
	
	private final long toPercent(long used, long max) {
		double ratio = (double)used / (double)max;
		double percent = ratio * 100.0d;
		return Math.round(percent);
	}
}
