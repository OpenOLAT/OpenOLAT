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
package org.olat.modules.selectus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Attachments;
import org.olat.modules.selectus.model.AttachmentsImpl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum DocumentEnum {
	coveringLetter("coveringLetter", "edit.application.document.covering_letter"),
	curriculumVitae("curriculumVitae", "edit.application.document.cv"),
	publications("publicationList", "edit.application.document.publications"),
	statements("researchStatement", "edit.application.document.statements"),
	proposals("teachingStatement", "edit.application.document.proposals"),
	leadership("leadership", "edit.application.document.leadership"),
	referees("listOfReferees", "edit.application.document.referees"),
	projects("projectList", "edit.application.document.projects"),
	references("referenceLetters", "edit.application.document.references"),
	teachingAssessment("teachingAssessment", "edit.application.document.teachingAssessment"),
	certificateOfStudy("certificateOfStudy", "edit.application.document.certificateOfStudy"),
	degreeCertificates("degreeCertificates", "edit.application.document.degreeCertificates"),
	dissertation("dissertation", "edit.application.document.dissertation"),
	habilitation("habilitation", "edit.application.document.habilitation"),
	clinicalDisciplines("clinicalDisciplines", "edit.application.document.clinicaldisciplines"),
	surgicalDisciplines("surgicalDisciplines", "edit.application.document.surgicaldisciplines"),
	reprints("reprints", "edit.application.document.reprints"),
	externalFunding("externalFunding", "edit.application.document.externalfunding"),
	publication1("publication1", "edit.application.document.publication1"),
	publication2("publication2", "edit.application.document.publication2"),
	publication3("publication3", "edit.application.document.publication3"),
	publication4("publication4", "edit.application.document.publication4"),
	publication5("publication5", "edit.application.document.publication5"),
	other("otherDocument", "edit.application.document.other"),
	combined("combined", "edit.application.document.combined.staff");

	private final String field;
	private final String i18nKey;
	private final String i18nExplainKey;
	
	private DocumentEnum(String field, String i18nKey) {
		this.field = field;
		this.i18nKey = i18nKey;
		this.i18nExplainKey = i18nKey + ".explain";
	}

	public String i18nKey() {
		return i18nKey;
	}
	
	public String i18nExplainKey() {
		return i18nExplainKey;
	}
	
	public Attachment path(Application application) {
		Attachments attachments = application.getAttachments();
		if(attachments == null) {
			return null;
		}
		switch(this) {
			case coveringLetter: return attachments.getCoveringLetter();
			case curriculumVitae: return attachments.getCurriculumVitae();
			case publications: return attachments.getPublicationList();
			case statements: return attachments.getResearchStatement();
			case proposals: return attachments.getTeachingStatement();
			case leadership: return attachments.getLeadership();
			case referees: return attachments.getListOfReferees();
			case projects: return attachments.getProjectList();
			case references: return attachments.getReferenceLetters();
			case teachingAssessment: return attachments.getTeachingAssessment();
			case certificateOfStudy: return attachments.getCertificateOfStudy();
			case degreeCertificates: return attachments.getDegreeCertificates();
			case dissertation: return attachments.getDissertation();
			case habilitation: return attachments.getHabilitation();
			case clinicalDisciplines: return attachments.getClinicalDisciplines();
			case surgicalDisciplines: return attachments.getSurgicalDisciplines();
			case reprints: return attachments.getReprints();
			case externalFunding: return attachments.getExternalFunding();
			case publication1: return attachments.getPublication1();
			case publication2: return attachments.getPublication2();
			case publication3: return attachments.getPublication3();
			case publication4: return attachments.getPublication4();
			case publication5: return attachments.getPublication5();
			case other: return attachments.getOtherDocument();
			case combined: return attachments.getCombinedDocument();
			default: return null;
		}
	}
	
	public void setPath(Application application, Attachment path) {
		Attachments attachments = application.getAttachments();
		if(attachments == null) {
			attachments = new AttachmentsImpl();
		}
		
		switch(this) {
			case coveringLetter: attachments.setCoveringLetter(path); break;
			case curriculumVitae: attachments.setCurriculumVitae(path); break;
			case publications: attachments.setPublicationList(path); break;
			case statements: attachments.setResearchStatement(path); break;
			case proposals: attachments.setTeachingStatement(path); break;
			case leadership: attachments.setLeadership(path); break;
			case referees: attachments.setListOfReferees(path); break;
			case projects: attachments.setProjectList(path); break;
			case references: attachments.setReferenceLetters(path); break;
			case teachingAssessment: attachments.setTeachingAssessment(path); break;
			case certificateOfStudy: attachments.setCertificateOfStudy(path); break;
			case degreeCertificates: attachments.setDegreeCertificates(path); break;
			case dissertation: attachments.setDissertation(path); break;
			case habilitation: attachments.setHabilitation(path); break;
			case clinicalDisciplines: attachments.setClinicalDisciplines(path); break;
			case surgicalDisciplines: attachments.setSurgicalDisciplines(path); break;
			case reprints: attachments.setReprints(path); break;
			case externalFunding: attachments.setExternalFunding(path); break;
			case publication1: attachments.setPublication1(path); break;
			case publication2: attachments.setPublication2(path); break;
			case publication3: attachments.setPublication3(path); break;
			case publication4: attachments.setPublication4(path); break;
			case publication5: attachments.setPublication5(path); break;
			case other: attachments.setOtherDocument(path); break;
			case combined: attachments.setCombinedDocument(path); break;
		}
		
		application.setAttachments(attachments);
	}
	
	public String field() {
		return field;
	}
	
	public static Set<String> documentStringToSet(String doc) {
		Set<String> docSet = new HashSet<>();
		if(StringHelper.containsNonWhitespace(doc)) {
			for(StringTokenizer tokenizer= new StringTokenizer(doc,","); tokenizer.hasMoreTokens(); ) {
				docSet.add(tokenizer.nextToken());
			}
		}
		return docSet;
	}
	
	public static Set<DocumentEnum> documentStringToEnumSet(String doc) {
		Set<DocumentEnum> docs = new HashSet<>();
		if(StringHelper.containsNonWhitespace(doc)) {
			for(StringTokenizer tokenizer= new StringTokenizer(doc,","); tokenizer.hasMoreTokens(); ) {
				docs.add(DocumentEnum.valueOf(tokenizer.nextToken()));
			}
		}
		return docs;
	}
	
	public static String documentStringSetToString(Collection<String> docs) {
		StringBuilder sb = new StringBuilder();
		for(String doc:docs) {
			if(sb.length() > 0) sb.append(",");
			sb.append(doc);
		}
		return sb.toString();
	}
}
