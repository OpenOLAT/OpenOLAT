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

import java.util.List;

import org.olat.admin.sysinfo.manager.ThreadInfosManager;
import org.olat.admin.sysinfo.model.ThreadView;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 19.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JavaThreadsController extends BasicController {

	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	
	private final ThreadInfosManager threadInfosManager;
	
	public JavaThreadsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		threadInfosManager = CoreSpringFactory.getImpl(ThreadInfosManager.class);
		
		mainVC = createVelocityContainer("jvm_threads");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setPageingEnabled(false);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.name.i18nKey(), Cols.name.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.state.i18nKey(), Cols.state.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.cpuPercent.i18nKey(), Cols.cpuPercent.ordinal(), null, getLocale()));		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.cpuTime.i18nKey(), Cols.cpuTime.ordinal(), null, getLocale()));
		listenTo(tableCtr);
		loadModel();
		mainVC.put("threads", tableCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private void loadModel() {
		List<ThreadView> threads = threadInfosManager.getThreadViews();
		ThreadsDataModel model = new ThreadsDataModel(threads);
		tableCtr.setTableDataModel(model);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private enum Cols {
		name("java.thread.name"),
		state("java.thread.alive"),
		cpuPercent("java.thread.cpu.percent"),
		cpuTime("java.thread.cpu.time");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}

	private static class ThreadsDataModel extends DefaultTableDataModel<ThreadView> {
		public ThreadsDataModel(List<ThreadView> threads) {
			super(threads);
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public Object getValueAt(int row, int col) {
			ThreadView view = getObject(row);
			switch(Cols.values()[col]) {
				case name: return view.getName();
				case state: return view.getState().name();
				case cpuPercent: return view.getCpuUsagePercent();
				case cpuTime: {
					long timeInNanoSeconds = view.getCpuTime();
					return timeInNanoSeconds / (1000.0d * 1000.0d * 1000.0d);
				}
				default: return "ERROR";
			}
		}
	}
}