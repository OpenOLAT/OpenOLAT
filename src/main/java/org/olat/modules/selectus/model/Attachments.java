/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Attachments {
	
	public Attachment getCoveringLetter();

	public void setCoveringLetter(Attachment coveringLetter);

	public Attachment getCurriculumVitae();

	public void setCurriculumVitae(Attachment curriculumVitae);

	public Attachment getPublicationList();

	public void setPublicationList(Attachment publicationList);

	public Attachment getResearchStatement();

	public void setResearchStatement(Attachment statementOfResearch);

	public Attachment getTeachingStatement();

	public void setTeachingStatement(Attachment teachingStatement);

	public Attachment getListOfReferees();

	public void setListOfReferees(Attachment listOfReferees);
	
	public Attachment getProjectList();

	public void setProjectList(Attachment projectList);

	public Attachment getReferenceLetters();

	public void setReferenceLetters(Attachment referenceLetters);
	
	public Attachment getTeachingAssessment();

	public void setTeachingAssessment(Attachment teachingAssessment);
	
	public Attachment getCertificateOfStudy();

	public void setCertificateOfStudy(Attachment certificate);
	
	public Attachment getLeadership();

	public void setLeadership(Attachment leadership);
	
	public Attachment getDegreeCertificates();

	public void setDegreeCertificates(Attachment degreeCertificates);
	
	public Attachment getDissertation();

	public void setDissertation(Attachment dissertation);
	
	public Attachment getHabilitation();

	public void setHabilitation(Attachment habilitation);
	
	public Attachment getClinicalDisciplines();

	public void setClinicalDisciplines(Attachment clinicalDisciplines);

	public Attachment getSurgicalDisciplines();

	public void setSurgicalDisciplines(Attachment surgicalDisciplines);

	public Attachment getReprints();

	public void setReprints(Attachment reprints);

	public Attachment getExternalFunding();

	public void setExternalFunding(Attachment externalFunding);
	
	public Attachment getPublication1();

	public void setPublication1(Attachment publication1);

	public Attachment getPublication2();

	public void setPublication2(Attachment publication2);
	
	public Attachment getPublication3();

	public void setPublication3(Attachment publication3);
	
	public Attachment getPublication4();

	public void setPublication4(Attachment publication4);
	
	public Attachment getPublication5();

	public void setPublication5(Attachment publication5);


	public Attachment getOtherDocument();
	
	public void setOtherDocument(Attachment otherdocument);
	
	public Attachment getCombinedDocument();
	
	public void setCombinedDocument(Attachment combinedDocument);
	
	
	

}
