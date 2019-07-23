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
package org.olat.restapi.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WorkThreadInformations;
import org.olat.restapi.system.vo.DateParam;
import org.olat.restapi.system.vo.MemoryPoolVO;
import org.olat.restapi.system.vo.MemorySampleVO;
import org.olat.restapi.system.vo.MemoryVO;


/**
 * 
 * <h3>Description:</h3>

 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class MemoryWebService implements Sampler {
	
	private static final Logger log = Tracing.createLoggerFor(MemoryWebService.class);
	
	private List<MemorySampleVO> memorySamples = new ArrayList<>(100);
	
	public MemoryWebService() {
		//start sampling
	}
	
	@Override
	public void takeSample() {
		MemorySampleVO sample = new MemorySampleVO();
		sample.setDate(new Date());
		sample.setMemory(createMemoryVO());
		sample.setMemoryPools(createMemoryPools());
		while(memorySamples.size() >= 1000) {
			memorySamples.remove(0);
		}
		memorySamples.add(sample);
		
		Runtime r = Runtime.getRuntime();
		if(r.freeMemory() < 20000000) {
			List<String> currentThreadNames = new ArrayList<>();
			WorkThreadInformations.currentThreadNames(currentThreadNames);
			for(String currentThreadName:currentThreadNames) {
				String currentWork = WorkThreadInformations.get(currentThreadName);
				log.info("High memory usage:" + currentThreadName + " work at: " + currentWork);
			}
		}
	}

	/**
	 * Return informations about memory.
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc Informations about memory
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @param request The HTTP request
	 * @return The informations about the memory
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMemory() {	
		MemoryVO vo = createMemoryVO();
		return Response.ok(vo.toString()).build();
	}
	
	/**
	 * Return some informations about memory.
	 * @response.representation.200.qname {http://www.example.com}memoryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc A short summary of the number of classes
   * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_MEMORYVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @param request The HTTP request
	 * @return The informations about the memory
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMemoryXml() {	
		MemoryVO vo = createMemoryVO();
		return Response.ok(vo).build();
	}
	
	private MemoryVO createMemoryVO() {
		Runtime r = Runtime.getRuntime();
		long totalMem = r.totalMemory();
		// Total used memory in megabytes
		long totalUsed = (totalMem - r.freeMemory()) / 1000000; 
		// Max available memory in VM in megabytes
		long maxAvailable = r.maxMemory() / 1000000; 
		MemoryVO vo = new MemoryVO(totalMem/1000000, totalUsed, maxAvailable);
		return vo;
	}

	@GET
	@Path("pools")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMemoryPools() {
		MemoryPoolVO[] voes = createMemoryPools();
		StringBuilder sb = new StringBuilder();
		for(MemoryPoolVO vo:voes) {
			sb.append(vo.getName()).append(" - ").append(vo.getType()).append(" - ").append(vo.getUsage());
		}
		return Response.ok(sb.toString()).build();
	}
	
	@GET
	@Path("pools")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMemoryPoolsXml() {
		MemoryPoolVO[] voes = createMemoryPools();
		return Response.ok(voes).build();
	}
	
	@GET
	@Path("samples")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSamplesXml(@QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("lastSamples") Integer maxResults) {
		List<MemorySampleVO> samples = new ArrayList<>(memorySamples);
		
		Date fromDate = null;
		if(StringHelper.containsNonWhitespace(from)) {
			fromDate = parseDate(from);
		}
		Date toDate = null;
		if(StringHelper.containsNonWhitespace(to)) {
			toDate = parseDate(to);
		}
		
		if(fromDate != null || toDate != null) {
			for(Iterator<MemorySampleVO> sampleIt=samples.iterator(); sampleIt.hasNext();) {
				MemorySampleVO sample = sampleIt.next();
				if((fromDate != null && fromDate.compareTo(sample.getDate()) > 0) ||
						(toDate != null && sample.getDate().compareTo(toDate) > 0)) {
					sampleIt.remove();
				}
			}
		}
		
		if(maxResults != null) {
			int fromIndex = Math.max(0, memorySamples.size() - maxResults);
			samples = samples.subList(fromIndex, memorySamples.size());
		}
		
		MemorySampleVO[] voes = new MemorySampleVO[0];
		voes = samples.toArray(voes);
		return Response.ok(voes).build();
	}
	
	private static Date parseDate(String dateStr) {
		return new DateParam(dateStr).getDate();
	}
	
	private MemoryPoolVO[] createMemoryPools() {
		List<MemoryPoolMXBean> memoryPool = ManagementFactory.getMemoryPoolMXBeans();
		MemoryPoolVO[] voes = new MemoryPoolVO[memoryPool.size()];
		int count = 0;
		for(MemoryPoolMXBean bean:memoryPool) {
			if(bean.isValid()) {
				voes[count++] = new MemoryPoolVO(bean);
			}
		}
		return voes;
	}
}
