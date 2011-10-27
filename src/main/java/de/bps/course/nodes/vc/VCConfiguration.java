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
import java.util.List;


/**
 * 
 * Description:<br>
 * Each virtual classroom implementation must persist it's configuration
 * by using this interface. The course node is able to differentiate for
 * which implementation the stored configuration is intended (necessary to
 * support multiple virtual classroom implementations in one OLAT instance). 
 * 
 * <P>
 * Initial Date:  20.12.2010 <br>
 * @author skoeber
 */
public interface VCConfiguration extends Serializable {

	public String getProviderId();
	public String getTemplateKey();
	public boolean isUseMeetingDates();
	public List<MeetingDate> getMeetingDates();
	public boolean isCreateMeetingImmediately();
	public boolean isConfigValid();
}
//</OLATCE-103>