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
    	
    	try {
    		if(type == 'histogramScore') {
        		histogramScore(this, settings);
        	} else if(type == 'histogramDuration') {
        		histogramDuration(this, settings);
        	} else if(type == 'horizontalBarSingleChoice') {
        		horizontalBarSingleChoice(this, settings);
        	} else if(type == 'rightAnswerPerItem') {
        		rightAnswerPerItem(this, settings);
        	} else if(type == 'averageScorePerItem') {
        		averageScorePerItem(this, settings);
        	} else if(type == 'horizontalBarMultipleChoice') {
        		horizontalBarMultipleChoice(this, settings);
        	} else if(type == 'horizontalBarMultipleChoiceSurvey') {
        		horizontalBarMultipleChoiceSurvey(this, settings);
        	} else if(type == 'highScore') {
        		highScore(this, settings);
        	}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    averageScorePerItem = function($obj, settings) {
    	var placeholderwidth = $obj.width();
    	var data = settings.values.reverse();

    	var placeholderheight = $obj.height();
    	var margin = {top: 10, right: 60, bottom: 40, left: 60},
    	   width = placeholderwidth - margin.left - margin.right;

    	var height = data.length * settings.barHeight;
    	$obj.height(height + margin.top + margin.bottom + 'px');

    	var x = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d[1]; })])
    	   .range([0, width]);
    	var xAxis = d3.svg.axis()
    	   .scale(x)
    	   .orient('bottom')
    	   .ticks(10);
    	
    	var y = d3.scale.ordinal()
    	  .domain(data.map(function(d) { return d[0]; }))
    	   .rangeRoundBands([height, 0]);
    	var yAxis = d3.svg.axis()
    	   .scale(y)
    	   .orient('left');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	    .attr('width', width + margin.left + margin.right)
    	    .attr('height', height + margin.top + margin.bottom)
    	   .append('g')
    	    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
    	svg.append('g')
    	    .attr('class', 'x axis')
    	    .attr('transform', 'translate(0,' + height + ')')
    	   .call(xAxis)  .append('text')
    	    .attr('y', (margin.bottom / 1.7))
    	    .attr('x', (width / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.xBottomLegend);
    	
    	svg.selectAll('.bar0')
    	    .data(data)
    	  .enter().append('rect')
    	    .attr('class', 'bar bar_default')
    	    .attr('fill', 'bar_default')
    	    .attr('x', 0)
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[1]); })
    	    .attr('height', y.rangeBand() - 4);
    	
       	var valDy = ((y.rangeBand() / 2) + 3) + "px";
    	svg.selectAll('.bar0')
    	     .data(data)
    	   .enter().append('text')
    	    .attr('x', function(d) { return x(d[1]); })
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('dx', -3)
    	    .attr('dy', valDy)
    	    .attr('text-anchor', 'end')
    	    .attr('fill', 'rgb(48,48,48)')
    	    .attr('stroke', 'none')
    	    .text(function(d) { return (d[1] <= 0) ? '' : d3.round(d[1], 2); });
    	
    	svg.append('g')
    	    .attr('class', 'y axis')
    	    .call(yAxis)
    	   .append('text')
    	    .attr('transform', 'rotate(-90)')
    	    .attr('y', 0 - (margin.right / 1.7))
    	    .attr('x', 0 - (height / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.yLeftLegend);
    }
    
    rightAnswerPerItem = function($obj, settings) {
    	var placeholderheight = $obj.height();
    	var placeholderwidth = $obj.width();
    	
    	var data = settings.values.reverse();
    	
    	var margin = {top: 15, right: 60, bottom: 40, left: 60},
    	   width = placeholderwidth - margin.left - margin.right;
    	
    	var height = data.length * settings.barHeight;
    	$obj.height(height + margin.top + margin.bottom + 'px');
    	
    	var sum =  settings.participants;
    	
    	var x = d3.scale.linear()
    	  .domain([0, sum])
    	   .range([0, width]);
    	var x2 = d3.scale.linear()
    	  .domain([0, 1])
    	   .range([0, width]);
    	var x2Axis = d3.svg.axis()
    	   .scale(x2)
    	   .orient('bottom')
    	   .ticks(10, '%');
    	
    	var y = d3.scale.ordinal()
    	  .domain(data.map(function(d) { return d[0]; }))
    	   .rangeRoundBands([height, 0]);
    	var yAxis = d3.svg.axis()
    	   .scale(y)
    	   .orient('left');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	   .attr('width', width + margin.left + margin.right)
    	   .attr('height', height + margin.top + margin.bottom)
    	  .append('g')
    	   .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    	svg.append('g')
    	     .attr('class', 'x axis')
    	     .attr('transform', 'translate(0,' + height + ')')
    	     .call(x2Axis)  .append('text')
    	    .attr('y', (margin.bottom / 1.7))
    	    .attr('x', (width / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.xTopLegend);
    	
    	svg.selectAll('.bar0')
    	    .data(data)
    	  .enter().append('rect')
    	    .attr('class', 'bar bar_green')
    	    .attr('fill', 'bar_green')
    	    .attr('x', 0)
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[1]); })
    	    .attr('height', y.rangeBand() - 4);
    	
    	var valDy = ((y.rangeBand() / 2) + 3) + "px";
    	svg.selectAll('.bar0')
    	     .data(data)
    	   .enter().append('text')
    	    .attr('x', function(d) { return x(d[1]); })
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('dx', -3)
    	    .attr('dy', valDy)
    	    .attr('text-anchor', 'end')
    	    .attr('fill', 'rgb(48,48,48)')
    	    .attr('stroke', 'none')
    	    .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    	
    	svg.append('g')
    	    .attr('class', 'y axis')
    	   .call(yAxis)
    	    .append('text')
    	    .attr('transform', 'rotate(-90)')
    	    .attr('y', 0 - (margin.right / 1.7))
    	    .attr('x', 0 - (height / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.yLeftLegend);
    };
    
    horizontalBarMultipleChoiceSurvey = function($obj, settings) {
    	var placeholderwidth = $obj.width();
    	var data = settings.values.reverse();
    	
    	var margin = {top: 40, right: 15, bottom: 40, left: 40};
    	var height = data.length * settings.barHeight;
    	$obj.height(height + margin.top + margin.bottom + 'px');
    	var width = placeholderwidth - margin.left - margin.right;
    	
    	var sum =  settings.participants;
    	var max = sum;

    	var x = d3.scale.linear()
    	  .domain([0, max])
    	   .range([0, width]);
    	var x2 = d3.scale.linear()
    	  .domain([0, 1])
    	   .range([0, width]);
    	var x2Axis = d3.svg.axis()
    	   .scale(x2)
    	   .orient('bottom')
    	   .ticks(10, '%');
    	
    	var y = d3.scale.ordinal()
    	  .domain(data.map(function(d) { return d[0]; }))
    	   .rangeRoundBands([height, 0]);
    	var yAxis = d3.svg.axis()
    	   .scale(y)
    	   .orient('left');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	   .attr('width', width + margin.left + margin.right)
    	   .attr('height', height + margin.top + margin.bottom)
    	  .append('g')
    	   .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
    	svg.append('g')
    	     .attr('class', 'x axis')
    	     .attr('transform', 'translate(0,' + height + ')')
    	     .call(x2Axis)  .append('text')
    	    .attr('y', (margin.bottom / 1.7))
    	    .attr('x', (width / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.xBottomLegend);
    	
    	svg.append('g')
    	    .attr('class', 'y axis')
    	   .call(yAxis)
    	    .append('text')
    	    .attr('transform', 'rotate(-90)')
    	    .attr('y', 0 - margin.left)
    	    .attr('x', 0 - (height / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.yLeftLegend);

    	svg.selectAll('.bar0')
    	    .data(data)
    	  .enter().append('rect')
    	    .attr('class', 'bar bar_default')
    	    .attr('fill', 'bar_default')
    	    .attr('x', 0)
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[1]); })
    	    .attr('height', y.rangeBand() - 4);
    	
    	var valDy = ((y.rangeBand() / 2) + 3) + "px";
    	svg.selectAll('.bar0')
    	     .data(data)
    	   .enter().append('text')
    	    .attr('x', function(d) { return x(d[1]); })
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('dx', -3) // padding-left
    	    .attr('dy', valDy) // vertical-align: middle
    	    .attr('text-anchor', 'end') // text-align: right
    	    .attr('fill', 'rgb(48,48,48)')
    	    .attr('stroke', 'none')
    	    .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    };
    
    horizontalBarMultipleChoice = function($obj, settings) {
    	var placeholderwidth = $obj.width();
    	
    	var data = settings.values.reverse();
    	
    	var margin = {top: 40, right: 15, bottom: 40, left: 40};
    	var height = data.length * settings.barHeight;
    	$obj.height(height + margin.top + margin.bottom + 'px');
    	var width = placeholderwidth - margin.left - margin.right;
    	
    	var sum = settings.participants;
    	var max = sum;

    	var x = d3.scale.linear()
    	  .domain([0, max])
    	   .range([0, width]);
    	var x2 = d3.scale.linear()
    	  .domain([0, 1])
    	   .range([0, width]);
    	var x2Axis = d3.svg.axis()
    	   .scale(x2)
    	   .orient('bottom')
    	   .ticks(10, '%');
    	
    	var y = d3.scale.ordinal()
    	  .domain(data.map(function(d) { return d[0]; }))
    	   .rangeRoundBands([height, 0]);
    	var yAxis = d3.svg.axis()
    	   .scale(y)
    	   .orient('left');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	   .attr('width', width + margin.left + margin.right)
    	   .attr('height', height + margin.top + margin.bottom)
    	  .append('g')
    	   .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    	svg.append('g')
    	     .attr('class', 'x axis')
    	     .attr('transform', 'translate(0,' + height + ')')
    	     .call(x2Axis)  .append('text')
    	    .attr('y', (margin.bottom / 1.7))
    	    .attr('x', (width / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.xBottomlegend);
    	
    	svg.append('g')
    	    .attr('class', 'y axis')
    	   .call(yAxis)
    	    .append('text')
    	    .attr('transform', 'rotate(-90)')
    	    .attr('y', 0 - margin.left)
    	    .attr('x', 0 - (height / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.yLeftLegend);

    	svg.selectAll('.bar0')
    	    .data(data)
    	  .enter().append('rect')
    	    .attr('class', 'bar bar_green')
    	    .attr('fill', 'bar_green')
    	    .attr('x', 0)
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[1]); })
    	    .attr('height', y.rangeBand() - 4);
    	
    	var valDy = ((y.rangeBand() / 2) + 3) + "px";
    	svg.selectAll('.bar0')
    	     .data(data)
    	   .enter().append('text')
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
    	  .enter().append('rect')
    	    .attr('class', 'bar bar_red')
    	    .attr('fill', 'bar_red')
    	    .attr('x', function(d) { return x(d[1]); })
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[2]); })
    	    .attr('height', y.rangeBand() - 4);
    	
    	svg.selectAll('.bar1')
	     	.data(data)
		   .enter().append('text')
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
    	  .enter().append('rect')
    	    .attr('class', 'bar bar_grey')
    	    .attr('fill', 'bar_grey')
    	    .attr('x', function(d) { return x(d[1] + d[2]); })
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[3]); })
    	    .attr('height', y.rangeBand() - 4);
    	
    	svg.selectAll('.bar2')
	     	.data(data)
		   .enter().append('text')
		    .attr('x', function(d) { return x(d[1] + d[2] + d[3]); })
		    .attr('y', function(d) { return y(d[0]) + 2; })
		    .attr('dx', -3) // padding-left
		    .attr('dy', valDy) // vertical-align: middle
		    .attr('text-anchor', 'end') // text-align: right
		    .attr('fill', 'rgb(48,48,48)')
		    .attr('stroke', 'none')
		    .text(function(d) { return (d[3] <= 0) ? '' : d[3]; });
    };
    
    horizontalBarSingleChoice = function($obj, settings) {
    	var placeholderwidth = $obj.width();
    	
    	var data = settings.values.reverse();
    	var colors = settings.colors.reverse();
    	
    	var margin = {top: 15, right: 15, bottom: 40, left: 40};
    	var height = data.length * settings.barHeight;
    	$obj.height(height + margin.top + margin.bottom + 'px');
    	var width = placeholderwidth - margin.left - margin.right;
    	
    	var sum = settings.participants;
    	var max = sum;

    	var x = d3.scale.linear()
    	  .domain([0, max])
    	   .range([0, width]);
    	var x2 = d3.scale.linear()
    	  .domain([0, 1])
    	   .range([0, width]);
    	var x2Axis = d3.svg.axis()
    	   .scale(x2)
    	   .orient('bottom')
    	   .ticks(10, '%');
    	
    	var y = d3.scale.ordinal()
    	  .domain(data.map(function(d) { return d[0]; }))
    	   .rangeRoundBands([height, 0]);
    	var yAxis = d3.svg.axis()
    	   .scale(y)
    	   .orient('left');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	   .attr('width', width + margin.left + margin.right)
    	   .attr('height', height + margin.top + margin.bottom)
    	  .append('g')
    	   .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
    	svg.append('g')
    	     .attr('class', 'x axis')
    	     .attr('transform', 'translate(0,' + height + ')')
    	     .call(x2Axis)  .append('text')
    	    .attr('y', (margin.bottom / 1.7))
    	    .attr('x', (width / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.xBottomLegend);

    	svg.selectAll('.bar0')
    	    .data(data)
    	  .enter().append('rect')
    	    .attr('class', function(d, i){
    	    	if(colors == null) return 'bar bar0 bar_default';
    	    	else if(colors.length > i) return colors[i];
    	    	else return 'bar bar0 bar_default';
    	    })
    	    .attr('fill', 'bar_green')
    	    .attr('x', 0)
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('width', function(d) { return x(d[1]); })
    	    .attr('height', y.rangeBand() - 4);
    	
    	var valDy = ((y.rangeBand() / 2) + 3) + "px";
    	svg.selectAll('.bar0')
    	     .data(data)
    	   .enter().append('text')
    	    .attr('x', function(d) { return x(d[1]); })
    	    .attr('y', function(d) { return y(d[0]) + 2; })
    	    .attr('dx', -3) // padding-left
    	    .attr('dy', valDy) // vertical-align: middle
    	    .attr('text-anchor', 'end') // text-align: right
    	    .attr('fill', 'rgb(48,48,48)')
    	    .attr('stroke', 'none')
    	    .text(function(d) { return (d[1] <= 0) ? '' : d[1]; });
    	
    	svg.append('g')
    	    .attr('class', 'y axis')
    	   .call(yAxis)
    	    .append('text')
    	    .attr('transform', 'rotate(-90)')
    	    .attr('y', 0 - margin.left)
    	    .attr('x', 0 - (height / 2))
    	    .attr('dy', '1em')
    	    .style('text-anchor', 'middle')
    	    .text(settings.yLeftLegend);
    };
    
    histogramDuration = function($obj, settings) {
    	var placeholderheight = $obj.height();
    	var placeholderwidth = $obj.width();
    	var values = settings.values;
    	var minTime = d3.min(values, function(d) { return d; });
    	var maxTime = d3.max(values, function(d) { return d; });

    	var ref = new Date(2012, 0, 1, 0, 0),
    	  formatCount = d3.format(',.f');
    	
    	var timeFormat = '%M:%S';
    	var formatMinutes;
    	if(maxTime < 10) {
    		var formatTime = d3.time.format('%M:%S:%L'),
    		formatMinutes = function(d) { return formatTime(new Date(ref.getTime() + (d * 1000))); };
    	} else if(maxTime < 3600) {
    		var formatTime = d3.time.format('%M:%S'),
    		formatMinutes = function(d) { return formatTime(new Date(ref.getTime() + (d * 1000))); };
    	} else {
    		formatMinutes = function(d) { return Math.round( d / 60 ); }
    	}
    	  
    	var margin = {top: 10, right: 60, bottom: 40, left: 60},
    	  width = placeholderwidth - margin.left - margin.right,
    	  height = placeholderheight - margin.top - margin.bottom;
    	  
    	var x = d3.scale.linear()
    	  .domain([0, maxTime])
    	  .range([0, width]);
    	
    	var data = d3.layout.histogram()
    	  //.range([0, maxTime])
    	  .bins(x.ticks(20))
    	  (values);
    	
    	var sum = d3.sum(data, function(d) { return d.y; });
    	var y = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d.y; })])
    	  .range([height, 0]);
    	
    	var y2 = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d.y / sum; })])
    	  .range([height, 0]);
    	
    	var xAxis = d3.svg.axis()
    	  .scale(x)
    	  .orient('bottom')
    	  .tickFormat(formatMinutes);
    	
    	var yAxis = d3.svg.axis()
    	  .scale(y)
    	  .orient('right')
    	  .ticks(10)
    	  .tickSubdivide(0);
    	
    	var y2Axis = d3.svg.axis()
    	  .scale(y2)
    	  .orient('left')
    	  .ticks(10, '%');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	  .attr('width', width + margin.left + margin.right)
    	  .attr('height', height + margin.top + margin.bottom)
    	 .append('g')
    	  .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
    	var bar = svg.selectAll('.bar')
    	  .data(data)
    	 .enter().append('g')
    	  .attr('class', 'bar bar_default')
    	  .attr('transform', function(d) { return 'translate(' + x(d.x) + ',' + y(d.y) + ')'; })
    	  .append('rect')
    	  .attr('x', 2)
    	  .attr('width', x(data[0].dx) - 4)
    	  .attr('height', function(d) { return height - y(d.y); });
    	
    	svg.append('g')
    	  .attr('class', 'y axis')
    	  .call(y2Axis)
    	 .append('text')
    	  .attr('transform', 'rotate(-90)')
    	  .attr('y', 0 - margin.left)
    	  .attr('x', 0 - (height / 2))
    	  .attr('dy', '1em')
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
    	  .style('text-anchor', 'middle')
    	  .text(settings.yRightLegend);
    };

    histogramScore = function($obj, settings) {
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
    	var x = d3.scale.linear()
    	  .domain([0, maxScore])
    	  .range([0, width]);
    	
    	var data = d3.layout.histogram()
    	  .bins(x.ticks(20))
    	  (values);
    	
    	var sum = d3.sum(data, function(d) { return d.y; });
    	
    	var y = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d.y; })])
    	  .range([height, 0]);
    	
    	var y2 = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d.y / sum; })])
    	  .range([height, 0]);
    	
    	var xAxis = d3.svg.axis()
    	  .scale(x)
    	  .orient('bottom')
    	  .tickFormat(d3.format('.01f'));
    	
    	var yAxis = d3.svg.axis()
    	  .scale(y)
    	  .orient('right')
    	  .ticks(10)
    	  .tickSubdivide(0);

    	var y2Axis = d3.svg.axis()
    	  .scale(y2)
    	  .orient('left')
    	  .ticks(10, '%');
    	
    	var svg = d3.select('#' + $obj.attr('id')).append('svg')
    	  .attr('width', width + margin.left + margin.right)
    	  .attr('height', height + margin.top + margin.bottom)
    	 .append('g')
    	  .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    	
    	var bar = svg.selectAll('.bar')
    	  .data(data)
    	 .enter().append('g')
    	  .attr('class', function(d, i) {
    		  if(cut == null) return 'bar bar_default';
    		  else if(data[i].x < cut) return 'bar bar_red';
    		  else return 'bar bar_green';
    	  })
    	  .attr('transform', function(d) { return 'translate(' + x(d.x) + ',' + y(d.y) + ')'; })
    	  .append('rect')
    	  .attr('x', 2)
    	  .attr('width', x(data[0].dx) - 4)
    	  .attr('height', function(d) { return height - y(d.y); });
    	
    	svg.append('g')
    	  .attr('class', 'y axis')
    	  .call(y2Axis)
    	 .append('text')
    	  .attr('transform', 'rotate(-90)')
    	  .attr('y', 0 - margin.left)
    	  .attr('x', 0 - (height / 2))
    	  .attr('dy', '1em')
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
    	  .style('text-anchor', 'middle')
    	  .text(settings.yRightLegend);
    }
    
    highScore = function($obj, settings) {
    	var placeholderheight = $obj.height();
    	var placeholderwidth = $obj.width();
    	var values = settings.values;
    	
    	var lquartile = d3.quantile(values, 0.25);
    	var hquartile = d3.quantile(values, 0.75);
    	var means = d3.mean(values);
    	    	
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
    	
    	if(maxScore < 1.0) {
    		maxScore = 1.0;
    	}
    	var x = d3.scale.linear()
    	  .domain([min, max])
    	  .range([0, width]);
    	  	
    	var range = d3.range(min, max + step, step)
    	
    	var data = d3.layout.histogram()
    	  .bins(range)
    	  (values);
    	
    	var sum = d3.sum(data, function(d) { return d.y; });
    	
    	var y = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d.y; })])
    	  .range([height, 0]);
    	
    	var y2 = d3.scale.linear()
    	  .domain([0, d3.max(data, function(d) { return d.y / sum; })])
    	  .range([height, 0]);
    	
    	var xAxis = d3.svg.axis()
    	  .tickValues(range)
    	  .scale(x)
    	  .orient('bottom')
    	  .tickFormat(d3.format("d"));
    	
	    var yAxis = d3.svg.axis()
	  	  .scale(y)
	  	  .orient('right')
	  	  .ticks(10)
	  	  .tickSubdivide(0);
	
	  	var y2Axis = d3.svg.axis()
	  	  .scale(y2)
	  	  .orient('left')
	  	  .ticks(10, '%');
	  	
	  	var svg = d3.select('#' + $obj.attr('id')).append('svg')
	  	  .attr('width', width + margin.left + margin.right)
	  	  .attr('height', height + margin.top + margin.bottom)
	  	  .append('g')
	  	  .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

	  	var value = d3.range(min, max + step, step);
  		var bar = svg.selectAll('.bar')
	  		.data(data)
	  		.enter().append('g')
	  		.attr('class', function(d, i) {
	  			if(cut == null) return 'o_empty';
	  			else if(data[i].x == cut) return 'o_myself';
	  			else return 'o_other';	  		  
	  		})
	  		.attr('transform', function(d) { return 'translate(' + x(d.x) + ',' + y(d.y) + ')'; })
	  		.append('rect')
	  		.attr('x', function (d,i){ return - (width / value.length)/2;})
	  		.attr('y', function (d){return height - y(d.y);})
	  		.attr('width', (width / range.length))
	  		.attr('height', function (d){return 0;})
	  		.style('opacity', 0)
	  		 .transition().delay(function (d,i){ return i*200/(max-min);})
	  		 .duration(800)
	 	  	.attr('y', function(d) { return 0; })
	 	  	.attr('height', function(d) { return height - y(d.y); })
	 	  	.style('opacity', 1);
	  	

	  	
	  	svg.append('g')
	  	  .attr('class', 'y axis')
	  	  .call(y2Axis)
	  	 .append('text')
	  	  .attr('transform', 'rotate(-90)')
	  	  .attr('y', 0 - margin.left)
	  	  .attr('x', 0 - (height / 2))
	  	  .attr('dy', '1em')
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
	  	  .style('text-anchor', 'middle')
	  	  .text(settings.yRightLegend);
	        

  }
    
}( jQuery ));
