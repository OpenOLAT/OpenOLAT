package org.olat.restapi.system;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerInfos;
import org.olat.restapi.system.vo.BigBlueButtonStatisticsVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Initial Date:  2 jul. 2020 <br>
 * @author mjenny, moritz.jenny@frentix.com, http://www.frentix.com
 */

public class BigBlueButtonWebService {
	
	/**
	 * Return the statistics about Big Blue Button
	 * 
	 * @return The statistics about Big Blue Button
	 */
	@GET
	@Operation(summary = "Return the statistics about Big Blue Button", description = "Return the statistics about Big Blue Button")
	@ApiResponse(responseCode = "200", description = "Statistics about the Big Blue Button", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	
	public Response getStatistics() {
		BigBlueButtonStatisticsVO stats = getBigBlueButtonStatistics();
		return Response.ok(stats).build();
	}		

	private BigBlueButtonStatisticsVO getBigBlueButtonStatistics() {
		BigBlueButtonStatisticsVO stats = new BigBlueButtonStatisticsVO();
		BigBlueButtonModule bbbModule = CoreSpringFactory.getImpl(BigBlueButtonModule.class);		
		if(bbbModule.isEnabled()) {
			BigBlueButtonManager bbbManager = CoreSpringFactory.getImpl(BigBlueButtonManager.class);			
			long sumAttendees = 0;
			long sumMeetings = 0;	
			long recordingCount = 0;
			long videoCount = 0;
			long capacity = 0;
			List<BigBlueButtonServerInfos> bbbServerInfos = bbbManager.filterServersInfos(bbbManager.getServersInfos());
			for (int i = 0; i < bbbServerInfos.size(); i++) {
				List<BigBlueButtonMeetingInfos> bbbMeetingInfos = bbbServerInfos.get(i).getMeetingsInfos();			
				for(int j = 0; j < bbbMeetingInfos.size(); j++) {
					if(bbbMeetingInfos.get(j).isRunning()) {
						sumMeetings += 1;	
						videoCount += bbbMeetingInfos.get(j).getVideoCount();
						capacity += bbbMeetingInfos.get(j).getMaxUsers();
						sumAttendees += bbbMeetingInfos.get(j).getParticipantCount();
						if(bbbMeetingInfos.get(j).isRecording()){
							recordingCount += 1;
						}						
					}
				}				
			}					
			stats.setMeetingsCount(sumMeetings);
			stats.setAttendeesCount(sumAttendees);
			stats.setRecordingCount(recordingCount);
			stats.setVideoCount(videoCount);
			stats.setCapacity(capacity);
		}		
		return stats;
	}
}
