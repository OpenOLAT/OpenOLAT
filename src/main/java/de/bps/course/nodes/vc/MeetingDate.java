//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc;

import java.io.Serializable;
import java.util.Date;

/**
 * Description:<br>
 * Virtual Classroom appointment model to be used in course module configuration.
 * Initial Date: 30.08.2010 <br>
 * 
 * @author Jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class MeetingDate implements Serializable {
	
	private String title;
	private String description;
	private Date start;
	private Date end;
	
	public MeetingDate() {
		// nothing to do
	}

	public MeetingDate(final String title, final String description, Date start, Date end) {
		this.title = title;
		this.description = description;
		this.start= start;
		this.end = end;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(final String title) {
		this.title = title;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(final String description) {
		this.description = description;
	}
	
	public Date getBegin() {
		return start;
	}

	public void setBegin(Date start) {
			this.start = start;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
}
//</OLATCE-103>