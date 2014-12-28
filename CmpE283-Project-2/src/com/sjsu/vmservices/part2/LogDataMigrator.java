package com.sjsu.vmservices.part2;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.mysql.jdbc.PreparedStatement;

/**
 * This class is responsible to migrate the filtered log data from Master VM (that
 * collects logs from all other VMs under control and itself) to an 
 * Admin Machine (that feeds Data Visualization / Log Analytics).
 * i.e., MongoDB to MySQL DB
 */
public class LogDataMigrator {
	private static DB db;

	private static Connection conn;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String AH_URL = "jdbc:mysql://127.0.0.1:3306/logmanager";
	private static final String AH_USER = "root";
	private static final String AH_PASSWORD = "13111990";

	/**
	 * Connect to MongoDB
	 * @return
	 * @throws UnknownHostException
	 */
	private static DB getConnection() throws UnknownHostException {
		if (db == null) {
			MongoClient client = new MongoClient("130.65.133.150", 27017);
			db = client.getDB("myvm");
		}
		return db;
	}

	/**
	 * Connect to MySQL
	 * @return
	 */
	public static Connection getMysqlConnection() {
		if (conn == null) {
			try {
				Class.forName(DRIVER);
				conn = DriverManager
						.getConnection(AH_URL, AH_USER, AH_PASSWORD);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}

	/**
	 * @throws UnknownHostException
	 */
	private static void archivedata() throws UnknownHostException {
		DBCollection tbl = getConnection().getCollection("myvmstats");
		Date today = new Date();
		String atblname = "archive" + today.getYear() + today.getMonth()+ today.getDate();
		DBCollection atbl = getConnection().getCollection(atblname);
		DBCursor cur = tbl.find();
		while (cur.hasNext()) {
			atbl.insert(cur.next());
		}
		tbl.drop();
	}

	/**
	 * Aggregate the logs before inserting to MySQL
	 * @return
	 * @throws UnknownHostException
	 */
	public static String getAggregateData() throws UnknownHostException {
		DBCollection tbl = getConnection().getCollection("myvmstats");
		String grp = "{$group:{_id:'$vmname',avgcpu:{$avg:'$cpu_usagemhz'},avgdisk:{$avg:'$disktotal_latency'},avgmemory:{$avg:'$mem_active'},avgnetwork:{$avg:'$net_usage'},avgsystem:{$avg:'$sys_uptime'}}}";
		DBObject group = (DBObject) JSON.parse(grp);
		AggregationOutput output = tbl.aggregate(group);
		ArrayList<DBObject> list = (ArrayList<DBObject>) output.results();
		for (DBObject dbObject : list) {
			System.out.println(dbObject);
			insertInMysql(dbObject);
		}
		archivedata();
		return "";
	}

	/**
	 * Feed the data to MySQL DB that is used further for VM Performance Analytics.
	 * @param dbObj
	 */
	public static void insertInMysql(DBObject dbObj) {
		try {
			PreparedStatement st = (PreparedStatement) getMysqlConnection()
					.prepareStatement(
							"insert into logmanager.stats(host,vmname,time,cpu,disk,memory,network,system) values(?,?,sysdate(),?,?,?,?,?)");
			st.setString(1, "team14-desktop");

			System.out.println("Average CPU :" + dbObj.get("avgcpu").toString());

			if (dbObj.get("avgcpu").toString().equalsIgnoreCase("0.0")) {
				System.out.println("id null");
			} else {
				st.setString(2, dbObj.get("_id").toString());
				st.setDouble(3,
						Double.parseDouble(dbObj.get("avgcpu").toString()));
				st.setDouble(4,
						Double.parseDouble(dbObj.get("avgdisk").toString()));
				st.setDouble(5,
						Double.parseDouble(dbObj.get("avgmemory").toString()));

				st.setDouble(6,
						Double.parseDouble(dbObj.get("avgnetwork").toString()));
				st.setDouble(7,
						Double.parseDouble(dbObj.get("avgsystem").toString()));
				st.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the thread that does data migration for every 5 minutes.
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String... args) throws UnknownHostException {
		Thread t1 = new Thread() {
			public void run() {
			while (true) {
				try {
					getAggregateData();
					Thread.sleep(300000); //5-minutes
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	t1.start();
	}
}
