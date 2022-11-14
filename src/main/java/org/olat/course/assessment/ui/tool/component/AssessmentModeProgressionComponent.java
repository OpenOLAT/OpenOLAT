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
package org.olat.course.assessment.ui.tool.component;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.model.AssessmentModeStatistics;

/**
 * 
 * Initial date: 11 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeProgressionComponent extends FormBaseComponentImpl {
	
	private static final AssessmentModeProgressionRenderer RENDERER = new AssessmentModeProgressionRenderer();
	
	private final ProgressBar progressBar;
	private final AssessmentMode assessmentMode;
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	private int planned = -1;
	private int loggedIn = -1;
	private AssessmentMode.Status status;
	
	private final AssessmentModeProgressionItem element;
	
	public AssessmentModeProgressionComponent(String name, AssessmentMode assessmentMode,
			AssessmentModeProgressionItem element, Translator translator) {
		super(name);
		setTranslator(translator);
		this.assessmentMode = assessmentMode;
		this.element = element;
		status = assessmentMode.getStatus();
		assessmentModeCoordinationService = CoreSpringFactory.getImpl(AssessmentModeCoordinationService.class);
		progressBar = new ProgressBar(name.concat("_progress"));
		progressBar.setRenderSize(RenderSize.inline);
		progressBar.setRenderStyle(RenderStyle.pie);
		progressBar.setLabelAlignment(LabelAlignment.none);
		progressBar.setPercentagesEnabled(false);
		progressBar.setDomReplacementWrapperRequired(false);
		setDomReplacementWrapperRequired(false);
	}
	
	@Override
	public FormItem getFormItem() {
		return element;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public String getInformations() {
		String i18nKey = status == Status.none || status == Status.leadtime ? "assessment.progress.wait.infos" : "assessment.progress.started.infos";
		return getTranslator().translate(i18nKey, Integer.toString(planned), Integer.toString(loggedIn));
	}
	
	public void setMax(int i) {
		planned = i;
		progressBar.setMax(i);
	}
	
	public void setActual(int i) {
		loggedIn = i;
		progressBar.setActual(i);
	}

	@Override
	public boolean isDirty() {
		boolean dirty = super.isDirty();
		AssessmentModeStatistics stats = assessmentModeCoordinationService.getStatistics(assessmentMode);
		if(planned != stats.getNumPlanned()) {
			planned = stats.getNumPlanned();
			progressBar.setMax(planned);
			dirty |= true;
		}
		
		if(loggedIn != stats.getNumInOpenOlat()) {
			loggedIn = stats.getNumInOpenOlat();
			progressBar.setActual(loggedIn);
			dirty |= true;
		}
		
		if(status != stats.getStatus()) {
			status = stats.getStatus();
			dirty |= true;
		}
		
		return dirty;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	

}
