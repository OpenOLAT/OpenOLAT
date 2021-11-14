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
package org.olat.admin.sysinfo;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 19.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JavaMemoryController extends BasicController {

	private final Link gcButton;
	private final VelocityContainer mainVC;
	
	public JavaMemoryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("jvm_memory");
		gcButton = LinkFactory.createButton("run.gc", mainVC, this);

		loaddModel();
		putInitialPanel(mainVC);
	}
	
	private void loaddModel() {
		mainVC.contextPut("memory", getMemoryInfos());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == gcButton){
			Runtime.getRuntime().gc();
			getWindowControl().setInfo("Garbage collection done.");
			loaddModel();
		}
	}
	
	private String getMemoryInfos() {
		Runtime r = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder();
		appendFormattedKeyValue(sb, "Processors", new Integer(r.availableProcessors()));
		appendFormattedKeyValue(sb, "Total Memory", Formatter.formatBytes(r.totalMemory()));
		appendFormattedKeyValue(sb, "Free Memory", Formatter.formatBytes(r.freeMemory()));
		appendFormattedKeyValue(sb, "Max Memory", Formatter.formatBytes(r.maxMemory()));
		
		sb.append("<br />Detailed Memory Information (Init/Used/Max)<br/> ");
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		for(MemoryPoolMXBean item:pools) {
		    String name = item.getName();
		    MemoryType type = item.getType();
		    appendFormattedKeyValue(sb, name, " Type: " + type);
		    MemoryUsage usage = item.getUsage();
		    appendFormattedKeyValue(sb, "Usage", Formatter.formatBytes(usage.getInit()) + "/" + Formatter.formatBytes(usage.getUsed()) + "/" + Formatter.formatBytes(usage.getMax()));
		    MemoryUsage peak = item.getPeakUsage();
		    appendFormattedKeyValue(sb, "Peak", Formatter.formatBytes(peak.getInit()) + "/" + Formatter.formatBytes(peak.getUsed()) + "/" + Formatter.formatBytes(peak.getMax()));
		    MemoryUsage collections = item.getCollectionUsage();
		    if (collections!= null){
		    	appendFormattedKeyValue(sb, "Collections", Formatter.formatBytes(collections.getInit()) + "/" + Formatter.formatBytes(collections.getUsed()) + "/" + Formatter.formatBytes(collections.getMax()));
		    }
		    sb.append("<hr/>");
		}
		
		return sb.toString();
	}
	
	private void appendFormattedKeyValue(StringBuilder sb, String key, Object value) {
		sb.append("&nbsp;&nbsp;&nbsp;<b>");
		sb.append(key);
		sb.append(":</b>&nbsp;");
		sb.append(value);
		sb.append("<br />");
	}
}