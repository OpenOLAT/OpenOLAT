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
package org.olat.ims.qti21.restapi;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.ims.qti21.QTI21Service;

import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.qtiworks.mathassess.XsltStylesheetCacheAdapter;
import uk.ac.ed.ph.qtiworks.mathassess.glue.AsciiMathHelper;

/**
 * 
 * 
 * 
 * Initial date: 25.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Path("math")
public class MathWebService {
	
	private static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	@POST
    @Path("verifyAsciiMath")
    @Produces({MediaType.APPLICATION_JSON})
    public Response verifyAsciiMath(@FormParam("input") String asciiMathInput) {
    	XsltStylesheetCache stylesheetCache = CoreSpringFactory.getImpl(QTI21Service.class).getXsltStylesheetCache();
    	AsciiMathHelper asciiMathHelper = new AsciiMathHelper(new XsltStylesheetCacheAdapter(stylesheetCache));
        Map<String, String> upConvertedAsciiMathInput = asciiMathHelper.upConvertAsciiMathInput(asciiMathInput);
        return Response.ok(upConvertedAsciiMathInput).lastModified(new Date()).cacheControl(cc).build();
    }

}
