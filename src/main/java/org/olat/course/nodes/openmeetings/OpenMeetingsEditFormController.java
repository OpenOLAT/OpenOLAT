package org.olat.course.nodes.openmeetings;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.OpenMeetingsCourseNode;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.ui.OpenMeetingsRoomEditController;

/**
 * 
 * Initial date: 08.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsEditFormController extends FormBasicController {
	
	private FormLink editLink;
	private CloseableModalController cmc;
	private OpenMeetingsRoomEditController editController;
	
	private String courseTitle;
	private final OLATResourceable course;
	private final OpenMeetingsCourseNode courseNode;
	private final OpenMeetingsManager openMeetingsManager;
	
	private OpenMeetingsRoom room;

	public OpenMeetingsEditFormController(UserRequest ureq, WindowControl wControl, OLATResourceable course,
			OpenMeetingsCourseNode courseNode, String courseTitle)
	    throws OpenMeetingsException{
		super(ureq, wControl);
		
		this.course = course;
		this.courseNode = courseNode;
		this.courseTitle = courseTitle;
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);

		room = openMeetingsManager.getRoom(null, course, courseNode.getIdent());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(room == null) {
			FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonContainer);
			editLink = uifactory.addFormLink("create.room", buttonContainer, Link.BUTTON);
		} else {
			String name = room.getName();
			uifactory.addStaticTextElement("room.name", "room.name", name, formLayout);

			FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonContainer);
			editLink = uifactory.addFormLink("edit.room", buttonContainer, Link.BUTTON);	
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == editLink) {
			doEditRoom(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	protected void doEditRoom(UserRequest ureq) {
		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(cmc);
		
		try {
			editController = new OpenMeetingsRoomEditController(ureq, getWindowControl(), null, course, courseNode.getIdent(), courseTitle, true);
			listenTo(editController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent(), true, translate("edit.room"));
			listenTo(cmc);
			cmc.activate();
		} catch (Exception e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
		}
	}
	
	

}
