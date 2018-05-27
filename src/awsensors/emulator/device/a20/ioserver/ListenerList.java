package awsensors.emulator.device.a20.ioserver;

/******************************************************************
*
*	* Created on 22-jun-2009, 9:45:30 by Paco Arnau 
*
******************************************************************/

import java.util.*;

public class ListenerList extends Vector
{
	public boolean add(Object obj)
	{
		if (0 <= indexOf(obj))
			return false;
		return super.add(obj);
	}
}

