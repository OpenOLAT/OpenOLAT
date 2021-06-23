package org.olat.modules.portfolio.model.export;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 23 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderXML {

	private String title;
	private String summary;
	private String imagePath;

	private List<SectionXML> sections;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public List<SectionXML> getSections() {
		if(sections == null) {
			sections = new ArrayList<>();
		}
		return sections;
	}

	public void setSections(List<SectionXML> sections) {
		this.sections = sections;
	}
}
