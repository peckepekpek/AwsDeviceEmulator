/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awsensors.emulator.device.a20.ioserver;
import awsensors.emulator.device.a20.A20Device;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author farnauvi
 */
    public class A20IOServer extends SwingWorker<Void, Void> implements Runnable,StateChangeListener {
    public ServerSocket welcomeSocket;
    public boolean pause = false;
    private Thread hilo_server  = null;
    private ListenerList ServerListenerList = new ListenerList();
    private BufferedReader BRDataPort;
    char[] buf = new char[192];
    InetAddress ip;
    int port=1000;
    String tipo;
    DataOutputStream outToClient;
    String received;
    public boolean portActive = true;
    public boolean sweepFinished = false;
    String state;
    Timer timer;
    URL location;

    public A20IOServer( int port, InetAddress ip, String tipo ){
           this.ip  = ip;
           this.port = port;
           this.tipo = tipo;
           timer = new Timer();
        }


    @Override
     public Void doInBackground() {
         try {
             while (this.portActive) {
                 ioprocess();
             }
          } catch( Exception e ) {
             System.out.println( "Exception in server proccess: "
                     + e.getMessage() );
         }
     return null;
     }
    
        
    public synchronized void startServer() {
        if (hilo_server == null) {        
           hilo_server = new Thread(this);
           hilo_server.start();
       }
    }
    
    @Override
    public void done() {   
       try {
           welcomeSocket.close();
       } catch (IOException ex) {
           Logger.getLogger(A20IOServer.class.getName()).log(Level.SEVERE, null, ex);
       }   
    }
     
    public void addServerListener(ServerListener listener)
       {
               ServerListenerList.add(listener);
       }
     
     private void ioprocess() {          
        while (this.portActive) {        
            try {
                welcomeSocket = new ServerSocket(port);
                Socket connectionSocket = welcomeSocket.accept();
                BRDataPort = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                while (this.portActive) {      
                    if (readyDataPortTCP())  { 
                      recibeDatosFromAWSsuite(BRDataPort);
                    }
                    if (tipo.equals("datos")) {
                        if (state.equals("SWEEPING")) {
                            enviaSweep();                           
                        } 
                        if (state.equals("ACTIVE")) {
                            mantieneConexion();
                        }
                        if (state.equals("AUTOSET")) {
                            enviaSweepAutoSet();
                        }
                        if (state.equals("SETTING_FREQ")) {
                            enviaExperimento();
                        }             
                    } 
                }
                if (!this.portActive) {
                    connectionSocket.close();
                    welcomeSocket.close();
                }
            }
            catch (Exception e) {
                System.out.print("Exception:"+e);
            }
        }
     }
           
     public void runPaquetesRecibidos(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        if (tipo.equals("control")) {
                            listener.PaqueteControlRecibido(texto);
                        }
                        if (tipo.equals("datos")) {
                            listener.PaqueteDatosRecibido(texto);
                        }
		}
	}
     
     public void runPaquetesEnviados(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
			 if (tipo.equals("control")) {
                            listener.PaqueteControlEnviado(texto);
                        }
                        if (tipo.equals("datos")) {
                            listener.PaqueteDatosEnviado(texto);
                        }
		}
	}
     

    private void recibeDatosFromAWSsuite (BufferedReader inFromAWSSuite) throws Exception {
         try {           
             received = inFromAWSSuite.readLine();
             runPaquetesRecibidos(received);
         }
         catch (Exception e) {
            System.out.print("Error:"+e);
         }
         
     }
    
    public boolean readyDataPortTCP() {
        // Devuelve true si hay datos nuevos en el socket o false en caso contrario
        try {
            if (BRDataPort.ready()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //System.out.print("Error al comprobar si hay datos en el socket:"+e+"\n");
            return false;
        }
    }
    
    
    
    public synchronized void escribeAWSuite(String query) {
        // Función que envía un comando por el puerto de control
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            for (int i = 0; i < query.length(); i++) {
                salida.write(query.charAt(i));
            }
            salida.write(13);   // Retorno de carro
            salida.write(10);
            salida.writeTo(outToClient);
            outToClient.flush();
        } catch (Exception e) {
        }
    }

    @Override
    public void CambioEstadoProducido(String estado) {
        this.state=estado;
        System.out.println(this.tipo+"-->"+estado);
    }
    
    private void enviaSweep() throws InterruptedException {
        if (!sweepFinished) {
            InputStream inputStream = getClass().getResourceAsStream("/awsensors/emulator/resources/C1Mag.txt");
            Scanner s;
            s = new Scanner(inputStream);
            while (s.hasNextLine()&&portActive) {
                    String linea = s.nextLine();
                    escribeAWSuite(linea);
                    runPaquetesEnviados(linea);
                    Thread.sleep(100);
            }
            s.close();
            sweepFinished=true;
            runAlertas("SweepEnd");
        }
    }
    private void enviaSweepAutoSet() throws InterruptedException {
        if (!sweepFinished) {
            InputStream inputStream = getClass().getResourceAsStream("/awsensors/emulator/resources/C1Mag.txt");
            Scanner s;
            s = new Scanner(inputStream);
            while (s.hasNextLine()&&portActive) {
                    String linea = s.nextLine();
                    escribeAWSuite(linea);
                    runPaquetesEnviados(linea);
                    Thread.sleep(100);
            }
            s.close();
            sweepFinished=true;
            runAlertas("SweepEndAutoset");
        }
    }
    
    private void enviaExperimento() throws InterruptedException, FileNotFoundException {
            location = getClass().getProtectionDomain().getCodeSource().getLocation();
            String customFile = location.toString().substring(6, location.toString().length()-21)+"experiment.txt";
            System.out.println(customFile);
            File f = new File(customFile);
            InputStream inputStream;
            if(f.exists() && !f.isDirectory()) { 
                inputStream = new FileInputStream(f);
                System.out.println("Custom Experiment found");
            } else {
                inputStream = getClass().getResourceAsStream("/awsensors/emulator/resources/experiment.txt");
                System.out.println("Embebbed Experiment");
            }
            Scanner s;
            s = new Scanner(inputStream);
            while (s.hasNextLine()&&portActive) {
                    String linea = s.nextLine();
                    escribeAWSuite(timer.now()+" "+linea);
                    runPaquetesEnviados(timer.now()+" "+linea);
                    Thread.sleep(100);
            }
            s.close();
            runAlertas("SweepEndAutoset");
    }
    
    private void mantieneConexion() throws InterruptedException {
        while (state.equals("ACTIVE")&&portActive) {
            String linea = timer.now()+" +2.495577e+01 +2.799409e+01 99072000  -1 13170263 5 8912645 1 6 11702 3 99072000  0 8518125 0 3733215 1 2 0 6 99072000  0 0 0 0 1 2 0 6 99072000  0 0 0 0 1 2 0 6";
            escribeAWSuite(linea);
            runPaquetesEnviados(linea);
            Thread.sleep(2000);
        }
    }
    
    
    public void runAlertas(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);			 
                            listener.AlertaHilo(texto);
		}
	}
}
