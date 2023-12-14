package org.olat.course.editor;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 14 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditorTreeEvent extends Event {

	private static final long serialVersionUID = 1309687254843136507L;
	
	public static final String CONFIGURATION_VIEW = "configuration-view";
	
	public EditorTreeEvent(String name) {
		super(name);
	}
}
