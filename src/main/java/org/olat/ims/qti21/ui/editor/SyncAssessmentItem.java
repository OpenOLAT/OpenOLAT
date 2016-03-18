package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;

/**
 * 
 * Initial date: 18.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface SyncAssessmentItem {

	public void sync(UserRequest ureq, AssessmentItemBuilder itemBuilder);
}
