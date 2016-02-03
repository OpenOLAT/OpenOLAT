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
package org.olat.core.gui.components.chart;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.ValidationResult;

/**
 * Abstract class for d3, choose between d3 or r2d3 javascript
 * injection.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultD3Component extends AbstractComponent {
	
	private static final ComponentRenderer EMPTY_RENDERER = new DefaultComponentRenderer();
	
	public DefaultD3Component(String name) {
		super(name);
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//nothing to do
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return EMPTY_RENDERER;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		
		if(isUseR2D3(ureq)) {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/d3/r2d3.min.js");
		} else {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/d3/d3.min.js");
		}
	}
	
	private boolean isUseR2D3(UserRequest ureq) {
		String userAgent = ureq.getHttpReq().getHeader("user-agent");
		
		int msiePos = userAgent.indexOf("MSIE ");
        if (msiePos == -1 || userAgent.contains("Opera")) {
            return false;
        } else {
        	String next = userAgent.substring(msiePos + 5);
        	if(next.length() > 0) {
        		String val = next.substring(0, 1);
        		try {
					int ieVersion = Integer.valueOf(val);
					if(ieVersion <= 8 && ieVersion > 5) {
						return true;
					}
				} catch (NumberFormatException e) {
					//
				}
        	}
        }
		return false;
	}
}
