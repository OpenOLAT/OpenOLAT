package de.bps.webservices.clients.onyxreporter;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//<ONYX-705>

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "studentswrapper")
public class ResultsForStudentsWrapper {
	@XmlElement(name = "student")
	private ArrayList<ResultsForStudent> students;

	public ArrayList<ResultsForStudent> getStudents() {
		return students;
	}

	public void setStudents(ArrayList<ResultsForStudent> students) {
		this.students = students;
	}
}
