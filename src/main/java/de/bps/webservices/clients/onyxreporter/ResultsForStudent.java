package de.bps.webservices.clients.onyxreporter;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//<ONYX-705>

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="student")
public class ResultsForStudent implements Serializable{
	@XmlElement(name="studentId")
	private String studentId = "";
	@XmlElement(name="firstname")
	private String firstname = "";
	@XmlElement(name="lastname")
	private String lastname = "";
	@XmlElement(name="groupname")
	private String groupname = "";
	@XmlElement(name="tutorname")
	private String tutorname = "";
	@XmlElement(name="resultsFile")
	private byte[] resultsFile = null;

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getTutorname() {
		return tutorname;
	}

	public void setTutorname(String tutorname) {
		this.tutorname = tutorname;
	}

	public byte[] getResultsFile() {
		return resultsFile;
	}

	public void setResultsFile(byte[] resultsFile) {
		this.resultsFile = resultsFile;
	}
}
