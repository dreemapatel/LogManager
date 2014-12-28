package com.team14;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.*;
/**
 * Servlet implementation class index
 */
@WebServlet(name="DbServlet", urlPatterns="/getServlet")
public class index extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public index() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	   }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
		String DB_URL="jdbc:mysql://localhost/logmanager";

		String criteria = request.getParameter("criteria");
		//  Database credentials
		String USER = "root";
		String PASS = "13111990";
		
		Connection conn = null;
		Statement stmt = null;
		try{
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");
	         
	      // Open a connection
	         conn = DriverManager.getConnection(DB_URL,USER,PASS);

	         ResultSet rs1 =null;
	         ResultSet rs2 = null;
	         ResultSet rs3 =null;
	         ResultSet rs4 =null;
	         List<JSONObject> vm01 = new LinkedList<JSONObject>();
	         List<JSONObject> vm02 = new LinkedList<JSONObject>();
	         List<JSONObject> vm03 = new LinkedList<JSONObject>();
	         List<JSONObject> vm04 = new LinkedList<JSONObject>();
	         List<JSONObject> cpuList = new LinkedList<JSONObject>();
	         List<JSONObject> sysList = new LinkedList<JSONObject>();
	         List<JSONObject> memoryList = new LinkedList<JSONObject>();
	         List<JSONObject> networkList = new LinkedList<JSONObject>();
	         List<JSONObject> diskList = new LinkedList<JSONObject>();
	         JSONObject mngrobj = null;
	         JSONObject dataobj =null;
	         String query1= null;
	         String query2 =null;
	         String query3=null;
	         String query4=null;
	         Calendar mydate = new GregorianCalendar();
	         String strdate = request.getParameter("date");
	         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	         Date date = formatter.parse(request.getParameter("date"));
	         mydate.setTime(date);
	     //    int year = mydate.get(Calendar.YEAR);
	       //  int month =mydate.get(Calendar.MONTH)+1;
	        // int day =mydate.get(Calendar.DAY_OF_MONTH);
	       
	         if(criteria.equalsIgnoreCase("Hourly data")){
	        // String vmcpu1 = "select vmname as vmname,Hour(time) as hour, Avg(cpu) as avgt from stats where vmname=? where DATE(time)=? and time between (curdate()+ INTERVAL (select HOUR(now())) - INTERVAL 23 hour ) AND now() GROUP BY hour";
	       // query1 = "SELECT vmname as vmname,Hour(time) AS time_interval, Avg(cpu) AS cpu, Avg(disk) as disk, Avg(memory) as memory, Avg(network) as network, Avg(system) as system FROM logmanager.stats WHERE vmname=? and time BETWEEN ('"+strdate+"'+ INTERVAL 11 hour - interval 10 hour) AND '"+strdate+"' 23:59:00' group by time_interval ";
	        	 
	          query1 = "SELECT vmname as vmname,Hour(time) AS time_interval, Avg(cpu) AS cpu, Avg(disk) as disk, Avg(memory) as memory, Avg(network) as network, Avg(system) as system FROM logmanager.stats WHERE vmname=? and DATE(time) = '"+strdate+"' group by time_interval ";
	          query2 = "SELECT vmname as vmname,Hour(time) AS time_interval, Avg(cpu) AS cpu, Avg(disk) as disk, Avg(memory) as memory, Avg(network) as network, Avg(system) as system FROM logmanager.stats WHERE vmname=? and DATE(time) = '"+strdate+"' group by time_interval ";
	          query3 = "SELECT vmname as vmname,Hour(time) AS time_interval, Avg(cpu) AS cpu, Avg(disk) as disk, Avg(memory) as memory, Avg(network) as network, Avg(system) as system FROM logmanager.stats WHERE vmname=? and DATE(time) = '"+strdate+"' group by time_interval ";
	          query4 = "SELECT vmname as vmname,Hour(time) AS time_interval, Avg(cpu) AS cpu, Avg(disk) as disk, Avg(memory) as memory, Avg(network) as network, Avg(system) as system FROM logmanager.stats WHERE vmname=? and DATE(time) = '"+strdate+"' group by time_interval ";
	         }
	         else if(criteria.equalsIgnoreCase("5 Minute Data")){
	        	 int interval =Integer.parseInt(request.getParameter("interval"));
	        	 
	        	  query1 = "SELECT vmname as vmname,Minute(time) as time_interval, cpu as cpu, disk as disk, memory as memory, network as network, system as system from stats where vmname=? and HOUR(time)="+interval+" and DATE(time)='"+strdate+"'"; 
	        	  query2 = "SELECT vmname as vmname,Minute(time) as time_interval, cpu as cpu, disk as disk, memory as memory, network as network, system as system from stats where vmname=? and HOUR(time)="+interval+" and DATE(time)='"+strdate+"'"; 
	        	  query3 = "SELECT vmname as vmname,Minute(time) as time_interval, cpu as cpu, disk as disk, memory as memory, network as network, system as system from stats where vmname=? and HOUR(time)="+interval+" and DATE(time)='"+strdate+"'"; 
	        	  query4 = "SELECT vmname as vmname,Minute(time) as time_interval, cpu as cpu, disk as disk, memory as memory, network as network, system as system from stats where vmname=? and HOUR(time)="+interval+" and DATE(time)='"+strdate+"'"; 
	        	  
	        	 
	        	 
	         }
	         PreparedStatement pstm1= conn.prepareStatement(query1);
	         PreparedStatement pstm2= conn.prepareStatement(query2);
	         PreparedStatement pstm3= conn.prepareStatement(query3);
	         PreparedStatement pstm4= conn.prepareStatement(query4);
	         pstm1.setString(1, "T14-VM01-Ubuntu32");
	         pstm2.setString(1, "T14-VM02-Ubuntu32");
	         pstm3.setString(1, "T14-VM03-Ubuntu32");
	         pstm4.setString(1, "T14-VM04-Ubuntu32");
	        // pstm1.setString(1, strdate);
	       
	         rs1 = pstm1.executeQuery();
	         rs2 = pstm2.executeQuery();
	         rs3 = pstm3.executeQuery();
	         rs4 = pstm4.executeQuery();
	         
	        
	        while(rs1.next()){
	        	String vmname = rs1.getString("vmname");
	        	String time_interval = rs1.getString("time_interval");
	        	
	        	 float cpuspeed = rs1.getFloat("cpu");
	        	 
	        	float disk = rs1.getFloat("disk");
	        	float memory = rs1.getFloat("memory");
	        	 float network = rs1.getFloat("network");
	        	 float system = rs1.getFloat("system");
	        	 
	        	 mngrobj = new JSONObject();
	        	 mngrobj.put("vmname", vmname);
	        	 mngrobj.put("time_interval", time_interval);
	        	 mngrobj.put("cpuspeed", cpuspeed);
	        	 mngrobj.put("disk", disk);
	        	 mngrobj.put("network", network);
	        	 mngrobj.put("system", system);
	        	 mngrobj.put("memory", memory);
	        	 vm01.add(mngrobj);
	        	 
	      }
	        
	         
	         
		        	while(rs2.next()){
		        		String vmname = rs2.getString("vmname");
		        		String time_interval = rs2.getString("time_interval");
		        	
		        		float cpuspeed = rs2.getFloat("cpu");
		        	 
		        		float disk = rs2.getFloat("disk");
		        		float memory = rs2.getFloat("memory");
		        		float network = rs2.getFloat("network");
		        		float system = rs2.getFloat("system");
		        	 
		        		mngrobj = new JSONObject();
		        		mngrobj.put("vmname", vmname);
		        		mngrobj.put("time_interval", time_interval);
		        		mngrobj.put("cpuspeed", cpuspeed);
		        		mngrobj.put("disk", disk);
		        		mngrobj.put("network", network);
		        		mngrobj.put("system", system);
		        		mngrobj.put("memory", memory);
		        		vm02.add(mngrobj);
	         }  	 
		        	 
	        	 
	         
	         
	        
	         
	         while(rs3.next()){
	        	 String vmname = rs3.getString("vmname");
	        	 String time_interval = rs3.getString("time_interval");
		        	
		        	 float cpuspeed = rs3.getFloat("cpu");
		        	 
		        	float disk = rs3.getFloat("disk");
		        	float memory = rs3.getFloat("memory");
		        	 float network = rs3.getFloat("network");
		        	 float system = rs3.getFloat("system");
		        	 
		        	 mngrobj = new JSONObject();
		        	 mngrobj.put("vmname", vmname);
		        	 mngrobj.put("time_interval", time_interval);
		        	 mngrobj.put("cpuspeed", cpuspeed);
		        	 mngrobj.put("disk", disk);
		        	 mngrobj.put("network", network);
		        	 mngrobj.put("system", system);
		        	 mngrobj.put("memory", memory);
		        	 vm03.add(mngrobj);
		        	 
	         }	 
	        	 
	         
	         
	             while(rs4.next()){
	        	 String vmname = rs4.getString("vmname");
	        	 String time_interval = rs4.getString("time_interval");
		        	
		        	 float cpuspeed = rs4.getFloat("cpu");
		        	 
		        	float disk = rs4.getFloat("disk");
		        	float memory = rs4.getFloat("memory");
		        	 float network = rs4.getFloat("network");
		        	 float system = rs4.getFloat("system");
		        	 
		        	 mngrobj = new JSONObject();
		        	 mngrobj.put("vmname", vmname);
		        	 mngrobj.put("time_interval", time_interval);
		        	 mngrobj.put("cpuspeed", cpuspeed);
		        	 mngrobj.put("disk", disk);
		        	 mngrobj.put("network", network);
		        	 mngrobj.put("system", system);
		        	 mngrobj.put("memory", memory);
		        	 vm04.add(mngrobj);
	         }  	 
		        	
	        	 
	         
	         
	         
	         
	         try{
	        	 
	        	 for(int i=0;i<vm01.size();i++){
	        	 dataobj = new JSONObject();
	        	 dataobj.put("time_interval", vm01.get(i).get("time_interval"));
	        	 dataobj.put("T14-VM01-Ubuntu32", vm01.get(i).get("cpuspeed"));
	        	
	        	 cpuList.add(dataobj);  
	        	 }
	        	 for(int i=0;i<vm02.size();i++){
		        	 dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm02.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM02-Ubuntu32", vm02.get(i).get("cpuspeed"));
		        	 cpuList.add(dataobj); 
	        	 }
	        	 for(int i=0;i<vm03.size();i++){
		        	 dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm03.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM03-Ubuntu32", vm03.get(i).get("cpuspeed"));
		        	 cpuList.add(dataobj); 
	        	 }
	        	 for(int i=0;i<vm04.size();i++){
		        	 dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm04.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM04-Ubuntu32", vm04.get(i).get("cpuspeed"));
		        	 cpuList.add(dataobj); 
	        	 }
	       
	         System.out.println("cpu:"+cpuList);
	         request.setAttribute("cpudata", cpuList);
	        }
	        catch(IndexOutOfBoundsException e){
	        	
	        }
	        try{
	         for(int i=0;i<vm01.size();i++){
	         	dataobj = new JSONObject();
	        	 dataobj.put("time_interval", vm01.get(i).get("time_interval"));
	        	 dataobj.put("T14-VM01-Ubuntu32", vm01.get(i).get("disk"));
	        	 diskList.add(dataobj);
	        	
	         }
	         for(int i=0;i<vm02.size();i++){
		         	dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm02.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM02-Ubuntu32", vm02.get(i).get("disk"));
		        	 diskList.add(dataobj);
		        	
		         }
	         for(int i=0;i<vm03.size();i++){
		         	dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm03.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM03-Ubuntu32", vm03.get(i).get("disk"));
		        	 diskList.add(dataobj);
		        	
		         }
	         for(int i=0;i<vm04.size();i++){
		         	dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm04.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM04-Ubuntu32", vm04.get(i).get("disk"));
		        	 diskList.add(dataobj);
		        	
		         }
	         	System.out.println(diskList);
	         	 request.setAttribute("diskdata", diskList);
	        }
	        catch(IndexOutOfBoundsException e ){
	        	
	        }
	         try{	 
	         	for(int i=0; i<vm01.size();i++){
	         		dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm01.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM01-Ubuntu32", vm01.get(i).get("network"));
		        	 
		        	 networkList.add(dataobj);
		        	
		         }
	         	for(int i=0; i<vm02.size();i++){
	         		dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm02.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM02-Ubuntu32", vm02.get(i).get("network"));
		        	 
		        	 networkList.add(dataobj);
		        	
		         }
	         	for(int i=0; i<vm03.size();i++){
	         		dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm03.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM03-Ubuntu32", vm03.get(i).get("network"));
		        	 
		        	 networkList.add(dataobj);
		        	
		         }
	         	for(int i=0; i<vm04.size();i++){
	         		dataobj = new JSONObject();
		        	 dataobj.put("time_interval", vm04.get(i).get("time_interval"));
		        	 dataobj.put("T14-VM04-Ubuntu32", vm04.get(i).get("network"));
		        	 
		        	 networkList.add(dataobj);
		        	
		         }
	         	System.out.println(networkList);
	         	 request.setAttribute("networkdata", networkList);
	         }
	         catch(IndexOutOfBoundsException e){
		        	
		        }
		         
	         try{
		         	for(int i=0; i<vm01.size();i++){
		         		
		         		dataobj = new JSONObject();
			        	 dataobj.put("time_interval", vm01.get(i).get("time_interval"));
			        	 dataobj.put("T14-VM01-Ubuntu32", vm01.get(i).get("system"));
			        	
			        	 
			        	 sysList.add(dataobj);
			        	
			         }
		         	for(int i=0; i<vm02.size();i++){
		         		
		         		dataobj = new JSONObject();
			        	 dataobj.put("time_interval", vm02.get(i).get("time_interval"));
			        	 dataobj.put("T14-VM02-Ubuntu32", vm02.get(i).get("system"));
			        	
			        	 
			        	 sysList.add(dataobj);
			        	
			         }
		         	for(int i=0; i<vm03.size();i++){
		         		
		         		dataobj = new JSONObject();
			        	 dataobj.put("time_interval", vm03.get(i).get("time_interval"));
			        	 dataobj.put("T14-VM03-Ubuntu32", vm03.get(i).get("system"));
			        	
			        	 
			        	 sysList.add(dataobj);
			        	
			         }
		         		for(int i=0; i<vm04.size();i++){
		         		
		         		dataobj = new JSONObject();
			        	 dataobj.put("time_interval", vm04.get(i).get("time_interval"));
			        	 dataobj.put("T14-VM04-Ubuntu32", vm04.get(i).get("system"));
			        	
			        	 
			        	 sysList.add(dataobj);
			        	
			         }
		         	System.out.println(sysList);
		         	 request.setAttribute("sysdata", sysList);
	         }
	         catch(IndexOutOfBoundsException e){
		        	
		        }
		         	try{ 
			         	for(int i=0; i<vm01.size();i++){
			         		dataobj = new JSONObject();
				        	 dataobj.put("time_interval", vm01.get(i).get("time_interval"));
				        	 dataobj.put("T14-VM01-Ubuntu32", vm01.get(i).get("memory"));
				        	 
				        	
				        	 memoryList.add(dataobj);
				        	
				         }
			         	for(int i=0; i<vm02.size();i++){
			         		dataobj = new JSONObject();
				        	 dataobj.put("time_interval", vm02.get(i).get("time_interval"));
				        	 dataobj.put("T14-VM02-Ubuntu32", vm02.get(i).get("memory"));
				        	 
				        	
				        	 memoryList.add(dataobj);
				        	
				         }
			         	for(int i=0; i<vm03.size();i++){
			         		dataobj = new JSONObject();
				        	 dataobj.put("time_interval", vm03.get(i).get("time_interval"));
				        	 dataobj.put("T14-VM03-Ubuntu32", vm03.get(i).get("memory"));
				        	 
				        	
				        	 memoryList.add(dataobj);
				        	
				         }
			         	for(int i=0; i<vm01.size();i++){
			         		dataobj = new JSONObject();
				        	 dataobj.put("time_interval", vm04.get(i).get("time_interval"));
				        	 dataobj.put("T14-VM04-Ubuntu32", vm04.get(i).get("memory"));
				        	 
				        	
				        	 memoryList.add(dataobj);
				        	
				         }
			         		
			         	 request.setAttribute("memorydata", memoryList);
				         	System.out.println(memoryList);
		         	}
		         	catch(IndexOutOfBoundsException e){
			        	
			        }
	         
	         
	        	 
	         // Clean-up environment
	         conn.close();
	         request.getRequestDispatcher("graph.jsp").forward(request, response);
	      }catch(SQLException se){
	         //Handle errors for JDBC
	         se.printStackTrace();
	      }catch(Exception e){
	         //Handle errors for Class.forName
	         e.printStackTrace();
	      }finally{
	         //finally block used to close resources
	         try{
	            if(stmt!=null)
	               stmt.close();
	         }catch(SQLException se2){
	         }// nothing we can do
	         try{
	            if(conn!=null)
	            conn.close();
	         }catch(SQLException se){
	            se.printStackTrace();
	         }//end finally try
	      } //end try 
	}

}
