package awsensors.emulator.device.a20.ioserver;

/*
* Created on 22-jun-2009, 9:45:30 by Paco Arnau for Celartia
 */

/**
 *
 * @author Paco Arnau
 */

    public interface ServerListener
{
    public void PaqueteControlEnviado (String texto); //
    public void PaqueteControlRecibido (String texto); //
    public void PaqueteDatosEnviado (String texto); //
    public void PaqueteDatosRecibido (String texto); //
    public void AlertaHilo (String tipo); // Sirve para informar de algún evento producido en el hilo, ejemplo, fin del envío de Sweep
}
