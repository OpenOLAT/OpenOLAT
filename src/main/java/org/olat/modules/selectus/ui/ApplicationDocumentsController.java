/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.position.component.PreviewApplicationDocumentMapper;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationDocumentsController extends FormBasicController {
	
	private final String mapperBaseURL;
	private final String referenceMapperBaseURL;

	private final boolean preview;
	private final Position position;
	private final Application application;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public ApplicationDocumentsController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, RecruitingPositionSecurityCallback secCallback, boolean preview, Form rootForm) {
		super(ureq, wControl, "documents");
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.preview = preview;
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;

		String name = ApplicationDocumentsController.class.getSimpleName();
		Mapper mapper = application.getKey() == null && preview ? new PreviewApplicationDocumentMapper() : new ApplicationDocumentMapper(secCallback);
		mapperBaseURL = registerCacheableMapper(ureq, name, mapper);

		referenceMapperBaseURL = registerCacheableMapper(ureq, name + "_REF", new ReferenceDocumentMapper(application, position, secCallback));
		initForm(ureq);
	}
	
	public int getNumOfDocuments() {
		return getNumOfDocuments("documents") + getNumOfDocuments("otherDocuments")
			+ getNumOfDocuments("experts") + getNumOfDocuments("recommendations");
	}
	
	private int getNumOfDocuments(String variable) {
		int count = 0;
		if(flc.contextGet(variable) != null) {
			count += ((List<?>)flc.contextGet(variable)).size();
		}
		return count;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String fullName = RecruitingHelper.formatFullName(application, getTranslator());

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("mapperBaseURL", mapperBaseURL);
			layoutCont.contextPut("referenceMapperBaseURL", referenceMapperBaseURL);
			layoutCont.contextPut("fullName", StringHelper.escapeHtml(fullName));
			
			initDocumentsForm(layoutCont);
			initReferencesDocumentsForm(layoutCont);
		}
	}
		
	private void initDocumentsForm(FormLayoutContainer layoutCont) {	
		Set<DocumentEnum> documentsInCombinedFile = recruitingModule.getDocumentsInCombinedFile(position);
		Set<String> available = position.getAvailableDocuments();

		DocumentOption docCombinedOption = null;
		List<ApplicationDocument> documents = new ArrayList<>();
		List<ApplicationDocument> otherDocuments = new ArrayList<>();
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(!secCallback.canViewDocument(doc)) {
				continue;
			}
			if(DocumentEnum.combined == doc) {
				docCombinedOption = docOption;
				continue;
			}
			if(!available.contains(doc.name())) {
				continue;
			}
			
			Attachment attachment = doc.path(application);
			if(attachment != null) {
				String documentName = position.getDocumentName(doc, getLocale());
				if(!StringHelper.containsNonWhitespace(documentName)) {
					documentName = translate(doc.i18nKey());
				}
				StringBuilder linktext = new StringBuilder(documentName);
				if(attachment.getSize() != null) {
					String size = Formatter.formatBytes(attachment.getSize());
					linktext.append(" ").append(translate("edit.application.document.size", new String[]{ size }));
				}
				
				String normalizedDocumentName = RecruitingHelper.normalizeFilename(documentName).toLowerCase();
				if(StringHelper.containsNonWhitespace(attachment.getType())) {
					normalizedDocumentName += "." + attachment.getType();
				}
				String attachmentPseudoHash = attachment.getVersion() + "_" + (attachment.getSize() == null ? "0" : attachment.getSize());
				String relativePath = "/" + application.getKey() + "/" + doc.name() + "/" + attachmentPseudoHash + "/" + normalizedDocumentName + "?" + attachmentPseudoHash;
				if(!relativePath.endsWith(".pdf")
						&& !relativePath.endsWith(".xlsx") && !relativePath.endsWith(".docx")
						&& !relativePath.endsWith(".xls") && !relativePath.endsWith(".doc")
						&& !relativePath.endsWith(".jpg") && !relativePath.endsWith(".jpeg")) {
					relativePath += ".pdf";
				}
				ApplicationDocument document = new ApplicationDocument(linktext.toString(), relativePath);
				if(relativePath.endsWith(".jpg") || relativePath.endsWith(".jpeg")) {
					document.setImage(true);
				}
				if(documentsInCombinedFile.contains(doc)) {
					documents.add(document);
				} else {
					otherDocuments.add(document);
				}
			}
		}
		layoutCont.contextPut("documents", documents);
		layoutCont.contextPut("otherDocuments", otherDocuments);
		
		if(docCombinedOption != null && secCallback.canViewCombinedDocument()) {
			String combineText;
			Attachment attachment = DocumentEnum.combined.path(application);
			if(attachment != null) {
				combineText = translate("edit.application.document.combined.staff");
			} else {
				combineText = translate("edit.application.document.combined");
			}
			
			String relativePathPdf = "/" + application.getKey() + "/" + getCombinedDocName("pdf");
			ApplicationDocument document = new ApplicationDocument(combineText, relativePathPdf);
			layoutCont.contextPut("combinedDocument", document);
			String relativePathZip = "/" + application.getKey() + "/" + getCombinedDocName("zip");
			String otherCombinedText = translate("edit.application.document.combined.others");
			ApplicationDocument otherDocumentsZip = new ApplicationDocument(otherCombinedText, relativePathZip);
			layoutCont.contextPut("otherDocumentsZip", otherDocumentsZip);
		}
	}
	
	private void initReferencesDocumentsForm(FormLayoutContainer layoutCont) {
		if(secCallback.canViewReferences()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()
						|| position.isComparativeAssessmentExpertEnabled())) {
			List<Reference> allReferences;
			if(preview && application.getKey() == null) {
				allReferences = ReferenceHelper.generateDummyReferences();
			} else {
				allReferences = recruitingService.getApplicationReferences(application, null);
			}
			
			int showCombined = 0;
			if(position.isExpertRecommendationEnabled() && secCallback.canViewReferencesOfExperts()) {
				List<ReferenceDocument> experts = new ArrayList<>(allReferences.size());
				for(Reference reference:allReferences) {
					if(reference.getReferenceType() == ReferenceType.expert && isReferenceVisible(reference)) {
						experts.add(new ReferenceDocument(reference, salutationGenerator.getTitleFullname(reference, getLocale())));
						if(reference.getReferenceStatus() == ReferenceStatus.submitted) {
							showCombined++;
						}
					} 
				}
				
				if(position.isExpertRecommendationEnabled()) {
					layoutCont.contextPut("experts", experts);
				}
			}
			
			if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()
					&& secCallback.canViewReferencesOfExpertsComparativeAssessment()) {
				List<ReferenceDocument> comparativeExperts = new ArrayList<>(allReferences.size());
				for(Reference reference:allReferences) {
					if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert && isReferenceVisible(reference)) {
						comparativeExperts.add(new ReferenceDocument(reference, salutationGenerator.getTitleFullname(reference, getLocale())));
						if(reference.getReferenceStatus() == ReferenceStatus.submitted) {
							showCombined++;
						}
					}
				}
				if(position.isComparativeAssessmentExpertEnabled()) {
					layoutCont.contextPut("comparativeExperts", comparativeExperts);
				}
			}
			layoutCont.contextPut("expertsCombinedDocument", Boolean.valueOf(showCombined > 0));// > 1???
			
			if(position.isRefereeRecommendationEnabled() && secCallback.canViewReferencesOfReferees()) {
				List<ReferenceDocument> recommendations = new ArrayList<>(allReferences.size());
				for(Reference reference:allReferences) {
					if(reference.getReferenceType() == ReferenceType.recommendation && isReferenceVisible(reference)) {
						recommendations.add(new ReferenceDocument(reference, salutationGenerator.getTitleFullname(reference, getLocale())));
					}
				}
				layoutCont.contextPut("recommendations", recommendations);
			}
		}
	}
	
	private boolean isReferenceVisible(Reference reference) {
		boolean visible = false;
		if(reference.getRequestStatus() != ReferenceRequestStatus.declined) {
			ReferenceStatus[] statusArr = secCallback.canViewReferencesWithStatus();
			for(ReferenceStatus status:statusArr) {
				if(status == reference.getReferenceStatus()) {
					visible = true;
				}
			}
		}
		return visible;
	}
	
	private String getCombinedDocName(String suffix) {
		String name = application.getId() + "_" + application.getPerson().getLastName() 
			+ "_" + application.getPerson().getFirstName();
		return RecruitingHelper.normalizeFilename(name) + "_combined." + suffix;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
