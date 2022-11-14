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

package org.olat.core.gui.components.velocity;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.context.Context;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;

/**
 * Renderer for the VelocityContainer <br>
 * 
 * @author Felix Jost
 */
public class VelocityContainerRenderer extends DefaultComponentRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(VelocityContainerRenderer.class);

	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		VelocityContainer vc = (VelocityContainer) source;
		String pagePath = vc.getPage();
		Context ctx = vc.getContext();
		
		// the component id of the urlbuilder  will be overwritten by the recursive render call for
		// subcomponents (see Renderer)
		Renderer fr = Renderer.getInstance(vc, translator, ubu, renderResult, renderer.getGlobalSettings(), renderer.getCsrfToken());
		try(VelocityRenderDecorator vrdec = new VelocityRenderDecorator(fr, vc, target)) {
			ctx.put("r", vrdec);
			VelocityHelper vh = VelocityHelper.getInstance();
			vh.mergeContent(pagePath, ctx, target, null);
			//free the decorator
			ctx.remove("r");
			
			//set all not rendered component as not dirty
			for(Component cmp: vc.getComponents()) {
				if(cmp.isDirty()) {
					cmp.setDirty(false);
				}
			}
		} catch(Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		VelocityContainer vc = (VelocityContainer) source;
		// the velocity container itself needs no headerincludes, but ask the
		// children also
		for (Component child : vc.getComponents()) {
			renderer.renderHeaderIncludes(sb, child, rstate);
		}
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		VelocityContainer vc = (VelocityContainer) source;
		// the velocity container itself needs no headerincludes, but ask the
		// children also
		for (Component child : vc.getComponents()) {
			renderer.renderBodyOnLoadJSFunctionCall(sb, child, rstate);
		}
	}
}