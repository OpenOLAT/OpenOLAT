(function ($) {
	"use strict";
	$.fn.timeline = function(options) {
		var settings = this.data("data-oo-timeline");
		if(typeof timeline === "undefined") {
			settings = $.extend({
 				parentContainerId:'',
 				startTime: null,
 				endTime: null,
 				dateFormat: '%d.%m.%y',
 				dateLabel: 'Date',
 				status: { draft: "Draft", published: "Published", inRevision: "In revision", closed: "Closed", deleted: "Deleted"},
 				values: []
 			}, options);
    	
			this.data("data-oo-timeline", settings);
			createButtons(this, settings);
			createGraph(this, settings);
			timelineItems(this, settings);
			addHandlers(this, settings);
		}
        return this;
	};

	var svg;
	var maxCurveY = 10;
	var x, y, xAxis, yAxis;
	var margin, width, height;
	var lineGenerator, lineX, lineY, curveY;
	var formatDates;
	var startTime, endTime;
	var data, statusTranslations;
	var slideDelta = (183 * 24 * 60 * 60 * 1000);//six month in milliseconds
	var sliding = false;
	
	function createButtons($obj, settings) {
		sliding = false;
		var parentContainer = jQuery('#' + settings.parentContainerId);
		parentContainer.prepend( "<div class='o_timeline_up'><a href='javascript:;' onclick=''><i class='o_icon o_icon-lg o_icon o_icon_slide_up'> </i></a></div>" );
		parentContainer.append("<div class='o_timeline_down'><a href='javascript:;' onclick=''><i class='o_icon o_icon-lg o_icon o_icon_slide_down'> </i></a></div>")
	}

	function createGraph ($obj, settings) {
		var parentContainer = jQuery('#' + settings.parentContainerId);
		var placeholderwidth = parentContainer.width();
		$obj.width(placeholderwidth);
		var placeholderheight = $obj.height();
		if(parentContainer.height() < 500) {
			placeholderheight = 500;
		}
		$obj.height(placeholderheight);
		
		data = settings.values;
		endTime = settings.endTime;
		startTime = settings.startTime;
		statusTranslations = settings.status;
	
		margin = {top: 10, right: 60, bottom: 40, left: 80};
  	  	width = placeholderwidth - margin.left - margin.right;
  	  	height = placeholderheight - margin.top - margin.bottom;
  	  	
  	  	var formatDate = d3.timeFormat(settings.dateFormat);
  	  	formatDates = function(d) { return formatDate(new Date(d)); };

		x = d3.scaleLinear()
	  		.domain([0, 1])
	  		.range([0, width]);
	  		
	  	xAxis = d3.axisBottom(x)
	  		.ticks(0);
	  		
	  	var minTime = settings.startTime == null ? d3.min(data, function(d) { return d.time; }) : settings.startTime;
	  	var maxTime = settings.endTime == null ? d3.max(data, function(d) { return d.time; }) : settings.endTime;
    	
		y = d3.scaleLinear()
  	  		.domain([minTime, maxTime])
  	  		.range([height, 0]);

		yAxis = d3.axisLeft(y)
  	  		.ticks(5)
  	  		.tickFormat(formatDates)
  	  		.tickSize(-width, 5);

  	  	svg = d3.select('#' + $obj.attr('id')).append('svg')
  	  		.attr('width', width + margin.left + margin.right)
  	  		.attr('height', height + margin.top + margin.bottom)
  	  		.append('g')
  	  		.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		lineX = d3.scaleLinear().domain([0, 1]).range([0, width]);
		lineY = d3.scaleLinear().domain([0, maxCurveY]).range([height, 0]);
		curveY = d3.scaleLinear().domain([minTime, maxTime]).range([0, maxCurveY]);

		lineGenerator = d3.line()
		     .x(function(d) { return lineX(curvedX(d.y)); })
		     .y(function(d) { return lineY(d.y); })
		     .curve(d3.curveLinear);
	}
	
	function addHandlers($obj, settings) {
		jQuery('#' + settings.parentContainerId + " .o_timeline_up a").on('click', function() {
			slideUp();
		});
		jQuery('#' + settings.parentContainerId + " .o_timeline_down a").on('click', function() {
			slideDown();
		});
	}
	
	function slideUp() {
		if(sliding) return;
		
		sliding = true;
		endTime = endTime + slideDelta;
		startTime = startTime + slideDelta;
		updateTimeline(true);
	}
	
	function slideDown() {
		if(sliding) return;
		
		sliding = true;
		endTime = endTime - slideDelta;
		startTime = startTime - slideDelta;
		updateTimeline(false);
	}
	
	function updateTimeline(up) {
		try {
			curveY.domain([startTime, endTime]);
			y.domain([startTime, endTime]);

			drawAxis();
			drawDots(up);
		} catch(e) {
			if(window.console) console.log(e);
		}
	}
	
	function drawAxis() {
  		svg.select('.y').call(yAxis);
	}
	
	function drawDots(up) {	
		var dots = svg.selectAll('.dot')
			.data(data, idKey);
		
		dots
			.transition()
			.duration(1000)
			.ease(d3.easeLinear)
			.attrTween("cx", function(d) {
				return function(t) {
					var delta = (1.0 - t) * slideDelta;
					var time = up ? (d.time + delta) : (d.time - delta);
					return lineX(curvedX(curveY(time)));
				};
			})
		   	.attr("cy", function(d) { return y(d.time); })
		   	.on("end", function() {
		   		sliding = false;
		   	});
	}
  
	function timelineItems($obj, settings) {
		// x-axis
		svg.append("g")
		   .attr("class", "x axis")
		   .attr("transform", "translate(0," + height + ")")
		   .call(xAxis);

		// y-axis
		svg.append("g")
		   .attr("class", "y axis")
		   .call(yAxis)
		  .append("text")
		   .attr("class", "label")
		   .attr("transform", "rotate(-90)")
		   .attr("y", 6)
		   .attr("dy", ".71em")
		   .attr("dx", "-.71em")
		   .style("text-anchor", "end")
		   .text("Date");
		   
		var lineData = [];
		for(var i=0; i<(maxCurveY*20); i++) {
			lineData.push({y: (i == 0 ? 0 : i / 20.0)});
		}
		  
		var linePath = svg.append("path")
		   .data([lineData])
		   .attr("d", lineGenerator)
		   .attr("class", "o_timeline_curve");

		// draw dots
		var dots = svg.selectAll(".dot")
		   .data(data, idKey);

		dots.enter()
		   .append('g')
		   .append("circle")
		   .attr("r", function(d) { return (7 + Math.floor(Math.random() * 5)); })
		   .attr("id", idKey)
		   .attr("class", function(d) { return "dot o_pf_status_" + d.status; })
		   .attr("cx", function(d) { return lineX(curvedX(curveY(d.time))); })
		   .attr("cy", function(d) { return y(d.time); });
		   
		jQuery("g .dot").tooltip({
					html: true,
					container:'body',
					title: function() {
						var id = jQuery(this).attr('id');
						var row;
						for(var i=data.length; i-->0; ) {
							if(id == data[i].id) {
								row = data[i];
							}
						}
						var translatedStatus = statusTranslations[row.status];
						return '<p>' + row.title + '<br>Status: ' + translatedStatus + '</p>';
					}
			});
		dots.exit();
	}
	
	function idKey(d) {
  		return d.id;
	}
	
	function curvedX(cy) {
		return (1.5 + Math.sin(cy * 1.1)) / 3;
	}
}( jQuery ));