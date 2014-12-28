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
import java.util.TreeMap;

import com.sjsu.vmservices.util.ConstantUtil;
import com.sjsu.vmservices.util.PerformanceUtil;
import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Create new vHost and re-balance the vmload among 3 vHosts
 */
public class LoadBalancerDRS2 {

	private static HashMap<Integer, PerfCounterInfo> headerInfo = new HashMap<Integer, PerfCounterInfo>();
	private String username;
	private String password;
	private URL url;
	private List<HostSystem> vHostList;
	private List<VirtualMachine> vmList;
	private static LoadBalancerDRS2 vHostMonitor;
	private ServiceInstance si;
	private Folder rootFolder;
	private HostSystem minUsageHost;

	/**
	 * Initialize
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public LoadBalancerDRS2(URL url, String username, String password)
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
	 * Add new vHost
	 */
	private void addVHost() {
		try {
			HostConnectSpec hcs = new HostConnectSpec();
			String hostName = "130.65.133.23";
			hcs.setHostName("130.65.133.23");
			hcs.setPassword(ConstantUtil.ADMIN_PASSWORD);
			hcs.setUserName("root");
			hcs.setSslThumbprint(ConstantUtil.SSL_THUMBPRINT_HOST_143);

			// vCenter ip as management ip
			hcs.setManagementIp("130.65.133.20");

			ManagedEntity[] dcs = new InventoryNavigator(si.getRootFolder())
					.searchManagedEntities("Datacenter");
			ComputeResourceConfigSpec ccr = new ComputeResourceConfigSpec();

			// get the vHost folder to add new vHost
			Task getHostFolderTask = ((Datacenter) dcs[0]).getHostFolder()
					.addStandaloneHost_Task(hcs, ccr, true);

			if (getHostFolderTask.waitForTask() == Task.SUCCESS) {
				System.out.println("New Host (" + hostName + ") Added ");
			}
		} catch (Exception e) {
			System.out.println("Error adding vHost");
			e.printStackTrace();
		}
	}

	/**
	 * Check the machine usage.
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, String> computeVHostsMap() throws Exception {
		Map<Integer, String> selectedVHost = new HashMap<Integer, String>();
		System.out.println("Computing machine usage...");

		for (HostSystem vHost : vHostMonitor.getHosts()) {
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy/MM/dd hh:mm:ss");
			StringBuilder toDisplay = new StringBuilder();
			toDisplay.append("Time: " + format.format(date));
			toDisplay.append(", vHost: " + vHost.getName());
			String key = vHost.getName();
			HashMap<String, HashMap<String, String>> metricsMap = vHostMonitor
					.getvHostMetrics(vHost.getName());

			for (String metricName : ConstantUtil.METRIC_LIST) {
				HashMap<String, String> metricProps = metricsMap.get(metricName);
				for (String currKey : metricProps.keySet()) {
					if (ConstantUtil.PROJECT_PARAMETER_LIST_DRS2.contains(currKey)) {
						toDisplay.append(", " + currKey + ": " + metricProps.get(currKey));
						selectedVHost.put(Integer.parseInt(metricProps.get(currKey)),
								key);
					}
				}
			}
			System.out.println(toDisplay);
		}
		return selectedVHost;
	}

	/**
	 * Retrieve the List of all vHosts sorted based on their usage.
	 * @return
	 * @throws Exception
	 */
	private List<HostSystem> getHostUsageList() throws Exception {
		Map<Integer, String> vHostMap = new TreeMap<Integer, String>(
				vHostMonitor.computeVHostsMap());
		List<HostSystem> sortedHostList = new ArrayList<HostSystem>();

		Map<String, HostSystem> vHostList = new HashMap<String, HostSystem>();
		List<HostSystem> vHosts = vHostMonitor.getHosts();

		for (int i = 0; i < vHostMap.keySet().size(); i++) {
			if ((Float.parseFloat(""
					+ (Integer) (vHostMap.keySet().toArray()[0])) / 100) < ConstantUtil.USAGE_LOWER_LIMIT) {
				String hostName = vHostMap.get(vHostMap.keySet().toArray()[i]);
				for (HostSystem vHost : vHosts) {
					if (vHost.getName().equalsIgnoreCase(hostName)) {
						vHostList.put(hostName, vHost);
						sortedHostList.add(vHost);
					}
				}
			}
		}

		return sortedHostList;
	}

	/**
	 * Retrieve the VM with least usage from the given vHost
	 * @param vHost
	 * @return
	 * @throws Exception
	 */
	private String getTheLightVM(HostSystem vHost) throws Exception {
		Map<String, Integer> selectedVM = new HashMap<String, Integer>();
		VirtualMachine minimumUsageVM = null;
		int vmStat = -1;
		if (vHost.getVms() != null && vHost.getVms().length > 0) {
			for (VirtualMachine vm : vHost.getVms()) {
				HashMap<String, HashMap<String, String>> metricsMap = vHostMonitor
						.collectVMMetrics(vm.getName());
				for (String metricNam : ConstantUtil.METRIC_LIST) {
					HashMap<String, String> metricProps = metricsMap
							.get(metricNam);

					for (String propKey : metricProps.keySet()) {
						if (ConstantUtil.PROJECT_PARAMETER_LIST_DRS2.contains(propKey)) {
							if (vmStat == -1) {
								vmStat = Integer.parseInt(metricProps.get(propKey));
								minimumUsageVM = vm;
							}

							if (vmStat > Integer.parseInt(metricProps.get(propKey))) {
								vmStat = Integer.parseInt(metricProps.get(propKey));
								minimumUsageVM = vm;
								System.out.println("Selected VM:: "	+ vm.getName());
								selectedVM.put(vm.getName(),
										Integer.parseInt(metricProps.get(propKey)));
							}
						}
					}
				}
			}
		}
		System.out.println("vHost:: " + vHost.getName() +", and VM to be Migrated:: " + minimumUsageVM.getName());
		return minimumUsageVM.getName();
	}
	
	/**
	 * Collect the Metrics for Virtual Machines
	 * 
	 * @param vmName
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, HashMap<String, String>> collectVMMetrics(
			String vmName) throws Exception {
		int maxSamples = 3;
		ServiceInstance serviceInstance = new ServiceInstance(url, username,
				password, true);
		InventoryNavigator inventoryNavigator = new InventoryNavigator(
				serviceInstance.getRootFolder());
		VirtualMachine virtualMachine = (VirtualMachine) inventoryNavigator
				.searchManagedEntity("VirtualMachine", vmName);
		System.out.println("Collecting metrics for VM:: " + vmName);
		if (virtualMachine == null) {
			throw new Exception("Virtual Machine (" + vmName + ") not found");
		}

		PerformanceManager performanceManager = serviceInstance
				.getPerformanceManager();

		PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
		perfQuerySpec.setEntity(virtualMachine.getMOR());
		perfQuerySpec.setMaxSample(new Integer(maxSamples));
		perfQuerySpec.setFormat("normal");

		PerfProviderSummary perfSummary = performanceManager
				.queryPerfProviderSummary(virtualMachine);
		perfQuerySpec
				.setIntervalId(new Integer(perfSummary.getRefreshRate().intValue()));

		PerfEntityMetricBase[] result = performanceManager
				.queryPerf(new PerfQuerySpec[] { perfQuerySpec });

		if (result != null) {
			return PerformanceUtil.getPerformance(result, headerInfo);
		} else {
			throw new Exception("Something went wrong. No values obtained.");
		}
	}
	/**
	 * Retrieve the vHost Metrics
	 * 
	 * @param vHostName
	 * @return
	 * @throws Exception
	 */
	protected HashMap<String, HashMap<String, String>> getvHostMetrics(
			String vHostName) throws Exception {

		ServiceInstance serviceInstance = new ServiceInstance(url, username,
				password, true);
		InventoryNavigator inventoryNavigator = new InventoryNavigator(
				serviceInstance.getRootFolder());
		HostSystem vHost = (HostSystem) inventoryNavigator.searchManagedEntity(
				"HostSystem", vHostName);
		if (vHost == null) {
			throw new Exception("vHost (" + vHostName + ") not found");
		}

		PerformanceManager performanceManager = serviceInstance
				.getPerformanceManager();

		PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
		perfQuerySpec.setEntity(vHost.getMOR());
		perfQuerySpec.setMaxSample(new Integer(3));
		perfQuerySpec.setFormat("normal");

		PerfProviderSummary pps = performanceManager
				.queryPerfProviderSummary(vHost);
		perfQuerySpec
				.setIntervalId(new Integer(pps.getRefreshRate().intValue()));

		PerfEntityMetricBase[] pValues = performanceManager
				.queryPerf(new PerfQuerySpec[] { perfQuerySpec });

		if (pValues != null) {
			return PerformanceUtil.getPerformance(pValues, headerInfo);
		} else {
			throw new Exception("No values found!");
		}
	}


	/**
	 * Migrate(Live  or Cold) the specified VM to the given vHost
	 * 
	 * @param vmName
	 * @param newVHost
	 */
	public void migrateVM(String vmName, String newVHost) {
		try {
			Folder rootFolder = si.getRootFolder();

			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
					rootFolder).searchManagedEntity("VirtualMachine", vmName);

			HostSystem newHost = (HostSystem) new InventoryNavigator(rootFolder)
					.searchManagedEntity("HostSystem", newVHost);
			newHost.getName();
			ComputeResource cr = (ComputeResource) newHost.getParent();

			String[] checks = new String[] { "cpu", "software" };
			HostVMotionCompatibility[] vmcs = si.queryVMotionCompatibility(vm,
					new HostSystem[] { newHost }, checks);

			String[] comps = vmcs[0].getCompatibility();
			if (checks.length != comps.length) {
				System.out.println("Could not migrate the VM because of incompatibility.");
			} else {
				Task migrateTask = null;
				String type = null;
				if (VirtualMachinePowerState.poweredOn.equals(vm.getRuntime()
						.getPowerState())) {
					// live migration
					type = "Live";
					migrateTask = vm.migrateVM_Task(cr.getResourcePool(), newHost,
							VirtualMachineMovePriority.highPriority,
							VirtualMachinePowerState.poweredOn);
				} else {
					// cold migration
					type = "Cold";
					migrateTask = vm.migrateVM_Task(cr.getResourcePool(), newHost,
							VirtualMachineMovePriority.highPriority,
							VirtualMachinePowerState.poweredOff);
				}

				if (migrateTask.waitForTask() == Task.SUCCESS) {
					System.out.println("VM (" + vmName +") " + type + " Migration completed.");
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get headerInfo
	 */
	public static HashMap<Integer, PerfCounterInfo> getHeaderInfo() {
		return headerInfo;
	}

	/**
	 * get all vmList
	 */
	public List<VirtualMachine> getVMs() {
		return vmList;
	}

	/**
	 * set VMs
	 */
	public void setVMs(List<VirtualMachine> vmList) {
		this.vmList = vmList;
	}
	
	/**
	 * get list of vHost
	 */
	public List<HostSystem> getHosts() {
		return vHostList;
	}

	/**
	 * set vHosts
	 */
	public void setHosts(List<HostSystem> vHosts) {
		this.vHostList = vHosts;
	}

	/**
	 * get the minimum usage vHost
	 */
	public HostSystem getMinUsageHost() {
		return minUsageHost;
	}

	/**
	 * set the minimum usage vHost
	 */
	public void setMinUsageHost(HostSystem minUsageHost) {
		this.minUsageHost = minUsageHost;
	}

	public static void main(String[] args) throws Exception {
		vHostMonitor = new LoadBalancerDRS2(new URL(ConstantUtil.URL),
				ConstantUtil.ADMIN_USER_NAME, ConstantUtil.ADMIN_PASSWORD);

		// New vHost Name
		String newHost = "130.65.133.23";
		
		// Add a new vHost
		vHostMonitor.addVHost();

		// Find vHost with minimum usage
		List<HostSystem> hostUsageList = vHostMonitor.getHostUsageList();


		if (hostUsageList != null && !hostUsageList.isEmpty()) {
			int index2 = 0;
			for (int index1 = hostUsageList.size() - 1; index1 > 0; index1--) {
				if (index2 > 0) {
					newHost = hostUsageList.get(0).getName();
				}
				HostSystem hostName = hostUsageList.get(index1);
				// Check if the selected vHost has only one VM.
				if (hostName.getVms().length <= 1) {
					System.out
							.println("This vHost has only one VM. So the VM is not migrated.");
				} else {
					// Get the name of the VM to be migrated to the new vHost and migrate.
					String vmName = vHostMonitor.getTheLightVM(hostName);
					if (vmName != null) {
						vHostMonitor.migrateVM(vmName, newHost);
						index2++;
						if (index2 > 1) {
							break;
						}
					}
				}
			}
		}
	}
}
