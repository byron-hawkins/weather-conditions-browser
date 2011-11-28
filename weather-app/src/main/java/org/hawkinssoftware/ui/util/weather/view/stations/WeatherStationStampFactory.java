package org.hawkinssoftware.ui.util.weather.view.stations;

import org.hawkinssoftware.azia.ui.model.RowAddress;
import org.hawkinssoftware.azia.ui.paint.basic.cell.CellStamp;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;

public class WeatherStationStampFactory implements CellStamp.Factory
{
	private final CellStamp<WeatherStation> itemStamp = new WeatherStationStamp();

	@SuppressWarnings("unchecked")
	public <DataType> CellStamp<DataType> getStamp(RowAddress address, DataType datum)
	{
		return (CellStamp<DataType>) itemStamp;
	}
}
