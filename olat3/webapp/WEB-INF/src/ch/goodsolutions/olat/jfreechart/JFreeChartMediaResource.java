/**
 * JGS goodsolutions GmbH<br>
 * http://www.goodsolutions.ch
 * <p>
 * This software is protected by the goodsolutions software license.<br>
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland.<br>
 * All rights reserved.
 * <p>
 */
package ch.goodsolutions.olat.jfreechart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.olat.core.gui.media.MediaResource;

/**
 * Description:<br>
 * TODO: Mike Stock Class Description for JFreeChartMediaResource
 * 
 * <P>
 * Initial Date:  18.04.2006 <br>
 *
 * @author Mike Stock
 */
public class JFreeChartMediaResource implements MediaResource {

	private static final String CONTENT_TYPE = "image/png";
	private static final Long UNDEF_SIZE = new Long(-1);
	private static final Long UNDEF_LAST_MODIFIED = new Long(-1);
	
	private JFreeChart chart;
	private Long width, height;
	
	public JFreeChartMediaResource(JFreeChart chart, Long width, Long height) {
		this.chart = chart;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	public String getContentType() {
		return CONTENT_TYPE;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getSize()
	 */
	public Long getSize() {
		return UNDEF_SIZE;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	public InputStream getInputStream() {
		ByteArrayInputStream pIn = null;
		try {
			ByteArrayOutputStream pOut = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(pOut, chart, width.intValue(), height.intValue());
			pIn = new ByteArrayInputStream(pOut.toByteArray());
		} catch (IOException e) {
			// bummer...
		}
		return pIn;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
	public Long getLastModified() {
		return UNDEF_LAST_MODIFIED;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	public void prepare(HttpServletResponse hres) {
		// nothing to do...
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	public void release() {
		// nothing to do...
	}

}
