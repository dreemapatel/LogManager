<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
 <%@page import="java.util.*" %>
    <%@page import="org.json.JSONObject" %>
<%@ page import="java.io.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">


<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>VM Data Visualization</title>
	<link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
<style>
#cpuChart {
	width	: 100%;
	height	: 500px;
}	
#diskChart {
	width	: 100%;
	height	: 500px;
}	
#memChart {
	width	: 100%;
	height	: 500px;
}	
#networkChart {
	width	: 100%;
	height	: 500px;
}	
#sysChart {
	width	: 100%;
	height	: 500px;
}	


</style>
  <script src="//code.jquery.com/jquery-1.10.2.js"></script>
  <script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
  
 
 <script type="text/javascript" src="js/amchart.js"></script>
 <script type="text/javascript" src="http://www.amcharts.com/lib/3/serial.js"></script>

<script type="text/javascript" src="http://docs.amcharts.com/2/javascriptcharts/AmGraph#fillColors"></script>
<script type="text/javascript" src="http://www.amcharts.com/lib/3/themes/none.js"></script>

  
  <script>
  $(function() {
    $( "#datepicker" ).datepicker({dateFormat: 'yy-mm-dd', minDate: '-1M',maxDate:'0D'});
   
 
  });
  </script>
 
 <script type="text/javascript">
<% 
List<JSONObject> cpuspeed = (List<JSONObject>)request.getAttribute("cpudata");
%> 


AmCharts.ready(function(){
	var cpudata= <%=cpuspeed%>;
chart = new AmCharts.AmXYChart();

chart.dataProvider = cpudata;
chart.autoMargins = false;
chart.marginLeft= 80;
chart.marginBottom= 60;
chart.startDuration = 0.5;
chart.handDrawn=true;

// AXES
// X
var xAxis = new AmCharts.ValueAxis();
xAxis.position = "bottom";
xAxis.gridAlpha = 0.1;
xAxis.autoGridCount = true;
xAxis.title ="Time";
chart.addValueAxis(xAxis);

// Y
var yAxis = new AmCharts.ValueAxis();
yAxis.position = "left";
yAxis.gridAlpha = 0.1;
yAxis.title = "CPU usage";
yAxis.autoGridCount = true;
chart.addValueAxis(yAxis);

// GRAPHS
var graph = new AmCharts.AmGraph();
graph.type = 'line';
graph.xField = "time_interval";
graph.yField = "T14-VM01-Ubuntu32";
graph.lineAlpha = 1;
graph.title ="T14-VM01-Ubuntu32";
graph.bullet ="round";
graph.balloonText ="CPU usage: [[T14-VM01-Ubuntu32]]";
graph.lineColor = '#FF9E01';
chart.addGraph(graph);

var graph2 = new AmCharts.AmGraph();
//graph2.type = 'line';
graph2.xField = "time_interval";
graph2.yField = "T14-VM02-Ubuntu32";
graph2.title ="T14-VM02-Ubuntu32";
graph2.lineAlpha = 1;
graph2.bullet ="round";
graph2.balloonText ="CPU usage: [[T14-VM02-Ubuntu32]]";
graph2.lineColor = '#9EFF01';
chart.addGraph(graph2);

var graph3 = new AmCharts.AmGraph();
graph3.type = 'line';
graph3.xField = "time_interval";
graph3.yField = "T14-VM03-Ubuntu32";
graph3.title ="T14-VM03-Ubuntu32";
graph3.lineAlpha = 1;
graph3.bullet ="round";
graph3.balloonText ="CPU Usage: [[T14-VM03-Ubuntu32]]";
graph3.lineColor = '#AB0301';
chart.addGraph(graph3);

var graph4 = new AmCharts.AmGraph();
graph4.type = 'line';
graph4.xField = "time_interval";
graph4.yField = "T14-VM04-Ubuntu32";
graph4.title ="T14-VM04-Ubuntu32";
graph4.lineAlpha = 1;
graph4.bullet ="round";
graph4.balloonText ="CPU usage: [[T14-VM04-Ubuntu32]]";
graph4.lineColor = '#000000';
chart.addGraph(graph4);
var chartCursor = new AmCharts.ChartCursor();
chart.addChartCursor(chartCursor);
var legend = new AmCharts.AmLegend();
chart.addLegend(legend);
// WRITE
chart.write("cpuChart");
});

</script>

<script type="text/javascript">
<% 
List<JSONObject> disk = (List<JSONObject>)request.getAttribute("diskdata");
%> 


AmCharts.ready(function(){
	var diskdata= <%=disk%>;
chart = new AmCharts.AmXYChart();

chart.dataProvider = diskdata;
chart.autoMargins = false;
chart.marginLeft= 80;
chart.marginBottom= 60;
chart.startDuration = 0.5;
chart.handDrawn=true;

// AXES
// X
var xAxis = new AmCharts.ValueAxis();
xAxis.position = "bottom";
xAxis.gridAlpha = 0.1;
xAxis.autoGridCount = true;
xAxis.title ="Time";
chart.addValueAxis(xAxis);

// Y
var yAxis = new AmCharts.ValueAxis();
yAxis.position = "left";
yAxis.gridAlpha = 0.1;
yAxis.title = "Disk usage";
yAxis.autoGridCount = true;
chart.addValueAxis(yAxis);

// GRAPHS
var graph = new AmCharts.AmGraph();
graph.type = 'line';
graph.xField = "time_interval";
graph.yField = "T14-VM01-Ubuntu32";
graph.lineAlpha = 1;
graph.title ="T14-VM01-Ubuntu32";
graph.bullet ="round";
graph.balloonText ="Disk usage: [[T14-VM01-Ubuntu32]]";
graph.lineColor = '#FF9E01';
chart.addGraph(graph);

var graph2 = new AmCharts.AmGraph();
//graph2.type = 'line';
graph2.xField = "time_interval";
graph2.yField = "T14-VM02-Ubuntu32";
graph2.title ="T14-VM02-Ubuntu32";
graph2.lineAlpha = 1;
graph2.bullet ="round";
graph2.balloonText ="Disk usage: [[T14-VM02-Ubuntu32]]";
graph2.lineColor = '#9EFF01';
chart.addGraph(graph2);

var graph3 = new AmCharts.AmGraph();
graph3.type = 'line';
graph3.xField = "time_interval";
graph3.yField = "T14-VM03-Ubuntu32";
graph3.title ="T14-VM03-Ubuntu32";
graph3.lineAlpha = 1;
graph3.bullet ="round";
graph3.balloonText ="Disk usage: [[T14-VM03-Ubuntu32]]";
graph3.lineColor = '#AB0301';
chart.addGraph(graph3);

var graph4 = new AmCharts.AmGraph();
graph4.type = 'line';
graph4.xField = "time_interval";
graph4.yField = "T14-VM04-Ubuntu32";
graph4.title ="T14-VM04-Ubuntu32";
graph4.lineAlpha = 1;
graph4.bullet ="round";
graph4.balloonText ="Disk Usage: [[T14-VM04-Ubuntu32]]";
graph4.lineColor = '#000000';
chart.addGraph(graph4);
var chartCursor = new AmCharts.ChartCursor();
chart.addChartCursor(chartCursor);
var legend = new AmCharts.AmLegend();
chart.addLegend(legend);
// WRITE
chart.write("diskChart");
});

</script>

<script type="text/javascript">
<% 
List<JSONObject> memory = (List<JSONObject>)request.getAttribute("memorydata");
%> 


AmCharts.ready(function(){
	var memorydata= <%=memory%>;
chart = new AmCharts.AmXYChart();

chart.dataProvider = memorydata;
chart.autoMargins = false;
chart.marginLeft= 130;
chart.marginBottom= 60;
chart.startDuration = 0.5;
chart.handDrawn=true;

// AXES
// X
var xAxis = new AmCharts.ValueAxis();
xAxis.position = "bottom";
xAxis.gridAlpha = 0.1;
xAxis.autoGridCount = true;
xAxis.title ="Time";
chart.addValueAxis(xAxis);

// Y
var yAxis = new AmCharts.ValueAxis();
yAxis.position = "left";
yAxis.gridAlpha = 0.1;
yAxis.title = "Memory usage";
yAxis.autoGridCount = true;
chart.addValueAxis(yAxis);

// GRAPHS
var graph = new AmCharts.AmGraph();
graph.type = 'line';
graph.xField = "time_interval";
graph.yField = "T14-VM01-Ubuntu32";
graph.lineAlpha = 1;
graph.title ="T14-VM01-Ubuntu32";
graph.bullet ="round";
graph.balloonText ="Memory: [[T14-VM01-Ubuntu32]]";
graph.lineColor = '#FF9E01';
chart.addGraph(graph);

var graph2 = new AmCharts.AmGraph();
//graph2.type = 'line';
graph2.xField = "time_interval";
graph2.yField = "T14-VM02-Ubuntu32";
graph2.title ="T14-VM02-Ubuntu32";
graph2.lineAlpha = 1;
graph2.bullet ="round";
graph2.balloonText ="Memory: [[T14-VM02-Ubuntu32]]";
graph2.lineColor = '#9EFF01';
chart.addGraph(graph2);

var graph3 = new AmCharts.AmGraph();
graph3.type = 'line';
graph3.xField = "time_interval";
graph3.yField = "T14-VM03-Ubuntu32";
graph3.title ="T14-VM03-Ubuntu32";
graph3.lineAlpha = 1;
graph3.bullet ="round";
graph3.balloonText ="Memory: [[T14-VM03-Ubuntu32]]";
graph3.lineColor = '#AB0301';
chart.addGraph(graph3);

var graph4 = new AmCharts.AmGraph();
graph4.type = 'line';
graph4.xField = "time_interval";
graph4.yField = "T14-VM04-Ubuntu32";
graph4.title ="T14-VM04-Ubuntu32";
graph4.lineAlpha = 1;
graph4.bullet ="round";
graph4.balloonText ="Memory: [[T14-VM04-Ubuntu32]]";
graph4.lineColor = '#000000';
chart.addGraph(graph4);
var chartCursor = new AmCharts.ChartCursor();
chart.addChartCursor(chartCursor);
var legend = new AmCharts.AmLegend();
chart.addLegend(legend);
// WRITE
chart.write("memChart");
});

</script>
<script type="text/javascript">
<% 
List<JSONObject> network = (List<JSONObject>)request.getAttribute("networkdata");
%> 


AmCharts.ready(function(){
	var networkdata= <%=network%>;
chart = new AmCharts.AmXYChart();

chart.dataProvider = networkdata;
chart.autoMargins = false;
chart.marginLeft= 80;
chart.marginBottom= 60;
chart.startDuration = 0.5;
chart.handDrawn=true;

// AXES
// X
var xAxis = new AmCharts.ValueAxis();
xAxis.position = "bottom";
xAxis.gridAlpha = 0.1;
xAxis.autoGridCount = true;
xAxis.title ="Time";
chart.addValueAxis(xAxis);

// Y
var yAxis = new AmCharts.ValueAxis();
yAxis.position = "left";
yAxis.gridAlpha = 0.1;
yAxis.title = "Network usage";
yAxis.autoGridCount = true;
chart.addValueAxis(yAxis);

// GRAPHS
var graph = new AmCharts.AmGraph();
graph.type = 'line';
graph.xField = "time_interval";
graph.yField = "T14-VM01-Ubuntu32";
graph.lineAlpha = 1;
graph.title ="T14-VM01-Ubuntu32";
graph.bullet ="round";
graph.balloonText ="Network: [[T14-VM01-Ubuntu32]]";
graph.lineColor = '#FF9E01';
chart.addGraph(graph);

var graph2 = new AmCharts.AmGraph();
//graph2.type = 'line';
graph2.xField = "time_interval";
graph2.yField = "T14-VM02-Ubuntu32";
graph2.title ="T14-VM02-Ubuntu32";
graph2.lineAlpha = 1;
graph2.bullet ="round";
graph2.balloonText ="Network: [[T14-VM02-Ubuntu32]]";
graph2.lineColor = '#9EFF01';
chart.addGraph(graph2);

var graph3 = new AmCharts.AmGraph();
graph3.type = 'line';
graph3.xField = "time_interval";
graph3.yField = "T14-VM03-Ubuntu32";
graph3.title ="T14-VM03-Ubuntu32";
graph3.lineAlpha = 1;
graph3.bullet ="round";
graph3.balloonText ="Network: [[T14-VM03-Ubuntu32]]";
graph3.lineColor = '#AB0301';
chart.addGraph(graph3);

var graph4 = new AmCharts.AmGraph();
graph4.type = 'line';
graph4.xField = "time_interval";
graph4.yField = "T14-VM04-Ubuntu32";
graph4.title ="T14-VM04-Ubuntu32";
graph4.lineAlpha = 1;
graph4.bullet ="round";
graph4.balloonText ="Network: [[T14-VM04-Ubuntu32]]";
graph4.lineColor = '#000000';
chart.addGraph(graph4);
var chartCursor = new AmCharts.ChartCursor();
chart.addChartCursor(chartCursor);
var legend = new AmCharts.AmLegend();
chart.addLegend(legend);
// WRITE
chart.write("networkChart");
});

</script>
<script type="text/javascript">
<% 
List<JSONObject> system = (List<JSONObject>)request.getAttribute("sysdata");
%> 


AmCharts.ready(function(){
	var sysdata= <%=system %>;
chart = new AmCharts.AmXYChart();

chart.dataProvider = sysdata;
chart.autoMargins = false;
chart.marginLeft= 120;
chart.marginBottom= 60;
chart.startDuration = 0.5;


// AXES
// X
var xAxis = new AmCharts.ValueAxis();
xAxis.position = "bottom";
xAxis.gridAlpha = 0.1;
xAxis.title = "Time";
xAxis.autoGridCount = true;
chart.addValueAxis(xAxis);

// Y
var yAxis = new AmCharts.ValueAxis();
yAxis.position = "left";
yAxis.gridAlpha = 0.1;
yAxis.title = "System";
yAxis.autoGridCount = true;
chart.addValueAxis(yAxis);

// GRAPHS
var graph = new AmCharts.AmGraph();
graph.type = 'line';
graph.xField = "time_interval";
graph.yField = "T14-VM01-Ubuntu32";
graph.title ="T14-VM01-Ubuntu32";
graph.lineAlpha = 1;
graph.bullet ="round";
graph.balloonText ="System: [[T14-VM01-Ubuntu32]]";
graph.lineColor = '#FF9E01';
chart.addGraph(graph);

var graph2 = new AmCharts.AmGraph();
//graph2.type = 'line';
graph2.xField = "time_interval";
graph2.yField = "T14-VM02-Ubuntu32";
graph2.title ="T14-VM02-Ubuntu32";
graph2.lineAlpha = 1;
graph2.bullet ="round";
graph2.balloonText ="System: [[T14-VM02-Ubuntu32]]";
graph2.lineColor = '#9EFF01';
chart.addGraph(graph2);

var graph3 = new AmCharts.AmGraph();
graph3.type = 'line';
graph3.xField = "time_interval";
graph3.yField = "T14-VM03-Ubuntu32";
graph3.title ="T14-VM03-Ubuntu32";
graph3.lineAlpha = 1;
graph3.bullet ="round";
graph3.balloonText ="System: [[T14-VM03-Ubuntu32]]";
graph3.lineColor = '#AB0301';
chart.addGraph(graph3);

var graph4 = new AmCharts.AmGraph();
graph4.type = 'line';
graph4.xField = "time_interval";
graph4.yField = "T14-VM04-Ubuntu32";
graph4.title ="T14-VM04-Ubuntu32";
graph4.lineAlpha = 1;
graph4.bullet ="round";
graph4.balloonText ="System: [[T14-VM04-Ubuntu32]]";
graph4.lineColor = '#000000';
chart.addGraph(graph4);
var chartCursor = new AmCharts.ChartCursor();
chart.addChartCursor(chartCursor);
var legend = new AmCharts.AmLegend();

chart.addLegend(legend);
// WRITE
chart.write("sysChart");
});

</script>


    <!-- Bootstrap Core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="css/2-col-portfolio.css" rel="stylesheet">
    
    

</head>

<body>
	
    <!-- Navigation -->
    <nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#">Home</a>
            </div>
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav">
                    <li>
                        <a href="#">About</a>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container -->
    </nav>

    <!-- Page Content -->
    <div class="container">

        <!-- Page Header -->
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Analysis of Logs
                    <small>Data Visualization</small>
                    
                    <form method="post" action="getServlet">
                    <p>Date: <input type="text" id="datepicker" name="date" required></p>
                    Select Hours:
                	<select name="interval">
                	<% for (int i=1;i<24;i++){ %>
                	<% String interval = i+ ":00 -"+ (i+1)+":00"; %>
         			<option value= <%=i %>> <%=interval%></option> <% }%>
                	
                	
                	
                	</select>
                	<input type="submit" value="5 Minute Data" name="criteria" >
                	<input type="submit" value="Hourly data" name="criteria" >
                	
                	
                </form>
               
                </h1>
            </div>
        </div>
        <!-- /.row -->

        <!-- Projects Row -->
        
               <h1>CPU Usage </h1>
               <br>
                    <div id ="cpuChart"></div>
                    
                    <br><br><br><br>
                    <h1>Disk Usage </h1><br>
           			 <div id ="diskChart"></div><br><br><br><br>
           			 <h1>Network Usage </h1><br>
           			  <div id ="networkChart"></div><br><br><br><br>
           			  <h1>Memory Usage </h1><br>
           			   <div id ="memChart"></div><br><br><br><br>
           			   <h1>System Usage </h1><br>
           			    <div id ="sysChart"></div>
           			
                
               
               

        <!-- Footer -->
        <footer>
            <div class="row">
                <div class="col-lg-12">
                    <p>Copyright &copy; Team 14 - CmpE283 Virtualization Technologies</p>
                </div>
            </div>
            <!-- /.row -->
        </footer>

    </div>
    <!-- /.container -->

    <!-- jQuery -->
    

    <!-- Bootstrap Core JavaScript -->
    <script src="js/bootstrap.min.js"></script>

</body>

</html>
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   