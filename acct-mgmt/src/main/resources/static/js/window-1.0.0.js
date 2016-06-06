var mw = new MovingWindow(); 

function MovingWindow() {
  this.data; 
  this.options = { 
    margin: {top: 50, right: 350, bottom: 80, left: 120},
    minHeight: 768,
    scaleFunc: d3.scale.linear
  };
  this.graph;
  this.zoom;
  this.zoomLeftLeft;
  this.zoomRight;                           

  
  this.removeCurrentGraph = function(selector) { 
    $(selector).fadeOut(1000, function() { $(selector).empty(); });
  };
  
  /**
   * @param selector Where to attach the graph
   * @param data Interventions data to display in graph
   */
  this.displayDataSet = function(selector, data) { 

    mw.data = data;
    
    /* d3 vars */
    var x;
    var y1;
    var y2;
    var y3;
    var m = [];
    var w = window.innerWidth - mw.options.margin.right - mw.options.margin.left;
    var h = window.innerHeight - mw.options.margin.top - mw.options.margin.bottom;

    x = d3.time.scale().domain(d3.extent(data['x'], function (d) {
        return d;
    })).range([0, w]);

    y1 = d3.scale.linear().domain([0, d3.max(data['y1'])]).range([h, 0]);
    y2 = d3.scale.linear().domain([0, d3.max(data['y2'])]).range([h, 0]);
    y3 = d3.scale.linear().domain([0, d3.max(data['y3'])]).range([h, 0]);
    y4 = d3.scale.linear().domain([0, d3.max(data['y4'])]).range([h, 0]);

    line1 = d3.svg.line()
        .interpolate("basis")
        .x(function (d, i) {
            return x(data['x'][i]);
        })
        .y(function (d) {
            return y1(d);
        });

    line2 = d3.svg.line()
        .interpolate("basis")
        .x(function (d, i) {
            return x(data['x'][i]);
        })
        .y(function (d) {
            return y2(d);
        });

    line3 = d3.svg.line()
        .interpolate("basis")
        .x(function (d, i) {
            return x(data['x'][i]);
        })
        .y(function (d) {
            return y3(d);
        });

    line4 = d3.svg.line()
        .interpolate("basis")
        .x(function (d, i) {
            return x(data['x'][i]);
        })
        .y(function (d) {
            return y4(d);
        });

    mw.zoom = d3.behavior.zoom()
        .x(x)
        .y(y1)
        .scaleExtent([1, 10])
        .on("zoom", mw.zoomed);

    mw.zoomLeftLeft = d3.behavior.zoom()
        .x(x)
        .y(y3)
        .scaleExtent([1, 10]);

    mw.zoomRight = d3.behavior.zoom()
        .x(x)
        .y(y2)
        .scaleExtent([1, 10]);

    // Add an SVG element with the desired dimensions and margin.
    mw.graph = d3.select("#visualisation").append("svg:svg")
        .attr("width", w + mw.options.margin.right + mw.options.margin.left)
        .attr("height", h + mw.options.margin.top + mw.options.margin.bottom)
        .call(mw.zoom)
        .append("svg:g")
        .attr("transform", "translate(" + mw.options.margin.left + "," + mw.options.margin.top + ")");

    // create xAxis
    xAxis = d3.svg.axis().scale(x).tickSize(-h).tickSubdivide(false);
    // Add the x-axis.
    mw.graph.append("svg:g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + h + ")")
        .call(xAxis);

    // create left yAxis
    yAxisLeftLeft = d3.svg.axis().scale(y2).ticks(10).orient("right");
    // Add the y-axis to the left
    mw.graph.append("svg:g")
        .attr("class", "y axis axisLeft")
        .attr("transform", "translate(0,0)")
        .call(yAxisLeftLeft);
    
    // left y axis label 
    mw.graph.append('text')
    .attr({
      x:0,
      y:25,
      class:'axis y1'
    })
    .attr('transform','rotate(-90),translate(-150,-90)')
    .text(data.labels[0]+' »');

    // create leftLeft yAxis
    yAxisLeft = d3.svg.axis().scale(y1).ticks(10).orient("left");
    // Add the y-axis to the left
    mw.graph.append("svg:g")
        .attr("class", "y axis axisLeftLeft")
        .attr("transform", "translate(0,0)")
        .call(yAxisLeft);
    
    // leftLeft y axis label 
    mw.graph.append('text')
    .attr({
      x:0,
      y:150,
      class:'axis y2'
    })
    .attr('transform','rotate(-90),translate(-150,-90)')
    .text(data.labels[1]+' »');

    // create right yAxis
    yAxisRight = d3.svg.axis().scale(y3).ticks(10).orient("left");
    // Add the y-axis to the right
    mw.graph.append("svg:g")
        .attr("class", "y axis axisRight")
        .attr("transform", "translate(" + (w) + ",0)")
        .call(yAxisRight);

    // right y axis label 
    mw.graph.append('text')
    .attr({
      x:0,
      y:w+50,
      class:'axis y3'
    })
    .attr('transform','rotate(-90),translate(-150,-90)')
    .text(data.labels[2]+' »');

    // create rightRight yAxis
    if (data.labels.length > 3) {
      yAxisRightRight = d3.svg.axis().scale(y4).ticks(2).orient("right");
      // Add the y-axis to the right
      mw.graph.append("svg:g")
          .attr("class", "y axis axisRightRight")
          .attr("transform", "translate(" + (w) + ",0)")
          .call(yAxisRightRight);
      
      // rightRight y axis label 
      mw.graph.append('text')
        .attr({
          x:0,
          y:w+120,
          class:'axis y4'
        })
        .attr('transform','rotate(-90),translate(-150,-90)')
        .text(data.labels[3]+' »');
    }

    // add lines
    // do this AFTER the axes above so that the line is above the tick-lines
    mw.graph.append("svg:path").attr("d", line1(data['y1'])).attr("class", "y1");
    mw.graph.append("svg:path").attr("d", line2(data['y2'])).attr("class", "y2");
    mw.graph.append("svg:path").attr("d", line3(data['y3'])).attr("class", "y3");
    mw.graph.append("svg:path").attr("d", line4(data['y4'])).attr("class", "y4");
  }

  this.makeXAxis = function () {
    return d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .ticks(5);
  };

  this.makeYAxis = function () {
    return d3.svg.axis()
        .scale(y1)
        .orient("left")
        .ticks(5);
  };
  
  this.zoomed = function() {
    mw.zoomRight.scale(mw.zoom.scale()).translate(mw.zoom.translate());
    mw.zoomLeftLeft.scale(mw.zoom.scale()).translate(mw.zoom.translate());

    mw.graph.select(".x.axis").call(mw.xAxis);
    mw.graph.select(".y.axisLeft").call(mw.yAxisLeft);
    mw.graph.select(".y.axisLeftLeft").call(mw.yAxisLeftLeft);
    mw.graph.select(".y.axisRight").call(mw.yAxisRight);
    mw.graph.select(".y.axisRightRight").call(mw.yAxisRightRight);
    mw.graph.select(".x.grid")
        .call(mw.makeXAxis()
        .tickFormat(""));
    mw.graph.select(".y.axis")
        .call(mw.makeYAxis()
            .tickSize(5, 0, 0));
    mw.graph.selectAll(".y1")
        .attr("d", line1(data['y1']));
    mw.graph.selectAll(".y2")
        .attr("d", line2(data['y2']));
    mw.graph.selectAll(".y3")
        .attr("d", line3(data['y3']));
    mw.graph.selectAll(".y4")
        .attr("d", line4(data['y4']));
  };
}