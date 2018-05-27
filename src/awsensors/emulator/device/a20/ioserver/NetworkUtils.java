/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package awsensors.emulator.device.a20.ioserver;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author farnauvi
 */
public class NetworkUtils {  
  InetAddress ip;
  String macSt;
  String ipStr;
  String maskStr;
  
    public String config () {
	try {
		ip = InetAddress.getLocalHost();
		System.out.println("Current IP address : " + ip.getHostAddress());
		ipStr = ip.getHostAddress();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			
		byte[] mac = network.getHardwareAddress();
			
		System.out.print("Current MAC address : ");
			
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
		}
                macSt =sb.toString();
		System.out.println(sb.toString());
                Short maskStrLength = network.getInterfaceAddresses().get(0).getNetworkPrefixLength();
		maskStr = mask ((int)maskStrLength);	
	} catch (UnknownHostException e) {
		e.printStackTrace();	
	} catch (SocketException e){	
		e.printStackTrace();		
	}
        return macSt+" "+ipStr+" "+maskStr+" 0.0.0.0";
    }
    
    public String mask (int cidrMask) {   
        long bits = 0;
        bits = 0xffffffff ^ (1 << 32 - cidrMask) - 1;
        String mask = String.format("%d.%d.%d.%d", (bits & 0x0000000000ff000000L) >> 24, (bits & 0x0000000000ff0000) >> 16, (bits & 0x0000000000ff00) >> 8, bits & 0xff);
        return mask;
    }
    
}
