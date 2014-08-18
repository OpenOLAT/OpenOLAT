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

package org.olat.portfolio;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Integration test of the PortfolioModule
 * 
 * <P>
 * Initial Date:  23 . 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class PortfolioModuleTest extends OlatTestCase {

	@Autowired
	private PortfolioModule portfolioModule;
	
	@Test
	public void testGetModule() {
		assertNotNull(portfolioModule);
	}
	
	@Test
	public void testAddArtefactHandler() {
		DummyArtefactHandler artefactHandler = new DummyArtefactHandler();
		portfolioModule.addArtefactHandler(artefactHandler);
		
		List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
		boolean found = false;
		for(EPArtefactHandler<?> handler:handlers) {
			if(handler == artefactHandler) {
				found = true;
			}
		}
		assertTrue(found);
	}
	
	@Test
	public void testRemoveArtefactHandler() {
		//prepare the dummy artefact handler
		EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
		if(handler == null) {
			handler = new DummyArtefactHandler();
			portfolioModule.addArtefactHandler(handler);
		}
		
		//remove it
		portfolioModule.removeArtefactHandler(handler);
		
		//check if
		EPArtefactHandler<?> removedHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
		assertNull(removedHandler);
	}
	
	@Test
	public void testGetArtefactHandlers() {
		List<EPArtefactHandler<?>> handlers = portfolioModule.getArtefactHandlers();
		assertFalse(handlers.isEmpty());
	}
	
	@Test
	public void testGetAllAvailableArtefactHandlers() {
		List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
		assertFalse(handlers.isEmpty());
	}
	
	@Test
	public void testSetEnableArtefactHandler() {
		//prepare the dummy artefact handler
		EPArtefactHandler<?> dummyHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
		if(dummyHandler == null) {
			dummyHandler = new DummyArtefactHandler();
			portfolioModule.addArtefactHandler(dummyHandler);
		}
		
		//////////////////////////////////
		// disable
		//////////////////////////////////
		portfolioModule.setEnableArtefactHandler(dummyHandler, false);
		sleep(2000);//settings asynchronous
		
		//found in the list of all available handlers
		List<EPArtefactHandler<?>> allHandlers = portfolioModule.getAllAvailableArtefactHandlers();
		boolean foundInAll = false;
		for(EPArtefactHandler<?> handler:allHandlers) {
			if(handler == dummyHandler) {
				foundInAll = true;
				assertFalse(handler.isEnabled());
			}
		}
		assertTrue(foundInAll);
		
		//not found in the list of handlers
		List<EPArtefactHandler<?>> enabledHandlers = portfolioModule.getArtefactHandlers();
		boolean foundInEnabled = false;
		for(EPArtefactHandler<?> handler:enabledHandlers) {
			if(handler == dummyHandler) {
				foundInEnabled = true;
				assertFalse(handler.isEnabled());
			}
		}
		assertFalse(foundInEnabled);
		
		//found but disabled in get with type
		EPArtefactHandler<?> disabledDummyHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
		assertFalse(disabledDummyHandler.isEnabled());
		
		//////////////////////////////////
		// enable
		//////////////////////////////////
		portfolioModule.setEnableArtefactHandler(dummyHandler, false);
		sleep(2000);//settings asynchronous
		
		//found in the list of all available handlers
		allHandlers = portfolioModule.getAllAvailableArtefactHandlers();
		foundInAll = false;
		for(EPArtefactHandler<?> handler:allHandlers) {
			if(handler == dummyHandler) {
				foundInAll = true;
				assertFalse(handler.isEnabled());
			}
		}
		assertTrue(foundInAll);
		
		//not found in the list of handlers
		enabledHandlers = portfolioModule.getArtefactHandlers();
		foundInEnabled = false;
		for(EPArtefactHandler<?> handler:enabledHandlers) {
			if(handler == dummyHandler) {
				foundInEnabled = true;
				assertFalse(handler.isEnabled());
			}
		}
		assertFalse(foundInEnabled);
		
		//found but disabled in get with type
		EPArtefactHandler<?> enabledDummyHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
		assertFalse(enabledDummyHandler.isEnabled());
	}
	
	@Test
	public void testSetEnabled() {
		portfolioModule.setEnabled(true);
		assertTrue(portfolioModule.isEnabled());
		portfolioModule.setEnabled(false);
		assertFalse(portfolioModule.isEnabled());
		portfolioModule.setEnabled(true);
	}
	
	public class DummyArtefactHandler extends EPAbstractHandler<DummyArtefact> {
		@Override
		public String getType() {
			return DummyArtefact.TYPE;
		}

		@Override
		public DummyArtefact createArtefact() {
			return new DummyArtefact();
		}

		@Override
		public PackageTranslator getHandlerTranslator(Translator fallBackTrans) {
			return null;
		}

		@Override
		public boolean isProvidingSpecialMapViewController() {
			return false;
		}
	}
	
	public class DummyArtefact extends AbstractArtefact {
		private static final long serialVersionUID = -7986085106701245624L;
		public static final String TYPE = "dummy";

		@Override
		public String getResourceableTypeName() {
			return TYPE;
		}

		@Override
		public String getIcon() {
			return "o_ep_dummy";
		}
		
	}
}
