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
package org.olat.modules.selectus.model;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AttachmentsImpl implements Attachments, Serializable {

	private static final long serialVersionUID = -4572614784937452587L;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_coveringletter_id", nullable=true, insertable=true, updatable=true)
	private Attachment coveringLetter;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_curriculumvitae_id", nullable=true, insertable=true, updatable=true)
	private Attachment curriculumVitae;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_publicationlist_id", nullable=true, insertable=true, updatable=true)
	private Attachment publicationList;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_researchstatement_id", nullable=true, insertable=true, updatable=true)
	private Attachment researchStatement;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_teachingstatement_id", nullable=true, insertable=true, updatable=true)
	private Attachment teachingStatement;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_leadership_id", nullable=true, insertable=true, updatable=true)
	private Attachment leadership;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_listofreferees_id", nullable=true, insertable=true, updatable=true)
	private Attachment listOfReferees;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_projectlist_id", nullable=true, insertable=true, updatable=true)
	private Attachment projectList;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_referenceletters_id", nullable=true, insertable=true, updatable=true)
	private Attachment referenceLetters;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_teachingassessment_id", nullable=true, insertable=true, updatable=true)
	private Attachment teachingAssessment;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_certificateofstudy_id", nullable=true, insertable=true, updatable=true)
	private Attachment certificateOfStudy;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_degreecertificates_id", nullable=true, insertable=true, updatable=true)
	private Attachment degreeCertificates;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_dissertation_id", nullable=true, insertable=true, updatable=true)
	private Attachment dissertation;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_habilitation_id", nullable=true, insertable=true, updatable=true)
	private Attachment habilitation;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_clinicaldisciplines_id", nullable=true, insertable=true, updatable=true)
	private Attachment clinicalDisciplines;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_surgicaldisciplines_id", nullable=true, insertable=true, updatable=true)
	private Attachment surgicalDisciplines;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_reprints_id", nullable=true, insertable=true, updatable=true)
	private Attachment reprints;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_externalfunding_id", nullable=true, insertable=true, updatable=true)
	private Attachment externalFunding;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_publication1_id", nullable=true, insertable=true, updatable=true)
	private Attachment publication1;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_publication2_id", nullable=true, insertable=true, updatable=true)
	private Attachment publication2;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_publication3_id", nullable=true, insertable=true, updatable=true)
	private Attachment publication3;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_publication4_id", nullable=true, insertable=true, updatable=true)
	private Attachment publication4;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_publication5_id", nullable=true, insertable=true, updatable=true)
	private Attachment publication5;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_otherdocument_id", nullable=true, insertable=true, updatable=true)
	private Attachment otherDocument;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_combineddocument_id", nullable=true, insertable=true, updatable=true)
	private Attachment combinedDocument;

	@Override
	public Attachment getCoveringLetter() {
		return coveringLetter;
	}

	@Override
	public void setCoveringLetter(Attachment coveringLetter) {
		this.coveringLetter = coveringLetter;
	}

	@Override
	public Attachment getCurriculumVitae() {
		return curriculumVitae;
	}

	@Override
	public void setCurriculumVitae(Attachment curriculumVitae) {
		this.curriculumVitae = curriculumVitae;
	}

	@Override
	public Attachment getPublicationList() {
		return publicationList;
	}

	@Override
	public void setPublicationList(Attachment publicationList) {
		this.publicationList = publicationList;
	}

	@Override
	public Attachment getResearchStatement() {
		return researchStatement;
	}

	@Override
	public void setResearchStatement(Attachment researchStatement) {
		this.researchStatement = researchStatement;
	}

	@Override
	public Attachment getTeachingStatement() {
		return teachingStatement;
	}

	@Override
	public void setTeachingStatement(Attachment teachingStatement) {
		this.teachingStatement = teachingStatement;
	}

	@Override
	public Attachment getListOfReferees() {
		return listOfReferees;
	}

	@Override
	public void setListOfReferees(Attachment listOfReferees) {
		this.listOfReferees = listOfReferees;
	}

	@Override
	public Attachment getProjectList() {
		return projectList;
	}

	@Override
	public void setProjectList(Attachment projectList) {
		this.projectList = projectList;
	}

	@Override
	public Attachment getReferenceLetters() {
		return referenceLetters;
	}

	@Override
	public void setReferenceLetters(Attachment referenceLetters) {
		this.referenceLetters = referenceLetters;
	}

	@Override
	public Attachment getTeachingAssessment() {
		return teachingAssessment;
	}

	@Override
	public void setTeachingAssessment(Attachment teachingAssessment) {
		this.teachingAssessment = teachingAssessment;
	}

	@Override
	public Attachment getCertificateOfStudy() {
		return certificateOfStudy;
	}

	@Override
	public void setCertificateOfStudy(Attachment certificateOfStudy) {
		this.certificateOfStudy = certificateOfStudy;
	}

	@Override
	public Attachment getLeadership() {
		return leadership;
	}

	@Override
	public void setLeadership(Attachment leadership) {
		this.leadership = leadership;
	}

	@Override
	public Attachment getDegreeCertificates() {
		return degreeCertificates;
	}

	@Override
	public void setDegreeCertificates(Attachment degreeCertificates) {
		this.degreeCertificates = degreeCertificates;
	}

	@Override
	public Attachment getDissertation() {
		return dissertation;
	}

	@Override
	public void setDissertation(Attachment dissertation) {
		this.dissertation = dissertation;
	}

	@Override
	public Attachment getHabilitation() {
		return habilitation;
	}

	@Override
	public void setHabilitation(Attachment habilitation) {
		this.habilitation = habilitation;
	}

	@Override
	public Attachment getClinicalDisciplines() {
		return clinicalDisciplines;
	}

	@Override
	public void setClinicalDisciplines(Attachment clinicalDisciplines) {
		this.clinicalDisciplines = clinicalDisciplines;
	}

	@Override
	public Attachment getSurgicalDisciplines() {
		return surgicalDisciplines;
	}

	@Override
	public void setSurgicalDisciplines(Attachment surgicalDisciplines) {
		this.surgicalDisciplines = surgicalDisciplines;
	}

	@Override
	public Attachment getReprints() {
		return reprints;
	}

	@Override
	public void setReprints(Attachment reprints) {
		this.reprints = reprints;
	}

	@Override
	public Attachment getExternalFunding() {
		return externalFunding;
	}

	@Override
	public void setExternalFunding(Attachment externalFunding) {
		this.externalFunding = externalFunding;
	}

	@Override
	public Attachment getPublication1() {
		return publication1;
	}

	@Override
	public void setPublication1(Attachment publication1) {
		this.publication1 = publication1;
	}

	@Override
	public Attachment getPublication2() {
		return publication2;
	}

	@Override
	public void setPublication2(Attachment publication2) {
		this.publication2 = publication2;
	}

	@Override
	public Attachment getPublication3() {
		return publication3;
	}

	@Override
	public void setPublication3(Attachment publication3) {
		this.publication3 = publication3;
	}

	@Override
	public Attachment getPublication4() {
		return publication4;
	}

	@Override
	public void setPublication4(Attachment publication4) {
		this.publication4 = publication4;
	}

	@Override
	public Attachment getPublication5() {
		return publication5;
	}

	@Override
	public void setPublication5(Attachment publication5) {
		this.publication5 = publication5;
	}

	@Override
	public Attachment getOtherDocument() {
		return otherDocument;
	}

	@Override
	public void setOtherDocument(Attachment otherDocument) {
		this.otherDocument = otherDocument;
	}

	@Override
	public Attachment getCombinedDocument() {
		return combinedDocument;
	}

	@Override
	public void setCombinedDocument(Attachment combinedDocument) {
		this.combinedDocument = combinedDocument;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("attachments[")
			.append("covering=").append(coveringLetter == null ? "null" : coveringLetter).append(";")
			.append("cv=").append(curriculumVitae == null ? "null" : curriculumVitae).append(";")
			.append("publications=").append(publicationList == null ? "null" : publicationList).append(";")
			.append("research=").append(researchStatement == null ? "null" : researchStatement).append(";")
			.append("teaching=").append(teachingStatement == null ? "null" : teachingStatement).append(";")
			.append("leadership=").append(leadership == null ? "null" : leadership).append(";")
			.append("referees=").append(listOfReferees == null ? "null" : listOfReferees).append(";")
			.append("projects=").append(projectList == null ? "null" : projectList).append(";")
			.append("references=").append(referenceLetters == null ? "null" : referenceLetters).append(";")
			.append("teachingAssessment=").append(teachingAssessment == null ? "null" : teachingAssessment).append(";")
			.append("degreeCertificates=").append(degreeCertificates == null ? "null" : degreeCertificates).append(";")
			.append("dissertation=").append(dissertation == null ? "null" : dissertation).append(";")
			.append("habilitation=").append(habilitation == null ? "null" : habilitation).append(";")
			.append("publication1=").append(publication1 == null ? "null" : publication1).append(";")
			.append("publication2=").append(publication2 == null ? "null" : publication2).append(";")
			.append("publication3=").append(publication3 == null ? "null" : publication3).append(";")
			.append("publication4=").append(publication4 == null ? "null" : publication4).append(";")
			.append("publication5=").append(publication5 == null ? "null" : publication5).append(";")
			.append("others=").append(otherDocument == null ? "null" : otherDocument).append(";")
			.append("combined=").append(combinedDocument == null ? "null" : combinedDocument).append(";")
			.append("]");
		return sb.toString();
	}
}
