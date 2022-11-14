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
package org.olat.modules.assessment.ui.component;

import java.util.Locale;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;

/**
 * 
 * Initial date: 13 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningProgressComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new LearningProgressRenderer();
	
	private final Translator translator;
	
	private Boolean fullyAssessed;
	private AssessmentEntryStatus status;
	private float completion;
	
	private boolean chartVisible = true;
	private boolean labelVisible = true;
	
	private final LearningProgressItem element;
	
	public LearningProgressComponent(String name, Locale locale, LearningProgressItem element) {
		super(name);
		this.element = element;
		setDomReplacementWrapperRequired(false);
		translator = Util.createPackageTranslator(AssessedIdentityListController.class, locale);
		setTranslator(translator);
	}
	
	@Override
	public FormItem getFormItem() {
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public Boolean getFullyAssessed() {
		return fullyAssessed;
	}

	public void setFullyAssessed(Boolean fullyAssessed) {
		this.fullyAssessed = fullyAssessed;
		setDirty(true);
	}

	public AssessmentEntryStatus getStatus() {
		return status;
	}

	public void setStatus(AssessmentEntryStatus status) {
		this.status = status;
		setDirty(true);
	}

	public float getCompletion() {
		return completion;
	}

	public void setCompletion(float completion) {
		this.completion = completion;
		setDirty(true);
	}

	public void setCompletion(Double completion) {
		setCompletion(completion != null? completion.floatValue(): 0);
	}

	public boolean isChartVisible() {
		return chartVisible;
	}

	public void setChartVisible(boolean chartVisible) {
		this.chartVisible = chartVisible;
	}

	public boolean isLabelVisible() {
		return labelVisible;
	}

	public void setLabelVisible(Boolean labelVisible) {
		this.labelVisible = labelVisible;
	}

}
