package br.ufba.dcc.wiser.soft_iot.local_storage;

import java.util.Date;
import java.util.List;

import br.ufba.dcc.wiser.soft_iot.entities.Device;
import br.ufba.dcc.wiser.soft_iot.entities.Sensor;
import br.ufba.dcc.wiser.soft_iot.entities.SensorData;

public interface LocalDataController {
	
	SensorData getLastSensorData(Device device, Sensor sensor);
	
	List<SensorData> getSensorDataByDateTime(Device device, Sensor sensor, Date startDateTime, Date endDateTime);

}
