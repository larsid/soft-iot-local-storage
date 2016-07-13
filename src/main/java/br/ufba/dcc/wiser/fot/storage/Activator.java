package br.ufba.dcc.wiser.fot.storage;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    static BundleContext bc;
    MqttFusekiController mc;
    private ServiceTracker m_tracker = null;

    public void start(BundleContext bc) throws Exception {
        System.out.println("Starting the bundle FoT Storage");

        // Buscando o serviço dinamicamente
        m_tracker = new ServiceTracker(
                bc,
                bc.createFilter(
                        "(objectClass=" + IMqttFusekiController.class.getName() + ")"),
                null);
        m_tracker.open();

        // Criando uma thread para buscar o serviço, se existir serviço ele chama o init e para o loop infinito
        Thread thread = new Thread(new Runnable() {

            private IMqttFusekiController iMqttFusekiController;

            public void run() {
                while (true) {
                    // A cada 3 segundos busca o serviço. Se encontrou chama o init e para de buscar
                    try {
                        try {
                            iMqttFusekiController = (IMqttFusekiController) m_tracker.getService();
                            iMqttFusekiController.init();
                            break;
                        } catch (Exception exception) {}
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        thread.start();

    }

    public void stop(BundleContext bc) throws Exception {
        System.out.println("Stopping the bundle FoT Storage");
        mc.disconnect();
    }

}
