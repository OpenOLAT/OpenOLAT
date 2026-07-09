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
package org.olat.repository.ui;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;

import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.AutomaticLifecycleService;
import org.olat.repository.RepositoryEntryLifeCycleValue;
import org.olat.repository.RepositoryEntryLifeCycleValue.RepositoryEntryLifeCycleUnit;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.AutomaticLifecycleJob;
import org.olat.repository.model.AutomaticLifecycleInfos;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryLifecycleAdminController extends FormBasicController {
	
	private String[] onKeys = new String[]{ "on" };
	private static final String[] unitKeys = new String[]{
			RepositoryEntryLifeCycleUnit.day.name(), RepositoryEntryLifeCycleUnit.week.name(),
			RepositoryEntryLifeCycleUnit.month.name(), RepositoryEntryLifeCycleUnit.year.name()
		};
	
	private static final JobKey LIFECYCLE_KJOB_KEY = JobKey.jobKey("automaticLifecycleJob");
	
	private FormLink stopButton;
	private MultipleSelectionElement notificationEl;
	private MultipleSelectionElement toCloseEl;
	private MultipleSelectionElement toDeleteEl;
	private MultipleSelectionElement toDefinitivelyDeleteEl;
	private TextElement closeValueEl;
	private TextElement deleteValueEl;
	private TextElement definitivelyDeleteValueEl;
	private SingleSelection closeUnitEl;
	private SingleSelection deleteUnitEl;
	private SingleSelection definitivelyDeleteUnitEl;
	private FormLayoutContainer closeRuleCont;
	private FormLayoutContainer deleteRuleCont;
	private FormLayoutContainer processRunCont;
	private FormLayoutContainer configurationOverviewCont;
	private FormLayoutContainer definitivelyDeleteRuleCont;
	
	private WidgetGroup widgetGroup;
	private FigureWidget closeWidget;
	private ProgressBar closeProgressBar;
	private FigureWidget deleteWidget;
	private ProgressBar deleteProgressBar;
	private FigureWidget definitivelyDeleteWidget;
	private ProgressBar definitivelyDeleteProgressBar;
	
	private CloseableModalController cmc;
	private ConfirmChangeLifecycleController confirmCtrl;
	
	private Date nextFire;
	private AutomaticLifecycleInfos nextFireInfos;
	
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private AutomaticLifecycleService lifecycleService;
	
	public RepositoryLifecycleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		nextFire = getJobDetails().nextExecution();
		nextFireInfos = lifecycleService.getLifecycleInfos(nextFire);

		initForm(ureq);
		updateDashboard();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initConfigurationOverviewForm(formLayout);
		initProcessRunForm(formLayout);
		
		FormLayoutContainer lifecycleCont = uifactory.addDefaultFormLayout("leave", null, formLayout);
		lifecycleCont.setFormTitle(translate("repository.admin.lifecycle.title"));

		String id = Long.toString(CodeHelper.getRAMUniqueID());
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/date_rule.html";
		String[] unitValues = new String[] {
				translate(RepositoryEntryLifeCycleUnit.day.name()), translate(RepositoryEntryLifeCycleUnit.week.name()),
				translate(RepositoryEntryLifeCycleUnit.month.name()), translate(RepositoryEntryLifeCycleUnit.year.name())
			};
		
		initCloseForm(id, page, unitValues, lifecycleCont);
		initDeleteForm(id, page, unitValues, lifecycleCont);
		initDefinitivelyDeleteForm(id, page, unitValues, lifecycleCont);
		
		FormLayoutContainer notificationsCont = FormLayoutContainer.createDefaultFormLayout("notis", getTranslator());
		notificationsCont.setFormTitle(translate("repository.admin.lifecycle.notifications.title"));
		formLayout.add(notificationsCont);
		notificationsCont.setRootForm(mainForm);
		
		boolean notification = repositoryModule.isLifecycleNotificationByCloseDeleteEnabled();
		String[] notificationValues = new String[] { translate("repository.admin.lifecycle.notifications.enabled") };
		notificationEl = uifactory.addCheckboxesHorizontal("repository.admin.lifecycle.notifications", notificationsCont, onKeys, notificationValues);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		if(notification) {
			notificationEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		notificationsCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private void initConfigurationOverviewForm(FormItemContainer formLayout) {
		String page = velocity_root + "/lifecycle_dashboard.html";
		configurationOverviewCont = uifactory.addCustomFormLayout("configurationOverview", null, page, formLayout);
		configurationOverviewCont.setFormLayout("nolayout");
		
		JobInfos jobDetails = getJobDetails();
		String executionTime = jobDetails.cronExpression();
		// If it's the default configuration, we show it in a human readable way
		if("0 45 5 * * ?".equals(executionTime)) {
			String time = Formatter.getInstance(getLocale()).formatTimeShort(LocalTime.of(5, 45));
			executionTime = translate("configuration.cron.daily", time);
		}
		configurationOverviewCont.contextPut("executionItem", executionTime);
	}
	
	private void initProcessRunForm(FormItemContainer formLayout) {
		String page = velocity_root + "/lifecycle_process_run.html";
		processRunCont = uifactory.addCustomFormLayout("processRun", null, page, formLayout);
		processRunCont.setFormLayout("nolayout");
		
		widgetGroup = new ProgressWidgetGroup("widgets");
		processRunCont.add("widgets", new ComponentWrapperElement(widgetGroup));
		
		int numOfEntriesToClose = nextFireInfos.getTotalToClose();
		int numOfEntriesToDelete = nextFireInfos.getTotalToDelete();
		int numOfEntriesToDefinitivelyDelete = nextFireInfos.getTotalToDefinitivelyDelete();
		
		closeWidget = WidgetFactory.createFigureWidget("close", processRunCont.getFormItemComponent(),
				translate("process.close.title"), "o_icon_repo_status_closed");
		closeWidget.setValue("0");
		closeWidget.setDesc(translate("process.of", Integer.toString(numOfEntriesToClose)));
		widgetGroup.add(closeWidget);
		closeProgressBar = initProgress(closeWidget, 0, numOfEntriesToClose);
		
		deleteWidget = WidgetFactory.createFigureWidget("delete", processRunCont.getFormItemComponent(),
				translate("process.delete.title"), "o_icon_repo_status_trash");
		deleteWidget.setValue("0");
		deleteWidget.setDesc(translate("process.of", Integer.toString(numOfEntriesToDelete)));
		widgetGroup.add(deleteWidget);
		deleteProgressBar = initProgress(deleteWidget, 0, numOfEntriesToDelete);

		definitivelyDeleteWidget = WidgetFactory.createFigureWidget("definitivelyDelete", processRunCont.getFormItemComponent(),
				translate("process.definitively.delete.title"), "o_icon_repo_status_deleted");
		definitivelyDeleteWidget.setValue("0");
		definitivelyDeleteWidget.setDesc(translate("process.of", Integer.toString(numOfEntriesToDefinitivelyDelete)));
		widgetGroup.add(definitivelyDeleteWidget);
		definitivelyDeleteProgressBar = initProgress(definitivelyDeleteWidget, 0, numOfEntriesToDefinitivelyDelete);
		
		stopButton = uifactory.addFormLink("stop.automatic.lifecycle.job", processRunCont, Link.BUTTON_LARGE);
		stopButton.setIconLeftCSS("o_icon o_icon-fw o_icon_cancel");
		stopButton.setElementCssClass("btn-danger");
		stopButton.setEnabled(false);
	}

	private ProgressBar initProgress(FigureWidget widget, int progress, int max) {
		ProgressBar progressBar = new ProgressBar("scoreProgress", 100, progress, max, null);
		progressBar.setWidthInPercent(true);
		progressBar.setLabelAlignment(LabelAlignment.none);
		progressBar.setRenderSize(RenderSize.small);
		progressBar.setLabelMaxEnabled(false);
		widget.setAdditionalComp(progressBar);
		widget.setAdditionalCssClass("o_widget_progress");
		return progressBar;
	}
	
	private void initCloseForm(String id, String page, String[] unitValues, FormLayoutContainer lifecycleCont) {
		RepositoryEntryLifeCycleValue autoCloseValue = repositoryModule.getLifecycleAutoCloseValue();
		String[] toCloseValues = new String[] { translate("change.to.close.text") };
		toCloseEl = uifactory.addCheckboxesHorizontal("change.to.close", lifecycleCont, onKeys, toCloseValues);
		toCloseEl.addActionListener(FormEvent.ONCHANGE);
		if(autoCloseValue != null) {
			toCloseEl.select(onKeys[0], true);
		}
		
		closeRuleCont = uifactory.addCustomFormLayout("close.".concat(id), null, page, lifecycleCont);
		closeRuleCont.contextPut("ruleMsg", translate("after.course.end"));
		closeRuleCont.setVisible(toCloseEl.isAtLeastSelected(1));
		closeRuleCont.contextPut("prefix", "clo");
		
		String currentCloseValue = autoCloseValue == null ? null : Integer.toString(autoCloseValue.getValue());
		closeValueEl = uifactory.addTextElement("clo-value", null, 128, currentCloseValue, closeRuleCont);
		closeValueEl.setDomReplacementWrapperRequired(false);
		closeValueEl.setDisplaySize(3);

		closeUnitEl = uifactory.addDropdownSingleselect("clo-unit", null, closeRuleCont, unitKeys, unitValues, null);
		closeUnitEl.setDomReplacementWrapperRequired(false);
		selectUnitEl(closeUnitEl, autoCloseValue);
	}
	
	private void initDeleteForm(String id, String page, String[] unitValues, FormLayoutContainer lifecycleCont) {
		RepositoryEntryLifeCycleValue autoDeleteValue = repositoryModule.getLifecycleAutoDeleteValue();
		String[] toDeleteValues = new String[] { translate("change.to.delete.text") };
		toDeleteEl = uifactory.addCheckboxesHorizontal("change.to.delete", lifecycleCont, onKeys, toDeleteValues);
		toDeleteEl.addActionListener(FormEvent.ONCHANGE);
		if(autoDeleteValue != null) {
			toDeleteEl.select(onKeys[0], true);
		}
		
		deleteRuleCont = uifactory.addCustomFormLayout("delete.".concat(id), null, page, lifecycleCont);
		deleteRuleCont.contextPut("ruleMsg", translate("after.course.end"));
		deleteRuleCont.setVisible(toDeleteEl.isAtLeastSelected(1));
		deleteRuleCont.contextPut("prefix", "del");
		
		String currentDeleteValue = autoDeleteValue == null ? null : Integer.toString(autoDeleteValue.getValue());
		deleteValueEl = uifactory.addTextElement("del-value", null, 128, currentDeleteValue, deleteRuleCont);
		deleteValueEl.setDomReplacementWrapperRequired(false);
		deleteValueEl.setDisplaySize(3);

		deleteUnitEl = uifactory.addDropdownSingleselect("del-unit", null, deleteRuleCont, unitKeys, unitValues, null);
		deleteUnitEl.setDomReplacementWrapperRequired(false);
		selectUnitEl(deleteUnitEl, autoDeleteValue);
	}
	
	private void initDefinitivelyDeleteForm(String id, String page, String[] unitValues, FormLayoutContainer lifecycleCont) {
		RepositoryEntryLifeCycleValue autoDefinitivelyDeleteValue = repositoryModule.getLifecycleAutoDefinitivelyDeleteValue();
		String[] toDeleteValues = new String[] { translate("change.to.delete.definitively.text") };
		toDefinitivelyDeleteEl = uifactory.addCheckboxesHorizontal("change.to.delete.definitively", lifecycleCont, onKeys, toDeleteValues);
		toDefinitivelyDeleteEl.addActionListener(FormEvent.ONCHANGE);
		if(autoDefinitivelyDeleteValue != null) {
			toDefinitivelyDeleteEl.select(onKeys[0], true);
		}
		
		definitivelyDeleteRuleCont = uifactory.addCustomFormLayout("def-delete.".concat(id), null, page, lifecycleCont);
		definitivelyDeleteRuleCont.contextPut("ruleMsg", translate("after.course.deletion"));
		definitivelyDeleteRuleCont.setVisible(toDefinitivelyDeleteEl.isAtLeastSelected(1));
		definitivelyDeleteRuleCont.contextPut("prefix", "def-del");
		
		String currentDeleteValue = autoDefinitivelyDeleteValue == null ? null : Integer.toString(autoDefinitivelyDeleteValue.getValue());
		definitivelyDeleteValueEl = uifactory.addTextElement("def-del-value", null, 128, currentDeleteValue, definitivelyDeleteRuleCont);
		definitivelyDeleteValueEl.setDomReplacementWrapperRequired(false);
		definitivelyDeleteValueEl.setDisplaySize(3);

		definitivelyDeleteUnitEl = uifactory.addDropdownSingleselect("def-del-unit", null, definitivelyDeleteRuleCont, unitKeys, unitValues, null);
		definitivelyDeleteUnitEl.setDomReplacementWrapperRequired(false);
		selectUnitEl(definitivelyDeleteUnitEl, autoDefinitivelyDeleteValue);
	}
	
	private void selectUnitEl(SingleSelection unitEl, RepositoryEntryLifeCycleValue currentUnit) {
		boolean selected = false;
		if(currentUnit != null && currentUnit.getUnit() != null) {
			String unit = currentUnit.getUnit().name();
			for(String unitKey:unitKeys) {
				if(unit.equals(unitKey)) {
					unitEl.select(unitKey, true);
					selected = true;
				}
			}
		}
		if(!selected) {
			unitEl.select(unitKeys[1], true);	
		}
	}
	
	private void updateDashboard() {
		boolean running = isJobRunning();
		stopButton.setEnabled(running);
		if(running) {
			stopButton.setElementCssClass("btn-danger");
		} else {
			stopButton.setElementCssClass("");
		}
		
		AutomaticLifecycleInfos infos = getLifecycleInfos();
		String autoClose = getStringValue(toCloseEl, closeValueEl, closeUnitEl);
		updateWidget(closeWidget, closeProgressBar, autoClose, infos.getClosed(), infos.getTotalToClose());
		String autoDelete = getStringValue(toDeleteEl, deleteValueEl, deleteUnitEl);
		updateWidget(deleteWidget, deleteProgressBar, autoDelete, infos.getDeleted(), infos.getTotalToDelete());
		String autoDefinitivelyDelete = getStringValue(toDefinitivelyDeleteEl, definitivelyDeleteValueEl, definitivelyDeleteUnitEl);
		updateWidget(definitivelyDeleteWidget, definitivelyDeleteProgressBar, autoDefinitivelyDelete, infos.getDefinitivelyDeleted(), infos.getTotalToDefinitivelyDelete());
		
		//
		JobInfos jobDetails = getJobDetails();
		Date nextExecution = jobDetails.nextExecution();
		String nextExecutionDateTime = nextExecution != null
				? Formatter.getInstance(getLocale()).formatDateAndTime(nextExecution)
				: null;
		if(nextFire != null && nextExecution != null && nextFire.compareTo(nextExecution) != 0) {
			nextFire = nextExecution;
			nextFireInfos = lifecycleService.getLifecycleInfos(nextExecution);
		}
		
		// List of sub-processes
		int courses = 0;
		StringBuilder activeSubProcesses = new StringBuilder();
		if(StringHelper.containsNonWhitespace(autoClose)) {
			activeSubProcesses.append(translate("process.close.title"));
			courses += nextFireInfos.getTotalToClose();
		}
		if(StringHelper.containsNonWhitespace(autoDelete)) {
			if(!activeSubProcesses.isEmpty()) activeSubProcesses.append(" | ");
			activeSubProcesses.append(translate("process.delete.title"));
			courses += nextFireInfos.getTotalToDelete();
		}
		if(StringHelper.containsNonWhitespace(autoDefinitivelyDelete)) {
			if(!activeSubProcesses.isEmpty()) activeSubProcesses.append(" | ");
			activeSubProcesses.append(translate("process.definitively.delete.title"));
			courses += nextFireInfos.getTotalToDefinitivelyDelete();
		}
		configurationOverviewCont.contextPut("subProcess", activeSubProcesses.toString());
		
		String nextFireI18nKey = courses == 1
				? "configuration.next.fire.details"
				: "configuration.next.fire.details.plural";
		configurationOverviewCont.contextPut("nextFire", translate(nextFireI18nKey, nextExecutionDateTime, Integer.toString(courses)));
		
		Date lastExecution = jobDetails.previousExecution();
		String lastExecutionDateTime = lastExecution != null
				? Formatter.getInstance(getLocale()).formatDateAndTime(lastExecution)
				: null;
		configurationOverviewCont.contextPut("lastFire", lastExecutionDateTime);
	}
	
	private void updateWidget(FigureWidget widget, ProgressBar progressBar, String config, int current, int max) {
		if(StringHelper.containsNonWhitespace(config)) {
			widget.setValue(Integer.toString(current));
			widget.setDesc(translate("process.of", Integer.toString(max)));
			progressBar.setActual(current);
			progressBar.setMax(max);
		} else {
			widget.setValue("-");
			widget.setDesc("");
			progressBar.setActual(0.0f);
			progressBar.setMax(0.0f);
		}
	}
	
	private JobInfos getJobDetails() {
		Date nextExecution = null;
		Date previousExecution = null;
		String cron = null;
		try {
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(LIFECYCLE_KJOB_KEY);
			if(triggers != null) {
				List<CronTrigger> cronTriggers = triggers.stream()
						.filter(t -> t instanceof CronTrigger)
						.map(CronTrigger.class::cast)
						.toList();
				if(!cronTriggers.isEmpty()) {
					CronTrigger trigger = cronTriggers.get(0);
					cron = trigger.getCronExpression();
					previousExecution = trigger.getPreviousFireTime();
					TriggerState state = scheduler.getTriggerState(trigger.getKey());
					if(state != TriggerState.PAUSED) {
						nextExecution = trigger.getNextFireTime();
					}
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
		return new JobInfos(previousExecution, nextExecution, cron);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateFormLogic(toCloseEl, closeValueEl, closeUnitEl);
		allOk &= validateFormLogic(toDeleteEl, deleteValueEl, deleteUnitEl);
		allOk &= validateFormLogic(toDefinitivelyDeleteEl, definitivelyDeleteValueEl, definitivelyDeleteUnitEl);
		
		RepositoryEntryLifeCycleValue autoClose = getValue(toCloseEl, closeValueEl, closeUnitEl);
		RepositoryEntryLifeCycleValue autoDelete = getValue(toDeleteEl, deleteValueEl, deleteUnitEl);
		if(autoDelete != null && autoClose != null && autoDelete.compareTo(autoClose) <= 0) {
			deleteValueEl.setErrorKey("error.lifecycle.after");
			allOk &= false;
		}

		return allOk;
	}
	
	protected boolean validateFormLogic(MultipleSelectionElement enableEl, TextElement textEl, SingleSelection unitEl) {
		boolean allOk = true;
		enableEl.clearError();
		textEl.clearError();
		unitEl.clearError();
		
		if(enableEl.isAtLeastSelected(1)) {
			String value = textEl.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer.parseInt(value);
				} catch (NumberFormatException e) {
					textEl.setErrorKey("form.error.nointeger");
					allOk &= false;
				}
			} else {
				textEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
			
			if(!unitEl.isOneSelected()) {
				textEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmCtrl) {
			if(event == Event.DONE_EVENT) {
				doCommitChanges();
				doStartJob();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(toCloseEl == source) {
			closeRuleCont.setVisible(toCloseEl.isAtLeastSelected(1));
		} else if(toDeleteEl == source) {
			deleteRuleCont.setVisible(toDeleteEl.isAtLeastSelected(1));
		} else if(toDefinitivelyDeleteEl == source) {
			definitivelyDeleteRuleCont.setVisible(toDefinitivelyDeleteEl.isAtLeastSelected(1));
		} else if(stopButton == source) {
			doStopJob();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String autoClose = getStringValue(toCloseEl, closeValueEl, closeUnitEl);
		String autoDelete = getStringValue(toDeleteEl, deleteValueEl, deleteUnitEl);
		String autoDefinitivelyDelete = getStringValue(toDefinitivelyDeleteEl, definitivelyDeleteValueEl, definitivelyDeleteUnitEl);
		if(StringHelper.containsNonWhitespace(autoClose) || StringHelper.containsNonWhitespace(autoDelete) || StringHelper.containsNonWhitespace(autoDefinitivelyDelete)) {
			doConfirmCommitChanges(ureq, autoClose, autoDelete, autoDefinitivelyDelete);
		} else {
			doCommitChanges();
		}
	}

	private void doConfirmCommitChanges(UserRequest ureq, String autoClose, String autoDelete, String autoDefinitivelyDelete) {
		confirmCtrl = new ConfirmChangeLifecycleController(ureq, getWindowControl(),
				translate("confirmation.lifecycle"),
				translate("confirmation.lifecycle.conf"),
				translate("confirmation.lifecycle.save"),
				translate("cancel"), autoClose, autoDelete, autoDefinitivelyDelete);
		listenTo(confirmCtrl);
		
		String title = translate("confirmation.lifecycle.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCommitChanges() {
		boolean interrupted = interruptJob();
		getLogger().info(Tracing.M_AUDIT, "Change repository automatic lifecycle configuration");
		
		String autoClose = getStringValue(toCloseEl, closeValueEl, closeUnitEl);
		repositoryModule.setLifecycleAutoClose(autoClose);
		String autoDelete = getStringValue(toDeleteEl, deleteValueEl, deleteUnitEl);
		repositoryModule.setLifecycleAutoDelete(autoDelete);
		String autoDefinitivelyDelete = getStringValue(toDefinitivelyDeleteEl, definitivelyDeleteValueEl, definitivelyDeleteUnitEl);
		repositoryModule.setLifecycleAutoDefinitivelyDelete(autoDefinitivelyDelete);
		boolean notification = notificationEl.isAtLeastSelected(1);
		repositoryModule.setLifecycleNotificationByCloseDeleteEnabled(notification);
		
		if(interrupted) {
			// Restart the job only if it was interrupted, if not running, wait until the next thick
			// Wait until events are propagated
			taskExecutorManager.schedule(new ResumeJob(), 2000);
			nextFire = new Date();
		}
		nextFireInfos = lifecycleService.getLifecycleInfos(nextFire);
		updateDashboard();
	}
	
	private String getStringValue(MultipleSelectionElement enableEl, TextElement textEl, SingleSelection unitEl) {
		RepositoryEntryLifeCycleValue val = getValue(enableEl, textEl, unitEl);
		return val == null ? "" : val.toString();
	}
	
	private RepositoryEntryLifeCycleValue getValue(MultipleSelectionElement enableEl, TextElement textEl, SingleSelection unitEl) {
		if(enableEl.isAtLeastSelected(1)) {
			try {
				String value = textEl.getValue();
				String unit = unitEl.getSelectedKey();
				return new RepositoryEntryLifeCycleValue(Integer.parseInt(value), RepositoryEntryLifeCycleUnit.valueOf(unit));
			} catch (NumberFormatException e) {
				logError("", e);
			}
		}
		return null;
	}
	
	private boolean isJobRunning() {
		try {
			return scheduler.getCurrentlyExecutingJobs().stream()
					.anyMatch(job -> LIFECYCLE_KJOB_KEY.equals(job.getJobDetail().getKey()));
		} catch (SchedulerException e) {
			logError("", e);
			return false;
		}
	}
	
	private AutomaticLifecycleInfos getLifecycleInfos() {
		AutomaticLifecycleInfos infos = getJobRunningInfos();
		if(infos == null) {
			infos = nextFireInfos;
		}
		if(infos == null) {
			infos = new AutomaticLifecycleInfos();
		}
		return infos;
	}
	
	private AutomaticLifecycleInfos getJobRunningInfos() {
		try {
			Optional<JobExecutionContext> context = scheduler.getCurrentlyExecutingJobs().stream()
					.filter(job -> LIFECYCLE_KJOB_KEY.equals(job.getJobDetail().getKey()))
					.findFirst();
			if(context.isPresent() && context.get().get(AutomaticLifecycleJob.PROCESSED_INFOS) instanceof AutomaticLifecycleInfos infos) {
				return infos;
			}
		} catch (SchedulerException e) {
			logError("", e);
		}
		return null;
	}
	
	/**
	 * Method will stop a running before starting a new one.
	 */
	private void doStartJob() {
		try {
			boolean isRunning = isJobRunning();
			if(isRunning) {
				interruptJob();
				taskExecutorManager.schedule(new ResumeJob(), 2000);
			} else {
				scheduler.triggerJob(LIFECYCLE_KJOB_KEY);
				getLogger().info(Tracing.M_AUDIT, "Start repository automatic lifecycle job");
			}
		} catch (SchedulerException e) {
			getLogger().error("", e);
		}
	}
	
	private void doStopJob() {
		if(isJobRunning()) {
			interruptJob();
		}
	}
	
	private boolean interruptJob() {
		try {
			if(scheduler.interrupt(LIFECYCLE_KJOB_KEY)) {
				getLogger().info(Tracing.M_AUDIT, "Interrupt repository automatic lifecycle job");
				return true;
			}
		} catch (UnableToInterruptJobException e) {
			getLogger().error("", e);
		}
		return false;
	}
	
	private record JobInfos(Date previousExecution, Date nextExecution, String cronExpression) {
		//
	}
	
	private class ResumeJob extends TimerTask {
		@Override
		public void run() {
			try {
				boolean isRunning = isJobRunning();
				if(!isRunning) {
					scheduler.triggerJob(LIFECYCLE_KJOB_KEY);
					getLogger().info(Tracing.M_AUDIT, "Resume repository automatic lifecycle job");
				}
			} catch (SchedulerException e) {
				getLogger().error("", e);
			}
		}
	}

	private class ProgressWidgetGroup extends WidgetGroup {
		
		public ProgressWidgetGroup(String name) {
			super(name);
		}
		
		@Override
		public boolean isDirty() {
			updateDashboard();
			widgetGroup.setDirty(true);
			return true;
		}
	}
}
