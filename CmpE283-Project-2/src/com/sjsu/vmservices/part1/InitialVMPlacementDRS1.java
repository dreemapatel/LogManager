package com.sjsu.vmservices.part1;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sjsu.vmservices.util.ConstantUtil;
import com.sjsu.vmservices.util.PerformanceUtil;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * 
 * This class handles creating a new VM and place it among one of the two
 * existing vHosts. Placement is based on CPU load
 *
 */
public class InitialVMPlacementDRS1 {
	private static HashMap<Integer, PerfCounterInfo> headerInfo = new HashMap<Integer, PerfCounterInfo>();
	private String username;
	private String password;
	private ServiceInstance si;
	private Folder rootFolder;
	private URL url;
	private List<HostSystem> vHostList;
	private List<VirtualMachine> vmList;
	private static InitialVMPlacementDRS1 vHostMonitor;

	/**
	 * Initialize
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public InitialVMPlacementDRS1(URL url, String username, String password)
			throws RemoteException, MalformedURLException {
		this.url = url;
		this.username = username;
		this.password = password;
		this.vHostList = new ArrayList<HostSystem>();
		this.vmList = new ArrayList<VirtualMachine>();
		this.si = new ServiceInstance(url, username, password, true);
		this.rootFolder = si.getRootFolder();

		//List of Host Systems
		ManagedEntity[] hostsEntity = new InventoryNavigator(rootFolder)
				.searchManagedEntities("HostSystem");
		for (int i = 0; i < hostsEntity.length; i++) {
			vHostList.add((HostSystem) hostsEntity[i]);
		}

		// List of VMs
		ManagedEntity[] vmsEntity = new InventoryNavigator(rootFolder)
				.searchManagedEntities("VirtualMachine");
		for (int i = 0; i < vmsEntity.length; i++) {
			vmList.add((VirtualMachine) vmsEntity[i]);
		}

		PerformanceManager performanceManager = si.getPerformanceManager();
		PerfCounterInfo[] infos = performanceManager.getPerfCounter();
		for (PerfCounterInfo info : infos) {
			headerInfo.put(new Integer(info.getKey()), info);
		}
	}

	/**
	 * Collect the Performance Metrics.
	 * @param vHostmName
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, HashMap<String, String>> getPerfMetrics(
			String vHostmName) throws Exception {

		ServiceInstance serviceInstance = new ServiceInstance(url, username,
				password, true);
		InventoryNavigator inventoryNavigator = new InventoryNavigator(
				serviceInstance.getRootFolder());
		HostSystem vHost = (HostSystem) inventoryNavigator.searchManagedEntity(
				"HostSystem", vHostmName);
		if (vHost == null) {
			throw new Exception("vHost: '" + vHostmName + "' is not found.");
		}

		PerformanceManager perfMgr = serviceInstance
				.getPerformanceManager();

		PerfQuerySpec perfQuerySpecs = new PerfQuerySpec();
		perfQuerySpecs.setEntity(vHost.getMOR());
		perfQuerySpecs.setMaxSample(new Integer(3));
		perfQuerySpecs.setFormat("normal");

		PerfProviderSummary pps = perfMgr
				.queryPerfProviderSummary(vHost);
		perfQuerySpecs
				.setIntervalId(new Integer(pps.getRefreshRate().intValue()));

		PerfEntityMetricBase[] results = perfMgr
				.queryPerf(new PerfQuerySpec[] { perfQuerySpecs });

		if (results != null) {
			return PerformanceUtil.getPerformance(results, headerInfo);
		} else {
			throw new Exception("Something went wrong. No values obtained.");
		}
	}

	/**
	 * Add VM
	 * @throws Exception
	 */
	private void addVirtualMachine() throws Exception {
		Map<String, Integer> vHostMap = vHostMonitor.computeVHostUsage();
		int vHost1 = vHostMap.get("130.65.133.21");
		int vHost2 = vHostMap.get("130.65.133.22");
		ResourcePool resPool = null;
		String targetHostName;

		String dcName = "Team14_DC";
		Datacenter dc = (Datacenter) new InventoryNavigator(
				vHostMonitor.rootFolder).searchManagedEntity("Datacenter",
				dcName);

		if (vHost1 < vHost2) {
			targetHostName = "T14-vHost01-cum3_IP=.133.21";
			resPool = (ResourcePool) new InventoryNavigator(dc)
					.searchManagedEntities("ResourcePool")[0];

		} else {
			targetHostName = "T14-vHost02-cum3_IP=.133.22";
			resPool = (ResourcePool) new InventoryNavigator(dc)
					.searchManagedEntities("ResourcePool")[1];
		}
		System.out.println("Ready to add VM.");

		String newVMName = "NewVM";
		long memorySizeMB = 512;//MB
		int cpuCount = 1;
		String guestOsId = "ubuntuGuest";
		String datastoreName = "nfs1team14";

		//VM config spec
		VirtualMachineConfigSpec vmConfSpec = new VirtualMachineConfigSpec();
		vmConfSpec.setName(newVMName);
		vmConfSpec.setAnnotation("VirtualMachine Annotation");
		vmConfSpec.setMemoryMB(memorySizeMB);
		vmConfSpec.setNumCPUs(cpuCount);
		vmConfSpec.setGuestId(guestOsId);

		VirtualMachineFileInfo vmfileInfo = new VirtualMachineFileInfo();
		vmfileInfo.setVmPathName("[" + datastoreName + "]");
		vmConfSpec.setFiles(vmfileInfo);

		Folder vmFolder = dc.getVmFolder();
		Task task = vmFolder.createVM_Task(vmConfSpec, resPool, null);

		@SuppressWarnings("deprecation")
		String result = task.waitForMe();
		if (result == Task.SUCCESS) {
			System.out.println("VM (" + newVMName + ") Created @ " + targetHostName);
		} else {
			System.out.println("VM (" + newVMName + ") Creation Failed @ " + targetHostName);
		}
	}

	/**
	 * Check the VHost CPU usage and choose the right vHost to which new VM will be added.
	 * @return
	 * @throws Exception
	 */
	private Map<String, Integer> computeVHostUsage() throws Exception {
		Map<String, Integer> values = new HashMap<String, Integer>();
		System.out.println("Computing vHosts CPU Usage...");
		// Compute the value of the CPU usage for all vHosts.
		for (HostSystem vHost : vHostMonitor.getHosts()) {
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy/MM/dd hh:mm:ss");
			StringBuilder toDisplay = new StringBuilder();
			toDisplay.append("Time: " + format.format(date));
			toDisplay.append(", vHost: " + vHost.getName());
			String key = vHost.getName();
			HashMap<String, HashMap<String, String>> metricsMap = vHostMonitor
					.getPerfMetrics(vHost.getName());

			for (String metricName : ConstantUtil.METRIC_LIST) {
				HashMap<String, String> metricProps = metricsMap.get(metricName);

				for (String currKey : metricProps.keySet()) {
					if (ConstantUtil.PROJECT_PARAMETER_LIST_DRS1.contains(currKey)) {
						// vHostCPU.add(Integer.parseInt(metricProps.get(p)));
						toDisplay.append(", " + currKey + ": " + metricProps.get(currKey));
						values.put(key,
								Integer.parseInt(metricProps.get(currKey)));
					}
				}
			}
			System.out.println(toDisplay);
		}
		return values;
	}

	/**
	 * Return List of VMs
	 */
	public List<VirtualMachine> getVms() {
		return vmList;
	}

	/**
	 * Set VMs
	 */
	public void setVms(List<VirtualMachine> vms) {
		this.vmList = vms;
	}

	/**
	 * Return List of VHosts
	 */
	private List<HostSystem> getHosts() {
		return vHostList;
	}

	/**
	 * Set Hosts
	 */
	public void setHosts(List<HostSystem> hosts) {
		this.vHostList = hosts;
	}


	public static void main(String[] args) throws Exception {
		vHostMonitor = new InitialVMPlacementDRS1(new URL(ConstantUtil.URL),
				ConstantUtil.ADMIN_USER_NAME, ConstantUtil.ADMIN_PASSWORD);
		vHostMonitor.addVirtualMachine();
	}
}
