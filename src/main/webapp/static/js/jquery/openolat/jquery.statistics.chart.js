(function ( $ ) {
	$.fn.qtiStatistics = function(type, options) {
		var settings = $.extend({
            values: [],
            colors: [],
            cut: null,
            step: null,
            participants: -1,
            mapperUrl: '-',
            barHeight: 40,
            xTopLegend: 'x Top',
            xBottomLegend: 'x Bottom',
            yLeftLegend: 'y Left',
            yRightLegend: 'y Right'
        }, options );

		var d3holder = this;
		drawChart(d3holder, type, settings);
		jQuery(window).resize(function() {
			jQuery(d3holder).empty();
			drawChart(d3holder, type, settings);
		});
        return this;
    };
    
    function drawChart($obj, type, settings) {
    		try {
			if(type == 'histogramScore') {
				histogramScore($obj, settings);
			} else if(type == 'histogramDuration') {
				histogramDuration($obj, settings);
			} else if(type == 'horizontalBarSingleChoice') {
				horizontalBarSingleChoice($obj, settings);
			} else if(type == 'rightAnswerPerItem') {
				rightAnswerPerItem($obj, settings);
			} else if(type == 'averageScorePerItem') {
				averageScorePerItem($obj, settings);
			} else if(type == 'horizontalBarMultipleChoice') {
				horizontalBarMultipleChoice($obj, settings);
			} else if(type == 'horizontalBarMultipleChoiceSurvey') {
				horizontalBarMultipleChoiceSurvey($obj, settings);
			} else if(type == 'highScore') {
				highScore($obj, settings);
			}
		} catch(e) {
			if(window.console) console.log(e);
		}
    }

	function averageScorePerItem($obj, settings) {
		var placeholderwidth = $obj.width();
		var data = settings.values.reverse();
		
		var margin = {top: 10, right: 60, bottom: 40, left: 60},
			width = placeholderwidth - margin.left - margin.right;

		var height = data.length * settings.barHeight;
		$obj.height(height + margin.top + margin.bottom + 'px');

		var x = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d[1]; })])
			.range([0, width]);
		var xAxis = d3.axisBottom(x)
			.ticks(10);
    	
		var y = d3.scaleBand()
			.domain(data.map(function(d) { return d[0]; }))
			.rangeRound([height, 0]);
		var yAxis = d3.axisLeft(y);
    	
		var svg = d3.select('#' + $obj.attr('id')).append('svg')
			.attr('width', width + margin.left + margin.right)
			.attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		svg
			.append('g')
			 .attr('class', 'x axis')
			 .attr('transform', 'translate(0,' + height + ')')
			.call(xAxis)  .append('text')
			 .attr('y', (margin.bottom / 1.7))
			 .attr('x', (width / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xBottomLegend);

		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('rect')
			 .attr('class', 'bar bar_default')
			 .attr('fill', 'bar_default')
			 .attr('x', 0)
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('width', function(d) { return x(d[1]); })
			 .attr('height', y.bandwidth() - 4);

		var f = d3.format(".2");
		var valDy = ((y.bandwidth() / 2) + 3) + "px";
		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3)
			 .attr('dy', valDy)
			 .attr('text-anchor', 'end')
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[1] <= 0) ? '' : f(d[1]); });

		svg
			.append('g')
			 .attr('class', 'y axis')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - (margin.right / 1.7))
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);
    }

	function rightAnswerPerItem($obj, settings) {
		var placeholderwidth = $obj.width();
    	
		var data = settings.values.reverse();
    	
		var margin = {top: 15, right: 60, bottom: 40, left: 60},
			width = placeholderwidth - margin.left - margin.right;
    	
		var height = data.length * settings.barHeight;
		$obj.height(height + margin.top + margin.bottom + 'px');
    	
		var sum =  settings.participants;
    	
		var x = d3.scaleLinear()
			.domain([0, sum])
			.range([0, width]);
		var x2 = d3.scaleLinear()
			.domain([0, 1])
			.range([0, width]);
		var x2Axis = d3.axisBottom(x2)
			.ticks(10, '%');
    	
		var y = d3.scaleBand()
			.domain(data.map(function(d) { return d[0]; }))
			.rangeRound([height, 0]);
		var yAxis = d3.axisLeft(y);
    	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		svg
			.append('g')
			 .attr('class', 'x axis')
			 .attr('transform', 'translate(0,' + height + ')')
			.call(x2Axis)  .append('text')
			 .attr('y', (margin.bottom / 1.7))
			 .attr('x', (width / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xTopLegend);
    	
		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('rect')
			 .attr('class', 'bar bar_green')
			 .attr('fill', 'bar_green')
			 .attr('x', 0)
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('width', function(d) { return x(d[1]); })
			 .attr('height', y.bandwidth() - 4);
    	
		var valDy = ((y.bandwidth() / 2) + 3) + "px";
		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3)
			 .attr('dy', valDy)
			 .attr('text-anchor', 'end')
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    	
		svg
			.append('g')
			 .attr('class', 'y axis')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - (margin.right / 1.7))
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);
    }

	function horizontalBarMultipleChoiceSurvey($obj, settings) {
		var placeholderwidth = $obj.width();
		var data = settings.values.reverse();
    	
		var margin = {top: 40, right: 15, bottom: 40, left: 40};
		var height = data.length * settings.barHeight;
		$obj.height(height + margin.top + margin.bottom + 'px');
		var width = placeholderwidth - margin.left - margin.right;
    	
		var sum =  settings.participants;
		var max = sum;

		var x = d3.scaleLinear()
			.domain([0, max])
			.range([0, width]);
		var x2 = d3.scaleLinear()
			.domain([0, 1])
			.range([0, width]);
		var x2Axis = d3.axisBottom(x2)
			.ticks(10, '%');
    	
		var y = d3.scaleBand()
			.domain(data.map(function(d) { return d[0]; }))
			.rangeRound([height, 0]);
		var yAxis = d3.axisLeft(y);
    	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		svg
			.append('g')
			 .attr('class', 'x axis')
			 .attr('transform', 'translate(0,' + height + ')')
			.call(x2Axis)
			.append('text')
			 .attr('y', (margin.bottom / 1.7))
			 .attr('x', (width / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xBottomLegend);
    	
		svg
			.append('g')
			 .attr('class', 'y axis')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - margin.left)
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);

		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('rect')
			 .attr('class', 'bar bar_default')
			 .attr('fill', 'bar_default')
			 .attr('x', 0)
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('width', function(d) { return x(d[1]); })
			 .attr('height', y.bandwidth() - 4);
    	
		var valDy = ((y.bandwidth() / 2) + 3) + "px";
		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3) // padding-left
			 .attr('dy', valDy) // vertical-align: middle
			 .attr('text-anchor', 'end') // text-align: right
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    }

	function horizontalBarMultipleChoice($obj, settings) {
		var placeholderwidth = $obj.width();
    	
		var data = settings.values.reverse();
    	
		var margin = {top: 40, right: 15, bottom: 40, left: 40};
		var height = data.length * settings.barHeight;
		$obj.height(height + margin.top + margin.bottom + 'px');
		var width = placeholderwidth - margin.left - margin.right;
    	
		var sum = settings.participants;
		var max = sum;

		var x = d3.scaleLinear()
			.domain([0, max])
			.range([0, width]);
 		var x2 = d3.scaleLinear()
 			.domain([0, 1])
 			.range([0, width]);
 		var x2Axis = d3.axisBottom(x2)
 			.ticks(10, '%');
    	
		var y = d3.scaleBand()
			.domain(data.map(function(d) { return d[0]; }))
			.rangeRound([height, 0]);
		var yAxis = d3.axisLeft(y);
    	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		svg
			.append('g')
			 .attr('class', 'x axis')
			 .attr('transform', 'translate(0,' + height + ')')
			.call(x2Axis)
			.append('text')
			 .attr('y', (margin.bottom / 1.7))
			 .attr('x', (width / 2))
			 .attr('dx', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xBottomlegend);
    	
		svg
			.append('g')
			 .attr('class', 'y axis')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - margin.left)
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);

		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('rect')
			 .attr('class', 'bar bar_green')
			 .attr('fill', 'bar_green')
			 .attr('x', 0)
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('width', function(d) { return x(d[1]); })
			 .attr('height', y.bandwidth() - 4);
    	
		var valDy = ((y.bandwidth() / 2) + 3) + "px";
		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3)
			 .attr('dy', valDy)
			 .attr('text-anchor', 'end')
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    	
		svg.selectAll('.bar1')
			.data(data)
			.enter()
			.append('rect')
			 .attr('class', 'bar bar_red')
			 .attr('fill', 'bar_red')
			 .attr('x', function(d) { return x(d[1]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('width', function(d) { return x(d[2]); })
			 .attr('height', y.bandwidth() - 4);
    	
		svg.selectAll('.bar1')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1] + d[2]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3)
			 .attr('dy', valDy)
			 .attr('text-anchor', 'end')
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[2] <= 0) ? '' : d[2]; });
    	
		svg.selectAll('.bar2')
			.data(data)
			.enter()
			.append('rect')
			 .attr('class', 'bar bar_grey')
			 .attr('fill', 'bar_grey')
			 .attr('x', function(d) { return x(d[1] + d[2]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('width', function(d) { return x(d[3]); })
			 .attr('height', y.bandwidth() - 4);
    	
		svg.selectAll('.bar2')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1] + d[2] + d[3]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3) // padding-left
			 .attr('dy', valDy) // vertical-align: middle
			 .attr('text-anchor', 'end') // text-align: right
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[3] <= 0) ? '' : d[3]; });
    }

	function horizontalBarSingleChoice($obj, settings) {
		var placeholderwidth = $obj.width();
    	
		var data = settings.values.reverse();
		var colors = settings.colors.reverse();
    	
		var margin = {top: 15, right: 15, bottom: 40, left: 40};
		var height = data.length * settings.barHeight;
		$obj.height(height + margin.top + margin.bottom + 'px');
		var width = placeholderwidth - margin.left - margin.right;
    	
		var sum = settings.participants;
		var max = sum;

		var x = d3.scaleLinear()
			.domain([0, max])
			.range([0, width]);
		var x2 = d3.scaleLinear()
			.domain([0, 1])
			.range([0, width]);
		var x2Axis = d3.axisBottom(x2)
			.ticks(10, '%');
    	
		var y = d3.scaleBand()
			.domain(data.map(function(d) { return d[0]; }))
			.rangeRound([height, 0]);
		var yAxis = d3.axisLeft(y);
    	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
		svg
			.append('g')
			 .attr('class', 'x axis')
			 .attr('transform', 'translate(0,' + height + ')')
			.call(x2Axis)
			.append('text')
			 .attr('y', (margin.bottom / 1.7))
			 .attr('x', (width / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xBottomLegend);

		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('rect')
	    	     .attr('class', function(d, i){
	    	    	 	if(colors == null) return 'bar bar0 bar_default';
	    	    	 	else if(colors.length > i) return colors[i];
	    	    	 	else return 'bar bar0 bar_default';
	    	     })
	    	     .attr('fill', 'bar_green')
	    	     .attr('x', 0)
	    	     .attr('y', function(d) { return y(d[0]) + 2; })
	    	     .attr('width', function(d) { return x(d[1]); })
	    	     .attr('height', y.bandwidth() - 4);
    	
		var valDy = ((y.bandwidth() / 2) + 3) + "px";
		svg.selectAll('.bar0')
			.data(data)
			.enter()
			.append('text')
			 .attr('x', function(d) { return x(d[1]); })
			 .attr('y', function(d) { return y(d[0]) + 2; })
			 .attr('dx', -3) // padding-left
			 .attr('dy', valDy) // vertical-align: middle
			 .attr('text-anchor', 'end') // text-align: right
			 .attr('fill', 'rgb(48,48,48)')
			 .attr('stroke', 'none')
			 .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    	
		svg
			.append('g')
			 .attr('class', 'y axis')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - margin.left)
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);
    }

	function histogramDuration($obj, settings) {
		var placeholderheight = $obj.height();
		var placeholderwidth = $obj.width();
		var values = settings.values;
		var maxTime = d3.max(values, function(d) { return d; });

		var ref = new Date(2012, 0, 1, 0, 0);
    	
		var formatMinutes = undefined;
		if(maxTime < 10) {
			var formatTime = d3.timeFormat('%M:%S:%L');
			formatMinutes = function(d) { return formatTime(new Date(ref.getTime() + (d * 1000))); };
		} else if(maxTime < 3600) {
			var formatTimeShort = d3.timeFormat('%M:%S');
			formatMinutes = function(d) { return formatTimeShort(new Date(ref.getTime() + (d * 1000))); };
		} else {
			formatMinutes = function(d) { return Math.round( d / 60 ); }
		}
    	  
		var margin = {top: 10, right: 60, bottom: 40, left: 60},
			width = placeholderwidth - margin.left - margin.right,
			height = placeholderheight - margin.top - margin.bottom;
		
		var maxTimeLeft = (maxTime * 0.1);
    	  
		var x = d3.scaleLinear()
			.domain([0, maxTime + maxTimeLeft])
			.range([0, width]);
    	
		var data = d3.histogram()
			.domain(x.domain())
			.thresholds(x.ticks())
			(values);
    	
		var sum = d3.sum(data, function(d) { return d.length; });
		var y = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d.length; })])
			.range([height, 0]);
    	
		var y2 = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d.length / sum; })])
			.range([height, 0]);
    	
		var xAxis = d3.axisBottom(x)
			.tickFormat(formatMinutes);
    	
		var yAxis = d3.axisRight(y)
			.ticks(10);
    	
		var y2Axis = d3.axisLeft(y2)
			.ticks(10, '%');
    	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
		svg.selectAll('.bar')
			.data(data)
			.enter()
			.append('g')
			 .attr('class', 'bar bar_default')
			 .attr('transform', function(d) { return 'translate(' + x(d.x0) + ',' + y(d.length) + ')'; })
			.append('rect')
			 .attr('x', 2)
			 .attr('width', function(d) {
				var wx = x(data[0].x1) - x(data[0].x0) - 4;
				 var mx = x(d.x0);
				 if(mx + wx > x(maxTime + maxTimeLeft)) {
					 wx = wx - (mx + wx - x(maxTime + maxTimeLeft)) - 2;
				 }
				return wx;
			 })
			 .attr('height', function(d) { return height - y(d.length); });

		svg
			.append('g')
			 .attr('class', 'y axis')
			.call(y2Axis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - margin.left)
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);

		svg
			.append('g')
			 .attr('class', 'x axis')
			 .attr('transform', 'translate(0,' + height + ')')
			.call(xAxis)
			.append('text')
			 .attr('y', (margin.bottom / 1.1))
			 .attr('x', (width / 2))
			 .attr('dx', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xBottomLegend);

		svg
			.append('g')
			 .attr('class', 'y axis')
			 .attr('transform', 'translate(' + width + ',0)')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(90)')
			 .attr('y', 0 - (margin.right))
			 .attr('x', (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yRightLegend);
    }

	function histogramScore($obj, settings) {
		var placeholderheight = $obj.height();
		var placeholderwidth = $obj.width();
		var values = settings.values;

		var margin = {top: 10, right: 60, bottom: 40, left: 60},
			width = placeholderwidth - margin.left - margin.right,
			height = placeholderheight - margin.top - margin.bottom;
    	
		var cut = settings.cut;	
		var maxScore = d3.max(values, function(d) { return d; });
		if(maxScore < 1.0) {
			maxScore = 1.0;
		}
		var maxScoreLeft = (maxScore * 0.1);
		if(maxScoreLeft < 1.0) {
			maxScoreLeft = 1.0;
		}
		var x = d3.scaleLinear()
			.domain([0, maxScore + maxScoreLeft])
			.range([0, width]);
		
		var ticks = maxScore < 15 ? x.ticks(maxScore) : x.ticks();
		var data = d3.histogram()
			.domain(x.domain())
			.thresholds(ticks)(values);

		var sum = d3.sum(data, function(d) { return d.length; });
    	
		var y = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d.length; })])
			.range([height, 0]);
    	
		var y2 = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d.length / sum; })])
			.range([height, 0]);
    	
		var xAxis = d3.axisBottom(x);
		if(maxScore < 15) {
			xAxis = xAxis.ticks(maxScore);
		} else {
			xAxis = xAxis.ticks();
		}
		xAxis = xAxis.tickFormat(d3.format('d'));//.1f
    	
		var yAxis = d3.axisRight(y)
			.ticks(10);

		var y2Axis = d3.axisLeft(y2)
			.ticks(10, '%');
    	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		svg.selectAll('.bar')
			.data(data)
			.enter()
			.append('g')
			 .attr('class', function(d, i) {
				 if(cut == null) return 'bar bar_default';
				 else if(data[i].x < cut) return 'bar bar_red';
				 else return 'bar bar_green';
			 })
			 .attr('transform', function(d) {
				 var mx = x(d.x0) - 2;
				 return 'translate(' + mx + ',' + y(d.length) + ')';
			 })
			.append('rect')
			 .attr('x', 2)
			 .attr('width', function(d) {
				 var wx = x(data[0].x1) - x(data[0].x0) - 2;
				 var mx = x(d.x0);
				 if(mx + wx > x(maxScore + maxScoreLeft)) {
					 wx = wx - (mx + wx - x(maxScore + maxScoreLeft));
				 }
				 return wx;
			 })
			 .attr('height', function(d) { return height - y(d.length); });
    	
		svg.append('g')
			.attr('class', 'y axis')
			.call(y2Axis)
			.append('text')
			 .attr('transform', 'rotate(-90)')
			 .attr('y', 0 - margin.left)
			 .attr('x', 0 - (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yLeftLegend);
    	
		svg.append('g')
			.attr('class', 'x axis')
			.attr('transform', 'translate(0,' + height + ')')
			.call(xAxis)
			.append('text')
			 .attr('y', (margin.bottom / 1.1))
			 .attr('x', (width / 2))
			 .attr('dx', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.xBottomLegend);
    	
		svg.append('g')
			.attr('class', 'y axis')
			.attr('transform', 'translate(' + width + ',0)')
			.call(yAxis)
			.append('text')
			 .attr('transform', 'rotate(90)')
			 .attr('y', 0 - (margin.right))
			 .attr('x', (height / 2))
			 .attr('dy', '1em')
			 .attr('fill', '#000')
			 .style('text-anchor', 'middle')
			 .text(settings.yRightLegend);
    }

	function highScore($obj, settings) {
		var placeholderheight = $obj.height();
		var placeholderwidth = $obj.width();
		var values = settings.values;
    	    	
		var margin = {top: 20, right: 60, bottom: 40, left: 60},
			width = placeholderwidth - margin.left - margin.right,
			height = placeholderheight - margin.top - margin.bottom;
    	
		var cut = settings.cut;
		var maxScore = d3.max(values, function(d) { return d; });
		var minScore = d3.min(values, function(d) { return d; });

		var step = 1;
		if (settings.step != null) {
			step = settings.step;
		}
		var max = maxScore + step;
		var min = minScore - step;
		var x = d3.scaleLinear()
			.domain([min, max])
			.range([0, width]);
    	  	
		var range = d3.range(min, max + step, step)
    	
		var data = d3.histogram()
			.thresholds(range)(values);
    	
		var sum = d3.sum(data, function(d) { return d.length; });
    	
		var y = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d.length; })])
			.range([height, 0]);
    	
		var y2 = d3.scaleLinear()
			.domain([0, d3.max(data, function(d) { return d.length / sum; })])
			.range([height, 0]);
    	
		var xAxis = d3.axisBottom(x)
			.tickValues(range)
			.tickFormat(d3.format("d"));
    	
		var yAxis = d3.axisRight(y)
			.ticks(10);
		var y2Axis = d3.axisLeft(y2)
			.ticks(10, '%');
	  	
		var svg = d3.select('#' + $obj.attr('id'))
			.append('svg')
			 .attr('width', width + margin.left + margin.right)
			 .attr('height', height + margin.top + margin.bottom)
			.append('g')
			 .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

		var value = d3.range(min, max + step, step);
  		svg.selectAll('.bar')
  			.data(data)
  			.enter()
  			.append('g')
  			 .attr('class', function(d, i) {
  				 if(cut == null) return 'o_empty';
  				 else if(data[i].x == cut) return 'o_myself';
  				 else return 'o_other';	  		  
  			 })
  			 .attr('transform', function(d) { return 'translate(' + x(d.x0) + ',' + y(d.length) + ')'; })
  			.append('rect')
  			 .attr('x', function (d,i){ return - (width / value.length)/2; })
  			 .attr('y', function (d){ return height - y(d.length); })
  			 .attr('width', (width / range.length))
  			 .attr('height', function (d){return 0;})
  			.style('opacity', 0)
  			 .transition().delay(function (d,i){ return i*200/(max-min);})
  			 .duration(800)
  			 .attr('y', function(d) { return 0; })
  			 .attr('height', function(d) { return height - y(d.length); })
  			.style('opacity', 1);

  		svg
  			.append('g')
  			 .attr('class', 'y axis')
  			 .call(y2Axis)
  			.append('text')
  			 .attr('transform', 'rotate(-90)')
  			 .attr('y', 0 - margin.left)
  			 .attr('x', 0 - (height / 2))
  			 .attr('dy', '1em')
			 .attr('fill', '#000')
  			 .style('text-anchor', 'middle')
  			 .text(settings.yLeftLegend);
	  	
  		svg
  			.append('g')
  			 .attr('class', 'x axis')
  			 .attr('transform', 'translate(0,' + height + ')')
  			.call(xAxis)
  			.append('text')
  			 .attr('y', (margin.bottom / 1.1))
  			 .attr('x', (width / 2))
  			 .attr('dx', '1em')
			 .attr('fill', '#000')
  			 .style('text-anchor', 'middle')
  			 .text(settings.xBottomLegend);
	  	
  		svg
  			.append('g')
  			 .attr('class', 'y axis')
  			 .attr('transform', 'translate(' + width + ',0)')
  			.call(yAxis)
  			.append('text')
  			 .attr('transform', 'rotate(90)')
  			 .attr('y', 0 - (margin.right))
  			 .attr('x', (height / 2))
  			 .attr('dy', '1em')
			 .attr('fill', '#000')
  			 .style('text-anchor', 'middle')
  			 .text(settings.yRightLegend);
	}
}( jQuery ));
