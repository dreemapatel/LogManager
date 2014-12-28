package com.sjsu.vmservices.part1;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sjsu.vmservices.util.ConstantUtil;
import com.sjsu.vmservices.util.PerformanceUtil;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
/**
 * This class terminate VMs until it reaches lower threshold (typically average 30% cpu load),
 *  then DPM kicks in by consolidating VMs into two vHosts and shut down one vHost; 
 *  Ans repeats the same until there is only one vHost in the vCenter.
 */
public class BasicDPM {
	private static HashMap<Integer, PerfCounterInfo> headerInfo = new HashMap<Integer, PerfCounterInfo>();
	private String username;
	private String password;
	private URL url;
	private ServiceInstance si;
	private Folder rootFolder;
	private List<HostSystem> vHostList;
	private static BasicDPM dpmService;
	private static boolean start = false;

	public BasicDPM(URL url, String username, String password)
			throws RemoteException, MalformedURLException {
		this.url = url;
		this.username = username;
		this.password = password;
		
		this.si = new ServiceInstance(url, username, password, true);
		this.rootFolder = si.getRootFolder();

		PerformanceManager performanceManager = si.getPerformanceManager();
		PerfCounterInfo[] perfCounterInfo = performanceManager.getPerfCounter();
		for (PerfCounterInfo info : perfCounterInfo) {
			headerInfo.put(new Integer(info.getKey()), info);
		}
	}

	private HashMap<String, HashMap<String, String>> getPerfMetrics(
			String vHostmName) throws Exception {

		ServiceInstance serviceInstance = new ServiceInstance(url, username,
				password, true);
		InventoryNavigator inventoryNavigator = new InventoryNavigator(
				serviceInstance.getRootFolder());
		HostSystem vHost = (HostSystem) inventoryNavigator.searchManagedEntity(
				"HostSystem", vHostmName);
		if (vHost == null) {
			throw new Exception("vHost (" + vHostmName + ") is not found");
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

		PerfEntityMetricBase[] results = performanceManager
				.queryPerf(new PerfQuerySpec[] { perfQuerySpec });

		if (results != null) {
			return PerformanceUtil.getPerformance(results, headerInfo);
		} else {
			throw new Exception("Something wrong. No values found.");
		}
	}

	/**
	 * Check the machine usage.
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, String> computeVHostMap() throws Exception {
		Map<Integer, String> selectedVHost = new HashMap<Integer, String>();

		for (HostSystem vHost : dpmService.getHostList()) {
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy/MM/dd hh:mm:ss");
			StringBuffer stats = new StringBuffer();
			stats.append("Time: " + format.format(date));
			stats.append(", vHost: " + vHost.getName());
			String key = vHost.getName();
			HashMap<String, HashMap<String, String>> metricsMap = dpmService
					.getPerfMetrics(vHost.getName());

			for (String metricNam : ConstantUtil.METRIC_LIST) {
				HashMap<String, String> metricProps = metricsMap.get(metricNam);

				for (String prop : metricProps.keySet()) {
					if (ConstantUtil.PROJECT_PARAMETER_LIST_DRS2.contains(prop)) {
						stats.append(", " + prop + ": " + metricProps.get(prop));
						selectedVHost.put(Integer.parseInt(metricProps.get(prop)),key);
						if((Float.parseFloat(metricProps.get(prop))/100) <  30)
						{
							start = true;
						}
					}
				}
			}
			System.out.println(stats);
		}
		return selectedVHost;
	}

	/**
	 * Return the vHosts in the ascending order of thier usage.
	 * @return
	 * @throws Exception
	 */
	private Map<String, HostSystem> getSortedVHostMapByUsage() throws Exception {
		Map<Integer, String> vHostMap = new TreeMap<Integer, String>(
				dpmService.computeVHostMap());

		Map<String, HostSystem> vHostList = new LinkedHashMap<String, HostSystem>();
		List<HostSystem> vHosts = dpmService.getHostList();

		for (int index = 0; index < vHostMap.keySet().size(); index++) {
			String hostname = vHostMap.get(vHostMap.keySet().toArray()[index]);
			for (HostSystem vHost : vHosts) {
				if (vHost.getName().equalsIgnoreCase(hostname)) {
					vHostList.put(hostname, vHost);
				}
			}
		}
		return vHostList;
	}

	/**
	 * Migrate VMs from vHosts with less than 30% usage and remove the vHost.
	 * @throws Exception
	 */
	private void consolidate() throws Exception {
		Map<String, HostSystem> vHostList = dpmService.getSortedVHostMapByUsage();
		boolean valid = true;
		if (vHostList != null && !vHostList.isEmpty()) {
			// identify vHost whose VMs need to be migrated
			if(start)	{
			HostSystem leastUsageHost = vHostList.get((vHostList.keySet().toArray()[0]));
			VirtualMachine[] vms = leastUsageHost.getVms();

			// now start migrating the above VMs to second host
			for (int i = 0; i < vms.length; i++) {
				VirtualMachine vm = vms[i];
				HostSystem newHost = vHostList.get((vHostList.keySet()
						.toArray()[1]));

				System.out.println(newHost.getName());
				ComputeResource cr = (ComputeResource) newHost.getParent();

				String[] checks = new String[] { "cpu", "software" };
				HostVMotionCompatibility[] vmcs = si.queryVMotionCompatibility(
						vm, new HostSystem[] { newHost }, checks);

				String[] comps = vmcs[0].getCompatibility();
				if (checks.length != comps.length) {
					System.out.println("Could not migrate the VM because of incompatibility.");
					valid = false;
				}

				if (valid) {
					Task migrateTask = null;
					if (VirtualMachinePowerState.poweredOn.equals(vm
							.getRuntime().getPowerState())) {
						// live migration
						System.out
								.println("Live Migration of: " + vm.getName());
						migrateTask = vm.migrateVM_Task(cr.getResourcePool(), newHost,
								VirtualMachineMovePriority.highPriority,
								VirtualMachinePowerState.poweredOn);
					} else {
						// cold migration
						System.out
								.println("Cold Migration of: " + vm.getName());
						migrateTask = vm.migrateVM_Task(cr.getResourcePool(), newHost,
								VirtualMachineMovePriority.highPriority,
								VirtualMachinePowerState.poweredOff);
					}

					if (migrateTask.waitForTask() == Task.SUCCESS) {
						System.out.println("VM Migrated Successfully.");

					}
				}
			}
			// remove the processed vHost
			ComputeResource cr = (ComputeResource) vHostList.get(
					(vHostList.keySet().toArray()[0])).getParent();
			System.out.println("Removing the vHost " + vHostList.get((vHostList.keySet().toArray()[0])).getName());
			Task task = cr.destroy_Task();
			if (task.waitForTask() == Task.SUCCESS) {
				System.out.println("vHost Removed. ");
			}
			}	else 	{
				System.out.println("All vHosts are busy; usage is above 30%. Not migrating any VM or remoing any vHost.");
			}
		} else {
			System.out.println("There are no vHosts available to check.");
		}
	}

	/**
	 * @return the hostList
	 */
	public List<HostSystem> getHostList() {
		return vHostList;
	}

	/**
	 * @param hostList
	 *            the hostList to set
	 */
	public void setHostList(List<HostSystem> hostList) {
		this.vHostList = hostList;
	}

	public static void main(String[] args) throws Exception {
		dpmService = new BasicDPM(new URL(ConstantUtil.URL),
				ConstantUtil.ADMIN_USER_NAME, ConstantUtil.ADMIN_PASSWORD);

		// consolidate VMs and vHosts
		while (true) {
			ManagedEntity[] hostsEntity = new InventoryNavigator(dpmService.rootFolder).searchManagedEntities("HostSystem");
			dpmService.vHostList = new ArrayList<HostSystem>();
			dpmService.start = false;
			for (int i = 0; i < hostsEntity.length; i++) {
				dpmService.vHostList.add((HostSystem) hostsEntity[i]);
			}
			if (hostsEntity.length > 1) {
				dpmService.consolidate();
			} else {
				break;
			}
		}
	}
}
