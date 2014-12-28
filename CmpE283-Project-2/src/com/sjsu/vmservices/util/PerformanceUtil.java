package com.sjsu.vmservices.util;

import java.util.HashMap;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;

public class PerformanceUtil {

	/**
	 * This method will generate the Performance Results based on the performance metrics
	 * @param perfMetricBase
	 * @return
	 */
	public static HashMap<String, HashMap<String, String>> getPerformance(PerfEntityMetricBase[] perfMetricBase, HashMap<Integer, PerfCounterInfo> headerInfo) {
		HashMap<String, HashMap<String, String>> propertyGroupsMap = new HashMap<String, HashMap<String, String>>();
		for (PerfEntityMetricBase perfEntityBase : perfMetricBase) {
			PerfEntityMetric pem = (PerfEntityMetric) perfEntityBase;
			PerfMetricSeries[] pms = pem.getValue();
			for (PerfMetricSeries pmSeries : pms) {
				String propValue = "";
				int counterId = pmSeries.getId().getCounterId();
				PerfCounterInfo info = headerInfo.get(new Integer(counterId));

				if (pmSeries instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries series = (PerfMetricIntSeries) pmSeries;
					long[] values = series.getValue();
					long result = 0;
					for (long value : values) {
						result += value;
					}
					result = (long) (result / values.length);
					propValue = String.valueOf(result);
				} else if (pmSeries instanceof PerfMetricSeriesCSV) {
					PerfMetricSeriesCSV seriesCsv = (PerfMetricSeriesCSV) pmSeries;
					propValue = seriesCsv.getValue() + " in "
							+ info.getUnitInfo().getLabel();
				}

				HashMap<String, String> props;
				if (propertyGroupsMap.containsKey(info.getGroupInfo().getKey())) {
					props = propertyGroupsMap.get(info.getGroupInfo()
							.getKey());
				} else {
					props = new HashMap<String, String>();
					propertyGroupsMap
							.put(info.getGroupInfo().getKey(), props);
				}

				String propName = String.format("%s_%s", info.getGroupInfo()
						.getKey(), info.getNameInfo().getKey());
				props.put(propName, propValue);
			}
		}
		return propertyGroupsMap;
	}
	/*public static HashMap<String, HashMap<String, String>> collectPerformanceValues(
			PerfEntityMetricBase[] results, HashMap<Integer, PerfCounterInfo> headerInfo) {
		HashMap<String, HashMap<String, String>> propertyGroupsMap = new HashMap<String, HashMap<String, String>>();
		for (PerfEntityMetricBase pemBase : results) {
			PerfEntityMetric perfEntityMetric = (PerfEntityMetric) pemBase;
			PerfMetricSeries[] perfMetriSeries = perfEntityMetric.getValue();
			for (PerfMetricSeries perfMetric : perfMetriSeries) {
				int counterId = perfMetric.getId().getCounterId();
				PerfCounterInfo counterInfo = headerInfo.get(new Integer(counterId));
				String value = "";
				HashMap<String, String> props;

				if (perfMetric instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries series = (PerfMetricIntSeries) perfMetric;
					long[] values = series.getValue();
					long result = 0;
					for (long v : values) {
						result += v;
					}
					result = (long) (result / values.length);
					value = String.valueOf(result);
				} else if (perfMetric instanceof PerfMetricSeriesCSV) {
					PerfMetricSeriesCSV csv = (PerfMetricSeriesCSV) perfMetric;
					value = csv.getValue() + " in "	+ counterInfo.getUnitInfo().getLabel();
				}

				if (propertyGroupsMap.containsKey(counterInfo.getGroupInfo().getKey())) {
					props = propertyGroupsMap.get(counterInfo.getGroupInfo()
							.getKey());
				} else {
					props = new HashMap<String, String>();
					propertyGroupsMap
							.put(counterInfo.getGroupInfo().getKey(), props);
				}
				// Set the Property Name
				String propName = String.format("%s_%s", counterInfo.getGroupInfo()
						.getKey(), counterInfo.getNameInfo().getKey());
				props.put(propName, value);
			}
		}
		return propertyGroupsMap;
	}*/
}


/* from vmperfColl
 * private HashMap<String, HashMap<String, String>> generatePerformanceResult(
			PerfEntityMetricBase[] pValues) {
		HashMap<String, HashMap<String, String>> propertyGroups = new HashMap<String, HashMap<String, String>>();
		for (PerfEntityMetricBase p : pValues) {
			PerfEntityMetric pem = (PerfEntityMetric) p;
			PerfMetricSeries[] pms = pem.getValue();
			for (PerfMetricSeries pm : pms) {
				int counterId = pm.getId().getCounterId();
				PerfCounterInfo info = headerInfo.get(new Integer(counterId));

				String value = "";

				if (pm instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries series = (PerfMetricIntSeries) pm;
					long[] values = series.getValue();
					long result = 0;
					for (long v : values) {
						result += v;
					}
					result = (long) (result / values.length);
					value = String.valueOf(result);
				} else if (pm instanceof PerfMetricSeriesCSV) {
					PerfMetricSeriesCSV seriesCsv = (PerfMetricSeriesCSV) pm;
					value = seriesCsv.getValue() + " in "
							+ info.getUnitInfo().getLabel();
				}

				HashMap<String, String> properties;
				if (propertyGroups.containsKey(info.getGroupInfo().getKey())) {
					properties = propertyGroups.get(info.getGroupInfo()
							.getKey());
				} else {
					properties = new HashMap<String, String>();
					propertyGroups
							.put(info.getGroupInfo().getKey(), properties);
				}

				String propName = String.format("%s_%s", info.getGroupInfo()
						.getKey(), info.getNameInfo().getKey());
				properties.put(propName, value);
			}
		}
		return propertyGroups;

	}
 * */

/* from dpm
 * private HashMap<String, HashMap<String, String>> collectPerfResults(
			PerfEntityMetricBase[] pValues) {
		HashMap<String, HashMap<String, String>> propertyGroupsMap = new HashMap<String, HashMap<String, String>>();
		for (PerfEntityMetricBase p : pValues) {
			PerfEntityMetric pemBase = (PerfEntityMetric) p;
			PerfMetricSeries[] pms = pemBase.getValue();
			for (PerfMetricSeries pm : pms) {
				int counterId = pm.getId().getCounterId();
				PerfCounterInfo info = headerInfo.get(new Integer(counterId));

				String value = "";

				if (pm instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries series = (PerfMetricIntSeries) pm;
					long[] values = series.getValue();
					long result = 0;
					for (long v : values) {
						result += v;
					}
					result = (long) (result / values.length);
					value = String.valueOf(result);
				} else if (pm instanceof PerfMetricSeriesCSV) {
					PerfMetricSeriesCSV seriesCsv = (PerfMetricSeriesCSV) pm;
					value = seriesCsv.getValue() + " in "
							+ info.getUnitInfo().getLabel();
				}

				HashMap<String, String> properties;
				if (propertyGroupsMap.containsKey(info.getGroupInfo().getKey())) {
					properties = propertyGroupsMap.get(info.getGroupInfo()
							.getKey());
				} else {
					properties = new HashMap<String, String>();
					propertyGroupsMap
							.put(info.getGroupInfo().getKey(), properties);
				}

				String propName = String.format("%s_%s", info.getGroupInfo()
						.getKey(), info.getNameInfo().getKey());
				properties.put(propName, value);
			}
		}
		return propertyGroupsMap;
	}
 * */
 