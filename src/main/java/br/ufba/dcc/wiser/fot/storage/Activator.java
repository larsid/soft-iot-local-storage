
package br.ufba.dcc.wiser.fot.storage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;



public class Activator implements BundleActivator, BundleListener {
    static BundleContext bc;
    
    MqttCxfController mc;
    
    public void start(BundleContext bc) throws Exception {
        System.out.println("Starting the bundle FoT Storage");
        
        bc.addBundleListener(this);
        
    }

    public void stop(BundleContext bc) throws Exception {
        System.out.println("Stopping the bundle FoT Storage");
        bc.removeBundleListener(this);
    }


	public void bundleChanged(BundleEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void setMc(MqttCxfController mc) {
		this.mc = mc;
	}

}