package it.ing.unipi.anaws.devices;

import java.util.Comparator;

/**
 * This class is needed for ordering the devices by battery charge
 */
public class DeviceComparatorByCharge implements Comparator<Device> {

	@Override
	public int compare(Device d1, Device d2){
		return d2.getBattery().getCharge() - d1.getBattery().getCharge();
	}
}
