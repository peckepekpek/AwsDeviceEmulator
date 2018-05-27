/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awsensors.emulator.device.a20;

import awsensors.emulator.device.a20.ioserver.A20IOServer;
import awsensors.emulator.device.a20.ioserver.ListenerList;
import awsensors.emulator.device.a20.ioserver.NetworkUtils;
import awsensors.emulator.device.a20.ioserver.ServerListener;
import awsensors.emulator.device.a20.ioserver.StateChangeListener;
import awsensors.emulator.device.a20.ioserver.Timer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author farnauvi
 */
public class A20Device implements ServerListener {
    private ListenerList ServerListenerList = new ListenerList();
    private ListenerList StateChangeListenerList = new ListenerList();
    private String state;
    private boolean device_running = true;
    public A20IOServer ControlServer = null;
    public A20IOServer DataServer = null;
    private InetAddress ip;
    public Timer timer;
    public NetworkUtils network;
    
    public A20Device() {
        this.state = "IDLE";
        timer = new Timer();
        network = new NetworkUtils();
        timer.inicio();
         // Arrancamos el Pto de Control
        try {
            ip = InetAddress.getLocalHost();
            this.ControlServer = new A20IOServer(9760,ip,"control");  
            this.ControlServer.addServerListener(this);
            this.ControlServer.execute();
            addStateChangeListener((StateChangeListener) ControlServer);
            informaCambiodeEstado(this.state);
           } catch (UnknownHostException ex) {
            Logger.getLogger(DeviceA20Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         // Arrancamos el Pto de Datos
        try {
            ip = InetAddress.getLocalHost();
            this.DataServer = new A20IOServer(7,ip,"datos");  
            this.DataServer.addServerListener(this);
            this.DataServer.execute();
            addStateChangeListener((StateChangeListener) DataServer);
            informaCambiodeEstado(this.state);
           } catch (UnknownHostException ex) {
            Logger.getLogger(DeviceA20Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public boolean isDevice_running() {
        return device_running;
    }

    public void setDevice_running(boolean device_running) {
        this.device_running = device_running;
    }
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void PaqueteControlEnviado(String texto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void PaqueteControlRecibido(String comando) {
        muestraPaqueteControlRecibido(comando);
        procesaComando(comando);
    }
    @Override
    public void PaqueteDatosEnviado(String texto) {
        muestraPaqueteDatosEnviado(texto);
    }

    @Override
    public void PaqueteDatosRecibido(String texto) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void procesaComando (String comando) {
         String respuesta="";
         switch (state) {
             case "IDLE":
                if (comando.equals("sweep!")) {                 
                } 
                if (comando.equals("WCONF?")) {
                    respuesta=timer.now()+" WCONF T T T T T T F F F F F F F";               
                }
                if (comando.equals("CAL RESTORE")) {
                    respuesta=timer.now()+" CAL";
                }
                if (comando.equals("$R00?")) {
                    respuesta=timer.now()+" $R00? +2.000000e+01";
                }
                if (comando.equals("acq")) {
                    respuesta=timer.now()+" acq";
                    state="ACTIVE";
                    informaCambiodeEstado(this.state);
                }
                if (comando.equals("WNETCONF?")) {
                      respuesta=timer.now()+" WNETCONF "+network.config()+" 1234567 v4.3";
                }
                
                break;
             case "ACTIVE":  
                if (comando.equals("acq!")) {
                    respuesta=timer.now()+" acq!";
                }
                if (comando.equals("acq")) {
                    respuesta=timer.now()+" acq";
                    state="SETTING_FREQ";
                    informaCambiodeEstado(this.state);
                }
                 if (comando.startsWith("NSENSORS")) {
                    respuesta=timer.now()+" NSENSORS 1 0 0 0";
                    state="IDLE";
                    informaCambiodeEstado(this.state);
                }  
                if (comando.startsWith("analizador 38654705")) {
                    this.DataServer.sweepFinished=false;
                    respuesta=timer.now()+" analizador";
                    state="SWEEPING";
                    informaCambiodeEstado(this.state);
                } else if (comando.startsWith("analizador")) {
                    this.DataServer.sweepFinished=false;
                    respuesta=timer.now()+" analizador";
                    state="AUTOSET";
                    informaCambiodeEstado(this.state);              
                } 
                break;
                
              case "SWEEPING":  
                if (comando.equals("sweep!")) {
                    respuesta=timer.now()+" sweep!";
                    state="IDLE";
                    informaCambiodeEstado(this.state);
                } 
                if (comando.equals("acq")) {
                    respuesta=timer.now()+" acq";
                    state="ACTIVE";
                    informaCambiodeEstado(this.state);
                } 
                break;
                
               case "AUTOSET":
                 if (comando.equals("acq")) {
                    respuesta=timer.now()+" sweep!";
                    state="IDLE";
                    informaCambiodeEstado(this.state);
                }   
                break;
                
               case "SETTING_FREQ":
                 if (comando.equals("PLLON")) {
                    respuesta=timer.now()+" PLLON";
                }   
                 if (comando.equals("FNCOI")) {
                    respuesta=timer.now()+" FNCOI";
                }  
                 if (comando.startsWith("FNCOC")) {
                    respuesta=timer.now()+" NCh";
                }  
                break;
               
         }       
        ControlServer.escribeAWSuite(respuesta);
        muestraPaqueteControlEnviado(respuesta);
               
    }
     
    public void addServerListener(ServerListener listener)
       {
               ServerListenerList.add(listener);
       }
     
    public void addStateChangeListener(StateChangeListener listener)
       {
               StateChangeListenerList.add(listener);
       }
    
     public void muestraPaqueteControlEnviado(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        listener.PaqueteControlEnviado(texto);
		}
	}
     public void muestraPaqueteControlRecibido(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        listener.PaqueteControlRecibido(timer.now()+":"+texto);
		}
	}
     
      public void muestraPaqueteDatosEnviado(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        listener.PaqueteDatosEnviado(texto);
		}
	}
     public void muestraPaqueteDatosRecibido(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        listener.PaqueteDatosRecibido(texto);
		}
	}
     
      public void informaCambiodeEstado (String estado)
	{
		int listenerSize = StateChangeListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			StateChangeListener listener = (StateChangeListener)StateChangeListenerList.get(n);
                        listener.CambioEstadoProducido(estado);
		}
	}

    @Override
    public void AlertaHilo(String tipo) {
        if (tipo.equals("SweepEnd")) {
            String comando=timer.now()+" Analizador";
            ControlServer.escribeAWSuite(comando);
            muestraPaqueteControlEnviado(comando);
        }
        if (tipo.equals("SweepEndAutoset")) {
            String comando=timer.now()+" Analizador";
            ControlServer.escribeAWSuite(comando);
            muestraPaqueteControlEnviado(comando);
            state="ACTIVE";
            informaCambiodeEstado(this.state);
        }
        
    }
}
