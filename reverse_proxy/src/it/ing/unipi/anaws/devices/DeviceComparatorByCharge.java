package it.ing.unipi.anaws.devices;

import java.util.Comparator;

public class DeviceComparatorByCharge implements Comparator<Device> {

	@Override
	public int compare(Device d1, Device d2){
		return d2.getBattery().charge - d1.getBattery().charge;
	}
}
