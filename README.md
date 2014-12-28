LogManager
==========
The theme is a large-scale statistics gathering and analysis tool in scalable virtualized environments with an emphasis on resource scheduling.

The goal of this project is: 1. Practice with virtualized environment, managing, load balancing and testing VM. 2. Apply open source tools like Logstash or Scribe 3. Understanding the need of gathering and analyze for large data 4. Visualize the collected data using the tools like google charts

Problem description Develop a simple DRS (Distributed Resource Scheduler) and DPM (Distributed Power Management) function, and large-scale statistics gathering and analysis in scalable virtualized environments. Specific areas of interest include health models for multi-tier applications, VM and host performance, and detection of anomalies.

Part 1: 1. Simple DRS and DPM implementation a) DRS1, Initial placement: Create 2 vHosts, each running 2 VMs. Each VM should run some programs such as like Prime95 or Folding@Home to keep a vCPU busy. Create a new VM and place it among one of the two existing vHosts. Placement is based on CPU load. (capture before and after screenshots) b) DRS2, Load balancing: Create new vHost and re-balance the vmload among 3 vHosts. (create multiple VMs and load unbalance for setup, capture before and after screenshots) c) DPV: Terminate VMs until you reach lower threshold (typically average 30% cpu load), then DPM kicks in by consolidating VMs into two vHosts and shut down one vHost. Repeat the same until you reach one vHost. (capture before and after screenshots)
