var sb = new StackedBar();

function StackedBar() {
  this.data;
  this.options = {
    legendIndicator: 20,
    margin: {top: 50, right: 50, bottom: 50, left: 60},
    minHeight: 600,
    outerWidth: window.innerWidth,
    outerHeight: window.innerHeight
  };
  this.displayDataSet = function(selector, data, options) {
    sb.data = data;
    if (options!=undefined) jQuery.extend(sb.options, options);

    var width = sb.options.outerWidth - sb.options.margin.left - sb.options.margin.right ;
    var height = sb.options.outerHeight - sb.options.margin.top - sb.options.margin.bottom;
    if (height < sb.options.minHeight) height = sb.options.minHeight;

    var x = d3.scale.ordinal()
        .rangeRoundBands([0, width-150], .1);

    var y = d3.scale.linear()
        .rangeRound([height, 0]);

    // http://paletton.com/#uid=73y0u0k++yZAIZF+VWT+WqI+kkB
    var color = d3.scale.ordinal()
        .range(["#FFC000", "#FF9400", "#A45F00", "#005EAD", "0E8FFB"]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    // TODO option to scale to values not %
//    if (sb.options['yAxisPercentage']!=true) {
//    	y = d3.scale.linear()
//        .domain([152339,310504,2590304]);
//    }

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    if (sb.options['yAxisPercentage']==true) {
    	yAxis.tickFormat(d3.format(".0%"));
    }

    d3.select(selector+' svg').remove();
    var svg = d3.select(selector).append("svg")
        .attr("width", width)
        .attr("height", height+150)
      .append("g")
        .attr("transform", "translate(" + sb.options.margin.left + "," + sb.options.margin.top + ")");

    color.domain(d3.keys(data[0]).filter(function(key) { return key !== "Category"; }));

    data.forEach(function(d) {
      var y0 = 0;
      d.value = color.domain().map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
      d.value.forEach(function(d) { d.y0 /= y0; d.y1 /= y0; });
    });

    x.domain(data.map(function(d) { return d.Category; }));

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis);

    if (sb.options['yAxisLabel']!=undefined) {
	    svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	    .append("text")
	      .attr("transform", "rotate(-90)")
	      .attr("y", -55)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text(sb.options.yAxisLabel);
    }

    var category = svg.selectAll(".category")
        .data(data)
      .enter().append("g")
        .attr("class", "category")
        .attr("transform", function(d) { return "translate(" + x(d.Category) + ",0)"; });

    category.selectAll("rect")
        .data(function(d) { return d.value; })
      .enter().append("rect")
        .attr("width", x.rangeBand())
        .attr("y", function(d) { return y(d.y1); })
        .attr("height", function(d) { return y(d.y0) - y(d.y1); })
        .style("fill", function(d) { return color(d.name); });

    var legend = svg.select(".category:last-child").selectAll(".legend")
        .data(function(d) { return d.value; })
      .enter().append("g")
        .attr("class", "legend")
        .attr("transform", function(d) { return "translate(" + (x.rangeBand() - sb.options.legendIndicator/2) + "," + y((d.y0 + d.y1) / 2) + ")"; });

    legend.append("line")
        .attr("x2", sb.options.legendIndicator);

    legend.append("text")
        .attr("x", sb.options.legendIndicator+3)
        .attr("dy", ".35em")
        .text(function(d) { return d.value==undefined ? '' : d.name; });

  }
}