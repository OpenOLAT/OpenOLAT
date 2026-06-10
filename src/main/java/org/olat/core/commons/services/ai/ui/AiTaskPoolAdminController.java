/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.ui;

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.manager.AiTaskExecutorService;
import org.olat.core.commons.services.ai.manager.AiTaskExecutorService.AiTaskQueueStats;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Admin form for the two dedicated AI task pools: configure the number of
 * worker threads per pool (per node) and show a live snapshot of running
 * and waiting jobs. The right pool sizes depend on the infrastructure
 * behind the configured AI provider — cloud APIs tolerate many parallel
 * calls, a single self-hosted GPU saturates at a handful.
 *
 * Initial date: 2026-06-10<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiTaskPoolAdminController extends FormBasicController {

	private IntegerElement interactivePoolSizeEl;
	private IntegerElement batchPoolSizeEl;
	private StaticTextElement interactiveStatsEl;
	private StaticTextElement batchStatsEl;
	private FormLink refreshStatsLink;

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiTaskExecutorService aiTaskExecutorService;

	public AiTaskPoolAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.task.pool.title");
		setFormDescription("ai.task.pool.desc");

		interactivePoolSizeEl = uifactory.addIntegerElement("ai.task.pool.interactive",
				"ai.task.pool.interactive", aiModule.getAiTaskPoolInteractiveSize(), formLayout);
		interactivePoolSizeEl.setHelpTextKey("ai.task.pool.interactive.help", null);
		interactivePoolSizeEl.setMandatory(true);

		batchPoolSizeEl = uifactory.addIntegerElement("ai.task.pool.batch",
				"ai.task.pool.batch", aiModule.getAiTaskPoolBatchSize(), formLayout);
		batchPoolSizeEl.setHelpTextKey("ai.task.pool.batch.help", null);
		batchPoolSizeEl.setMandatory(true);

		interactiveStatsEl = uifactory.addStaticTextElement("ai.task.stats.interactive",
				"ai.task.stats.interactive", "", formLayout);
		batchStatsEl = uifactory.addStaticTextElement("ai.task.stats.batch",
				"ai.task.stats.batch", "", formLayout);
		updateStats();

		refreshStatsLink = uifactory.addFormLink("ai.task.stats.refresh", formLayout, Link.BUTTON_SMALL);
		refreshStatsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");

		uifactory.addFormSubmitButton("save", formLayout);
	}

	private void updateStats() {
		AiTaskQueueStats interactive = aiTaskExecutorService.getInteractiveStats();
		AiTaskQueueStats batch = aiTaskExecutorService.getBatchStats();
		interactiveStatsEl.setValue(formatStats(interactive));
		batchStatsEl.setValue(formatStats(batch));
	}

	private String formatStats(AiTaskQueueStats stats) {
		return translate("ai.task.stats.value",
				Integer.toString(stats.running()),
				Integer.toString(stats.waiting()),
				Integer.toString(stats.poolSize()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == refreshStatsLink) {
			updateStats();
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validatePoolSize(interactivePoolSizeEl);
		allOk &= validatePoolSize(batchPoolSizeEl);
		return allOk;
	}

	private boolean validatePoolSize(IntegerElement el) {
		el.clearError();
		if (el.getIntValue() < 1 || el.getIntValue() > 64) {
			el.setErrorKey("ai.task.pool.error.range");
			return false;
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		aiModule.setAiTaskPoolInteractiveSize(interactivePoolSizeEl.getIntValue());
		aiModule.setAiTaskPoolBatchSize(batchPoolSizeEl.getIntValue());
		logAudit("AI task pools resized: interactive=" + interactivePoolSizeEl.getIntValue()
				+ " batch=" + batchPoolSizeEl.getIntValue());
		updateStats();
	}
}
