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
package org.olat.modules.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.forms.handler.EvaluationFormHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormsModule extends AbstractSpringModule implements ConfigOnOff {

	private static final OLog log = Tracing.createLoggerFor(EvaluationFormsModule.class);

	public static final String FORMS_ENABLED = "forms.enabled";
	
	@Autowired
	private EvaluationFormHandler formHandler;
	
	@Value("${forms.enabled:true}")
	private boolean enabled;
	
	@Value("${forms.file.upload.limit.mb:5,10,20,50}")
	private String fileUploadLimitsMB;
	private List<Long> orderedFileUploadLimitsKB = new ArrayList<>(4);
	private long maxFileUploadLimitsKB = FileElement.UPLOAD_UNLIMITED;
	@Value("${forms.report.max.sessions:100}")
	private int reportMaxSessions;
	
	@Autowired
	public EvaluationFormsModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(FORMS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		initFileUploadLimit();

		RepositoryHandlerFactory.registerHandler(formHandler, 40);
	}

	private void initFileUploadLimit() {
		String[] limits = fileUploadLimitsMB.split(",");
		for (String limit: limits) {
			if (StringHelper.containsNonWhitespace(limit)) {
				try {
					long limitKB = Long.parseLong(limit) * 1000;
					orderedFileUploadLimitsKB.add(limitKB);
				} catch (Exception e) {
					log.warn("The value '" + limit + "' for the property 'forms.file.upload.limit.mb' is no valid numver.");
				}
			}
		}
		Collections.sort(orderedFileUploadLimitsKB);
		maxFileUploadLimitsKB = orderedFileUploadLimitsKB.get(orderedFileUploadLimitsKB.size() - 1);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(FORMS_ENABLED, Boolean.toString(enabled), true);
	}

	public List<Long> getOrderedFileUploadLimitsKB() {
		return orderedFileUploadLimitsKB;
	}
	
	public long getMaxFileUploadLimitKB() {
		return maxFileUploadLimitsKB;
	}

	public int getReportMaxSessions() {
		return reportMaxSessions;
	}
	
}
