/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awsensors.emulator.device.a20.ioserver;

/**
 *
 * @author farnauvi
 */
public class Timer {
    public long inicio;
    
    public void inicio () {
        inicio = System.currentTimeMillis();
    }
    
    public String now () {
        long ahora = System.currentTimeMillis();
        long militranscurridos = ahora-inicio;
        int ms = (int) militranscurridos;
        int mili = ms%1000; ms -= mili; ms /= 1000;
        int segs = ms%60; ms -= segs; ms /= 60;
        int mins = ms%60; ms -= mins; ms /= 60;
        int horas = ms;
        return String.valueOf(horas)+":"+String.valueOf(mins)+":"+String.valueOf(segs)+":"+String.valueOf(mili);
    }
    
      
}
