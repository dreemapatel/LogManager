package com.sjsu.vmservices.part2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.sjsu.vmservices.util.ConstantUtil;
import com.sjsu.vmservices.util.PerformanceUtil;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * This class has the responsibility of collecting all metrics data from the VM's and write to log file for further analysis.
 */
public class VMPerformanceCollector {
	private static HashMap<Integer, PerfCounterInfo> headerInfo = new HashMap<Integer, PerfCounterInfo>();
	private int maxSamples;
	private String username;
	private String password;
	private URL url;
	private List<VirtualMachine> vmList;
	static LogWriter writer1;
    static LogWriter archivewriter1;

	public VMPerformanceCollector(URL url, String username, String password,
			int maxSamples) throws RemoteException, MalformedURLException,IOException {
		this.url = url;
		this.username = username;
		this.password = password;
		this.maxSamples = maxSamples;
		this.vmList = new ArrayList<VirtualMachine>();
		archivewriter1 = LogWriter.getArchiveInstance();
		archivewriter1.write("\n", "archivewriter");
		ServiceInstance si = new ServiceInstance(url, username, password, true);
		Folder rootFolder = si.getRootFolder();

		ManagedEntity[] mngEntity = new InventoryNavigator(rootFolder)
				.searchManagedEntities("VirtualMachine");
		for (int i = 0; i < mngEntity.length; i++) {
			vmList.add((VirtualMachine) mngEntity[i]);
		}

		PerformanceManager performanceManager = si.getPerformanceManager();
		PerfCounterInfo[] infos = performanceManager.getPerfCounter();
		for (PerfCounterInfo info : infos) {
			headerInfo.put(new Integer(info.getKey()), info);
		}
	}

	/**
	 * Collect the Metrics for Virtual Machines.
	 * @param vmName
	 * @return
	 */
	protected HashMap<String, HashMap<String, String>> getPerfMetrics(
			String vmName) {

		ServiceInstance serviceInstance = null;
		try {
			serviceInstance = new ServiceInstance(url, username,
					password, true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		try {
			InventoryNavigator inventoryNavigator = new InventoryNavigator(
					serviceInstance.getRootFolder());
			VirtualMachine virtualMachine = (VirtualMachine) inventoryNavigator
					.searchManagedEntity("VirtualMachine", vmName);
			if (virtualMachine == null) {
				throw new Exception("Virtual Machine (" + vmName + ") not found");
			}
	
			PerformanceManager performanceManager = serviceInstance
					.getPerformanceManager();
	
			PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
			perfQuerySpec.setEntity(virtualMachine.getMOR());
			perfQuerySpec.setMaxSample(new Integer(maxSamples));
			perfQuerySpec.setFormat("normal");
	
			PerfProviderSummary perfProSummary = performanceManager
					.queryPerfProviderSummary(virtualMachine);
			perfQuerySpec
			.setIntervalId(new Integer(perfProSummary.getRefreshRate().intValue()));
	
			PerfEntityMetricBase[] pValues = performanceManager
					.queryPerf(new PerfQuerySpec[] { perfQuerySpec });
	
			if (pValues != null) {
				return PerformanceUtil.getPerformance(pValues, headerInfo);
			} else {
				throw new Exception("Something went wrong. No values obtained.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<VirtualMachine> getVMs() {
		return vmList;
	}

	public void setVMs(List<VirtualMachine> vmList) {
		this.vmList = vmList;
	}

	public static void main(String[] args) throws Exception {
		VMPerformanceCollector perColl = new VMPerformanceCollector(new URL(
				ConstantUtil.URL), ConstantUtil.ADMIN_USER_NAME,
				ConstantUtil.ADMIN_PASSWORD, 3);
		System.out.println("perfm: "+perColl.getVMs());
		while (true) {
			LogWriter.obj = null;
			writer1 = LogWriter.getInstance();
			for (VirtualMachine vm : perColl.getVMs()) {
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy/MM/dd hh:mm:ss");
				StringBuffer str = new StringBuffer();
				str.append( format.format(date)+",");
				str.append( vm.getName());			
				System.out.println("VM Name: "+vm.getName());

				if (vm.getName().matches("T14.*")) {
					HashMap<String, HashMap<String, String>> metricsMap = perColl
							.getPerfMetrics(vm.getName());

					if (metricsMap == null || metricsMap.isEmpty()) {
						continue;
					}
					
					for (String metricNam : ConstantUtil.METRIC_LIST) {
						HashMap<String, String> metricProps = metricsMap
								.get(metricNam);
						for (String p : metricProps.keySet()) {
							if (ConstantUtil.PARAMETER_LIST.contains(p)) {
								str.append("," + metricProps.get(p));
							}
						}
					}
					
					writer1.write(str.toString(),"writer");
					archivewriter1.write(str.toString(),"archivewriter");
					
					writer1.write("\n","writer");
					archivewriter1.write("\n","archivewriter");
						
					
				}
			}
			//Repeat the process for every 5 seconds.
			Thread.currentThread().sleep(5000);
		}
	}
}
