package com.sjsu.vmservices.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstantUtil {

	public static final List<String> METRIC_LIST = new ArrayList<String>(
			Arrays.asList("cpu","datastore","disk","mem","net","power","sys"));
	public static String URL = "https://130.65.133.20/sdk";
	public static String ADMIN_USER_NAME = "administrator";
	public static String ADMIN_PASSWORD = "12!@qwQW";
	public static String SSL_THUMBPRINT_HOST_143 = "A1:25:1D:24:4D:16:03:55:88:2B:B8:5D:50:7B:63:FF:85:D9:00:19";
	public static List<String> PARAMETER_LIST = new ArrayList<String>(
			Arrays.asList("cpu_usage","cpu_usagemhz",
					"datastore_totalWriteLatency", "datastore_totalReadLatency",
					"disk_write", "disk_read", "disk_maxTotalLatency",  "disk_usage",
					"mem_granted", "mem_consumed","mem_active","mem_vmmemctl",
					"net_usage","net_received","net_transmitted",
					"power_power",
					"sys_uptime"));
	
	public static List<String> PROJECT_PARAMETER_LIST_DRS1 = new ArrayList<String>(
			Arrays.asList("cpu_usagemhz"));
	
	public static List<String> PROJECT_PARAMETER_LIST_DRS2 = new ArrayList<String>(
			Arrays.asList("cpu_usage"));
	public static double USAGE_LOWER_LIMIT = 30.00;
}
