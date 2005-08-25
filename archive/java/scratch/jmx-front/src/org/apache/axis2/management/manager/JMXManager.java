package org.apache.axis2.management.manager;

import org.apache.axis2.engine.AxisFault;

import java.rmi.registry.LocateRegistry;
import org.apache.commons.modeler.Registry;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class JMXManager{

	private Registry registry;
	private static JMXManager jmxManager = null;
	private MBeanServer mbs = null;

	JMXConnectorServer cs;

	public static JMXManager getJMXManager(){

		if(jmxManager != null){
			return jmxManager;
		}
		else{
			jmxManager = new JMXManager();
			return jmxManager;
		}

	}

	private JMXManager(){

		try{
			initMBeanServer();
			publishRMI();
		}catch(Exception e){
		}
	}


	public void startMBeanServer() throws AxisFault{


		 try{
			//initMBeans();
		 }catch(Exception e){
			throw new AxisFault(e.getMessage());
		 }

	}



	public void initModeler() throws Exception{

		Registry registry = Registry.getRegistry(null, null);
		mbs = registry.getMBeanServer();

	}


	public void registerModelerMBeans(Object mbean, String mbeanName) throws Exception{

		registry.registerComponent(mbean, mbeanName, "Axis2Manager");
	}


	private void initMBeanServer() throws Exception{

			// try to find existing MBeanServers. If there are no MBeanServers create a new one for Axis2
			if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
				mbs = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
			} else {
				mbs = MBeanServerFactory.createMBeanServer();
			}

	}


	public void registerMBean(Object mbean, String mbeanName) throws Exception{

		ObjectName mbeanObjectName = ObjectName.getInstance(mbeanName);
		mbs.registerMBean(mbean, mbeanObjectName);
	}


	public void publishRMI() throws Exception{

		// RMI registry
		java.rmi.registry.Registry reg=null;

		try {
			if( reg==null )
				reg=LocateRegistry.createRegistry(9995);
		 } catch(Exception e) {
				throw new AxisFault(e.getMessage());
		 }

		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9995/axis2");
		cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
		cs.start();
	}

}