/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.control.generic.title;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.gui.control.generic.spacesaver.ToggleBoxController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * Wrapper controller to any controller for providing a title and a optional description
 * to the child controller. The title consists of the contextTitle plus the item's title.
 * 
 * <P>
 * Initial Date:  21.06.2007 <br>
 * @author Lavinia Dumitrescu, Florian Gnägi
 */
public class TitledWrapperController extends BasicController implements CloneableController {
	
	private static final String COMPONENT_NAME = "child";
	//Velocity variable
	private static final String TITLE_VAR = "title";
	private static final String TITLE_SIZE = "size";
	private static final String TITLE_CSS = "titleCss";
	private static final String WRAPPER_CSS = "wrapperCss";
	private static final String CONTEXT_TITLE_VAR = "contextTitle";
	private static final String DESCRIPTION_TITLE_VAR = "descriptionTitle";
	private static final String DESCRIPTION_VAR = "description";
	private static final String DESCRIPTION_CSS = "descriptionCss";
	private static final String USE_SEPARATOR = "separator";
	
	private VelocityContainer theVelocityContainer;
	private VelocityContainer descriptionContainer;
	private ToggleBoxController descriptionController;
	private Controller contentController;
	
	private String wrapperCss;
	private TitleInfo titleInfo;
	
	/**
	 * Constructor for a title wrapper with the following default configuration:
	 * <ul>
	 * <li>Title size: 3</li>
	 * <li>Separator: false</li>
	 * </ul>
	 * Use the setter methods to modify these configuration or set special css
	 * classes
	 * 
	 * @param ureq
	 * @param wControl
	 * @param controller
	 * @param titleInfo
	 */
	public TitledWrapperController(UserRequest ureq, WindowControl wControl, Controller controller, String wrapperCss, TitleInfo titleInfo) {
		super(ureq, wControl);		
		theVelocityContainer = createVelocityContainer("titled_wrapper");
		if (controller != null) {
			contentController = controller;
			listenTo(contentController);
			theVelocityContainer.put(COMPONENT_NAME, controller.getInitialComponent());				
		}
		
		this.wrapperCss = wrapperCss;
		this.titleInfo = titleInfo;

		// set title info variables
		theVelocityContainer.contextPut(TITLE_VAR, titleInfo.getTitle());	
		theVelocityContainer.contextPut(CONTEXT_TITLE_VAR, titleInfo.getContextTitle());
		theVelocityContainer.contextPut(TITLE_SIZE, titleInfo.getTitleSize());			
		theVelocityContainer.contextPut(USE_SEPARATOR, Boolean.valueOf(titleInfo.isSeparatorEnabled()));			
		theVelocityContainer.contextPut(TITLE_CSS, titleInfo.getCssClass());
		theVelocityContainer.contextPut(WRAPPER_CSS, wrapperCss);
		
		//set the description if any
		if (StringHelper.containsNonWhitespace(titleInfo.getDescription())) {
			descriptionContainer = createVelocityContainer("desc_wrapped");
			
			String desc = titleInfo.getDescription();
			String latexIt = Formatter.formatLatexFormulas(desc);
			descriptionContainer.contextPut(DESCRIPTION_VAR, latexIt);
			descriptionContainer.contextPut(DESCRIPTION_CSS, titleInfo.getDescriptionCssClass());

			descriptionController = new ToggleBoxController(ureq, wControl, titleInfo.getPersistedId(),
					"  ", " ", descriptionContainer);
			
			if (StringHelper.containsNonWhitespace(titleInfo.getDescriptionTitle())) {
				descriptionContainer.contextPut(DESCRIPTION_TITLE_VAR, titleInfo.getDescriptionCssClass());
			}
			
			theVelocityContainer.put(DESCRIPTION_VAR, descriptionController.getInitialComponent());
		}
		
		putInitialPanel(theVelocityContainer);
	}
	
	protected void doDispose() {		
		if (descriptionController != null) {
			descriptionController.dispose();
			descriptionController = null;
		}
		theVelocityContainer = null;
		descriptionContainer = null;
		contentController = null;
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch	
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}

	/**
	 * @return The current content controller
	 */
	public Controller getContentController() {
		return contentController;
	}
	/**
	 * @param controller The new content controller or NULL
	 */
	public void setContentController(Controller controller) {
		if (controller == null) {
			if (contentController != null) {
				theVelocityContainer.remove(contentController.getInitialComponent());
				removeAsListenerAndDispose(contentController);
				contentController = null;
			}
			// nothing to do when old and new content controller are empty
		} else {
			removeAsListenerAndDispose(contentController);
			contentController = controller;
			listenTo(contentController);
			theVelocityContainer.put(COMPONENT_NAME, contentController.getInitialComponent());							
			// set user activity logger
		} 
	}
	/**
	 * @param title The title
	 */
	public void setTitle(String title) {
		theVelocityContainer.contextPut(TITLE_VAR, title);	
	}
	/**
	 * @param contextTitle The optional context of the title.
	 */
	public void setContextTitle(String contextTitle) {
		theVelocityContainer.contextPut(CONTEXT_TITLE_VAR, contextTitle);	
	}
	/**
	 * @param titleSize Size of the title. Use the static
	 *          TitleInfo.TITLE_SIZE_H3 variables
	 */
	public void setTitleSize(int titleSize) {
		theVelocityContainer.contextPut(TITLE_SIZE, titleSize);			
	}
	/**
	 * @param cssClass One or multiple CSS classes placed on the H element
	 */
	public void setTitleCssClass(String cssClass) {
		theVelocityContainer.contextPut(TITLE_CSS, cssClass);					
	}
	/**
	 * @param useSeparator true to set a HR element after the title, false to not
	 *          use the HR element
	 */
	public void setSeparatorEnabled(boolean useSeparator) {
		theVelocityContainer.contextPut(USE_SEPARATOR, Boolean.valueOf(useSeparator));					
	}

	@Override
	public Controller cloneController(UserRequest ureq, WindowControl wControl) {
		if(contentController == null || contentController instanceof CloneableController) {
			Controller wrappedAndCloned = ((CloneableController)contentController).cloneController(ureq, wControl);
			TitledWrapperController clone = new TitledWrapperController(ureq, wControl, wrappedAndCloned, wrapperCss, titleInfo);
			clone.setTitle((String)theVelocityContainer.getContext().get(TITLE_VAR));
			clone.setContextTitle((String)theVelocityContainer.getContext().get(CONTEXT_TITLE_VAR));
			clone.setTitleCssClass((String)theVelocityContainer.getContext().get(TITLE_CSS));
			clone.setSeparatorEnabled((Boolean)theVelocityContainer.getContext().get(USE_SEPARATOR));	
			clone.setTitleSize((Integer)theVelocityContainer.getContext().get(TITLE_SIZE));	
			return clone;
		}
		return null;
	}
}
