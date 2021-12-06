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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SchedulerController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private JobTriggerDataModel tableModel;
	
	@Autowired
	private Scheduler scheduler;
	
	public SchedulerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "scheduler");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JobCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JobCols.previousFireTime, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JobCols.nextFireTime, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JobCols.runningJobs));
	
		tableModel = new JobTriggerDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 72, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "SchedulerJobsList");
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(250);
	}
	
	private void loadModel() {
		
		List<JobTriggerRow> rows = new ArrayList<>(32);
		
		try {
			List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
			List<String> jobGroupNames = scheduler.getJobGroupNames();
			for(String jobGroupName:jobGroupNames) {
				for (JobKey jobKey:scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
					String name = jobKey.getName();
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
					
					int executing = 0;
					for(JobExecutionContext executingJob:executingJobs) {
						Trigger trigger = executingJob.getTrigger();
						if(trigger != null && trigger.getJobKey() != null && jobKey.equals(trigger.getJobKey())) {
							executing++;
						}
					}

					for(Trigger trigger:triggers) {
						String expression = null;
						if(trigger instanceof CronTrigger) {
							expression = ((CronTrigger)trigger).getCronExpression();
						}
						rows.add(new JobTriggerRow(name, trigger.getPreviousFireTime(), trigger.getNextFireTime(), executing, expression));
					}
				}
			}	
		} catch (SchedulerException e) {
			logError("", e);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class JobTriggerRow {
		
		private final String jobKeyName;
		private final Date previousFireTime;
		private final Date nextFireTime;
		private final int runningJobs;
		private final String expression;
		
		public JobTriggerRow(String jobKeyName, Date previousFireTime, Date nextFireTime, int runningJobs, String expression) {
			this.jobKeyName = jobKeyName;
			this.previousFireTime = previousFireTime;
			this.nextFireTime = nextFireTime;
			this.runningJobs = runningJobs;
			this.expression = expression;
		}
		
		public String getName() {
			return jobKeyName;
		}

		public Date getPreviousFireTime() {
			return previousFireTime;
		}

		public Date getNextFireTime() {
			return nextFireTime;
		}
		
		public int getRunningJobs() {
			return runningJobs;
		}
		
		public String getTriggerExpression() {
			return expression;
		}
	}
	

	private static class JobTriggerDataModel extends DefaultFlexiTableDataModel<JobTriggerRow>
		implements SortableFlexiTableDataModel<JobTriggerRow> {
		
		private final Locale locale;
		
		public JobTriggerDataModel(FlexiTableColumnModel columnModel, Locale locale) {
			super(columnModel);
			this.locale = locale;
		}	

		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<JobTriggerRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
				super.setObjects(views);
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			JobTriggerRow jobRow = getObject(row);
			return getValueAt(jobRow, col);
		}

		@Override
		public Object getValueAt(JobTriggerRow row, int col) {
			switch(JobCols.values()[col]) {
				case name: return row.getName();
				case previousFireTime: return row.getPreviousFireTime();
				case nextFireTime: return row.getNextFireTime();
				case runningJobs: return row.getRunningJobs();
				case triggerExpression: return row.getTriggerExpression();
				default: return "ERROR";
			}
		}
	}
	
	public enum JobCols implements FlexiSortableColumnDef {
		name("table.header.name"),
		previousFireTime("table.header.previous.fire.time"),
		nextFireTime("table.header.next.fire.time"),
		runningJobs("table.header.running"),
		triggerExpression("table.header.trigger");
		
		private final String i18nKey;
		
		private JobCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
