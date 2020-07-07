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
package org.olat.ims.qti21;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.olat.core.configuration.AbstractSpringModule;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.ims.qti21.repository.handlers.QTI21AssessmentTestHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21Module extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21Module.class);
	
	@Autowired
	private QTI21AssessmentTestHandler assessmentHandler;
	
	@Value("${qti21.math.assessment.extension.enabled:false}")
	private boolean mathAssessExtensionEnabled;
	@Value("${qti21.digital.signature.enabled:false}")
	private boolean digitalSignatureEnabled;
	@Value("${qti21.digital.signature.certificate:}")
	private String digitalSignatureCertificate;
	@Value("${qti21.digital.signature.certificate.password:}")
	private String digitalSignatureCertificatePassword;
	@Value("${qti21.correction.workflow:anonymous}")
	private String correctionWorkflow;
	@Value("${qti21.results.visible.after.correction:true}")
	private String resultsVisibleAfterCorrectionWorkflow;
	@Value("${qti21.import.encoding.fallback:}")
	private String importEncodingFallback;
	
	@Autowired
	public QTI21Module(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	/**
	 * Saxon is mandatory, JQTI need XSLT 2.0
	 * XsltFactoryUtilities.SAXON_TRANSFORMER_FACTORY_CLASS_NAME;
	 */
	@Override
	public void init() {
		RepositoryHandlerFactory.registerHandler(assessmentHandler, 10);
		initFromChangedProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		String mathExtensionObj = getStringPropertyValue("math.extension", true);
		if(StringHelper.containsNonWhitespace(mathExtensionObj)) {
			mathAssessExtensionEnabled = "enabled".equals(mathExtensionObj);
		}
		
		String correctionWorkflowObj = getStringPropertyValue("qti21.correction.workflow", true);
		if(StringHelper.containsNonWhitespace(correctionWorkflowObj)) {
			correctionWorkflow = CorrectionWorkflow.valueOf(correctionWorkflowObj).name();
		}
		
		resultsVisibleAfterCorrectionWorkflow = getStringPropertyValue("qti21.results.visible.after.correction", resultsVisibleAfterCorrectionWorkflow);
		
		String digitalSignatureObj = getStringPropertyValue("digital.signature", true);
		if(StringHelper.containsNonWhitespace(digitalSignatureObj)) {
			digitalSignatureEnabled = "enabled".equals(digitalSignatureObj);
		}
		
		String digitalSignatureCertificateObj = getStringPropertyValue("qti21.digital.signature.certificate", true);
		if(StringHelper.containsNonWhitespace(digitalSignatureCertificateObj)) {
			digitalSignatureCertificate = digitalSignatureCertificateObj;
		}
		
		String digitalSignatureCertificatePasswordObj = getStringPropertyValue("qti21.digital.signature.certificate.password", true);
		if(StringHelper.containsNonWhitespace(digitalSignatureObj)) {
			digitalSignatureCertificatePassword = digitalSignatureCertificatePasswordObj;
		}
	}

	public boolean isMathAssessExtensionEnabled() {
		return mathAssessExtensionEnabled;
	}

	public void setMathAssessExtensionEnabled(boolean enabled) {
		mathAssessExtensionEnabled = enabled;
		setStringProperty("math.extension", enabled ? "enabled" : "disabled", true);
	}

	public CorrectionWorkflow getCorrectionWorkflow() {
		return StringHelper.containsNonWhitespace(correctionWorkflow)
				? CorrectionWorkflow.valueOf(correctionWorkflow) : CorrectionWorkflow.anonymous;
	}

	public void setCorrectionWorkflow(CorrectionWorkflow correctionWorkflow) {
		this.correctionWorkflow = correctionWorkflow.name();
		setStringProperty("qti21.correction.workflow", correctionWorkflow.name(), true);
	}
	
	public boolean isResultsVisibleAfterCorrectionWorkflow() {
		return "true".equals(resultsVisibleAfterCorrectionWorkflow);
	}
	
	public void setResultsVisibleAfterCorrectionWorkflow(boolean visible) {
		resultsVisibleAfterCorrectionWorkflow = visible ? "true" : "false";
		setStringProperty("qti21.results.visible.after.correction", resultsVisibleAfterCorrectionWorkflow, true);
	}

	public boolean isDigitalSignatureEnabled() {
		return digitalSignatureEnabled;
	}

	public void setDigitalSignatureEnabled(boolean enabled) {
		this.digitalSignatureEnabled = enabled;
		setStringProperty("digital.signature", enabled ? "enabled" : "disabled", true);
	}

	public String getDigitalSignatureCertificate() {
		return digitalSignatureCertificate;
	}
	
	public File getDigitalSignatureCertificateFile() {
		File file = new File(digitalSignatureCertificate);
		if(!file.isAbsolute()) {
			String userDataDirectory = WebappHelper.getUserDataRoot();
			file = Paths.get(userDataDirectory, "system", "configuration", digitalSignatureCertificate).toFile();
		}
		return file;
	}
	
	public void setDigitalSignatureCertificateFile(File file, String filename) {
		try {
			String userDataDirectory = WebappHelper.getUserDataRoot();
			File newFile = Paths.get(userDataDirectory, "system", "configuration", filename).toFile();
			String newName = FileUtils.rename(newFile);
			File uniqueFile = Paths.get(userDataDirectory, "system", "configuration", newName).toFile();
			Files.copy(file.toPath(), uniqueFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			digitalSignatureCertificate = uniqueFile.getName();
			setStringProperty("qti21.digital.signature.certificate", digitalSignatureCertificate, true);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public String getDigitalSignatureCertificatePassword() {
		return digitalSignatureCertificatePassword;
	}

	public void setDigitalSignatureCertificatePassword(String digitalSignatureCertificatePassword) {
		this.digitalSignatureCertificatePassword = digitalSignatureCertificatePassword;
		setStringProperty("qti21.digital.signature.certificate.password", digitalSignatureCertificatePassword, true);
	}
	
	public String getImportEncodingFallback() {
		return importEncodingFallback;
	}

	public enum CorrectionWorkflow {
		anonymous,
		named
	}
}
