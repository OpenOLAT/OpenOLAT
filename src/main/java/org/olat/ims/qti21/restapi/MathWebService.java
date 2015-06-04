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

import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.SimpleXsltStylesheetCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.qtiworks.mathassess.XsltStylesheetCacheAdapter;
import uk.ac.ed.ph.qtiworks.mathassess.glue.AsciiMathHelper;

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
  
    	XsltStylesheetCache stylesheetCache = new SimpleXsltStylesheetCache();
    	AsciiMathHelper asciiMathHelper = new AsciiMathHelper(new XsltStylesheetCacheAdapter(stylesheetCache));
        Map<String, String> upConvertedAsciiMathInput = asciiMathHelper.upConvertAsciiMathInput(asciiMathInput);
        return Response.ok(upConvertedAsciiMathInput).lastModified(new Date()).cacheControl(cc).build();
    }

}
