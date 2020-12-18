/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.VetoPopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.pool.QTI12To21Converter;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.author.CreateRepositoryEntryController;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * This particularly overview the veto of the QTI editor
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIRuntimeController extends RepositoryEntryRuntimeController implements VetoableCloseController  {
	
	private Link convertQTI21Link;

	private CloseableModalController localCmc;
	private CreateRepositoryEntryController createConvertedTestController;

	private Delayed delayedClose;
	
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public QTIRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	@Override
	protected void initToolsMenuEdition(Dropdown settingsDropdown) {
		super.initToolsMenuEdition(settingsDropdown);
		RepositoryEntry re = getRepositoryEntry();
		boolean copyManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.copy);
		boolean canConvert = (isAuthor || reSecurity.isEntryAdmin()) && (re.getCanCopy() || reSecurity.isEntryAdmin()) && !copyManaged
				&& QTI12To21Converter.isConvertible(re.getOlatResource());

		if(canConvert) {
			convertQTI21Link = LinkFactory.createToolLink("convert.qti.21", translate("tools.convert.qti21"), this, "o_FileResource-IMSQTI21_icon");
			convertQTI21Link.setIconLeftCSS("o_icon o_FileResource-IMSQTI21_icon");
			settingsDropdown.addComponent(convertQTI21Link);
		}
	}
	
	/**
	 * This is only used by the QTI editor
	 */
	@Override
	public boolean requestForClose(UserRequest ureq) {
		if(editorCtrl instanceof VetoableCloseController) {
			return ((VetoableCloseController)editorCtrl).requestForClose(ureq);
		}
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editorCtrl && source instanceof VetoableCloseController) {
			if(event == Event.DONE_EVENT) {
				if(delayedClose != null) {
					switch(delayedClose) {
						case details: super.doDetails(ureq); break;
						case settings: super.doSettings(ureq, null); break;
						case members: super.doMembers(ureq); break;
						case orders: super.doOrders(ureq); break;
						case close: super.doClose(ureq); break;
						case pop: {
							popToRoot(ureq);
							cleanUp();
							Controller runtimeCtrl = getRuntimeController();
							if(runtimeCtrl != null) {
								launchContent(ureq);
								initToolbar();
							}
							break;
						}
						default: {}
					}
					delayedClose = null;
				} else {
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		} else if(createConvertedTestController == source) {
			localCmc.deactivate();
			if(event == Event.DONE_EVENT) {
				showInfo("test.converted");
				RepositoryEntry convertedEntry = createConvertedTestController.getAddedEntry();
				String businessPath = "[RepositoryEntry:" + convertedEntry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
			cleanUp();
		} else if(localCmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(createConvertedTestController);
		removeAsListenerAndDispose(localCmc);
		createConvertedTestController = null;
		localCmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(event == Event.CLOSE_EVENT) {
			if(requestForClose(ureq)) {
				super.event(ureq, source, event);
			} else {
				delayedClose = Delayed.close;
			}
		} else if(event instanceof VetoPopEvent) {
			if(requestForClose(ureq)) {
				super.event(ureq, source, event);
			} else {
				delayedClose = Delayed.pop;
			}
		} else if(convertQTI21Link == source) {
			doConvertToQTI21(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected Activateable2 doSettings(UserRequest ureq, List<ContextEntry> entries) {
		if(requestForClose(ureq)) {
			return super.doSettings(ureq, entries);
		}
		delayedClose = Delayed.settings;
		return null;
	}

	@Override
	protected void doDetails(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doDetails(ureq);
		} else {
			delayedClose = Delayed.details; 
		}
	}

	@Override
	protected Activateable2 doMembers(UserRequest ureq) {
		if(requestForClose(ureq)) {
			return super.doMembers(ureq);
		}
		delayedClose = Delayed.members;
		return null;
	}

	@Override
	protected void doOrders(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doOrders(ureq);
		} else {
			delayedClose = Delayed.orders; 
		}
	}
	
	private void doConvertToQTI21(UserRequest ureq) {
		removeAsListenerAndDispose(localCmc);
		removeAsListenerAndDispose(createConvertedTestController);

		OLATResource originalObject = getRepositoryEntry().getOlatResource();
		RepositoryHandler qti21Handler = repositoryHandlerFactory.getRepositoryHandler(ImsQTI21Resource.TYPE_NAME);
		createConvertedTestController = new CreateRepositoryEntryController(ureq, getWindowControl(), qti21Handler, false);
		createConvertedTestController.setCreateObject(originalObject);
		createConvertedTestController.setDisplayname(getRepositoryEntry().getDisplayname());
		createConvertedTestController.setExampleAndHelp(translate("convert.qti21.hint"), "Change+from+QTI+1.2+to+QTI+2.1");
		listenTo(createConvertedTestController);

		localCmc = new CloseableModalController(getWindowControl(), translate("close"), createConvertedTestController.getInitialComponent(), true, translate("title.convert.qti21") );
		localCmc.activate();
		listenTo(localCmc);
	}

	private enum Delayed {
		details,
		settings,
		members,
		orders,
		close,
		pop
	}
}
