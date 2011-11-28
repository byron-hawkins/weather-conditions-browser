package org.hawkinssoftware.ui.util.weather.view.stations;

import org.hawkinssoftware.azia.ui.component.scalar.ScrollPane;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.paint.basic.cell.ListModelPainter;

public class WeatherStationList extends ScrollPaneComposite<WeatherStationListViewport>
{
	public static class ScrollPaneAssembly extends ScrollPane.Assembly<WeatherStationListViewport, WeatherStationList>
	{
		public ScrollPaneAssembly()
		{
			super(WeatherStationList.class, new WeatherStationListViewport.Assembly());
		}
	}

	private final ListDataModel model = new ListDataModel();
	private final WeatherStationStampFactory stampFactory = new WeatherStationStampFactory();

	public WeatherStationList(ScrollPane component)
	{
		super(component);
	}

	public ListDataModel getModel()
	{
		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ServiceType> ServiceType getService(Class<ServiceType> serviceType)
	{
		if (serviceType.isAssignableFrom(ListDataModel.class))
		{
			return (ServiceType) model;
		}
		if (serviceType.isAssignableFrom(WeatherStationListViewport.class))
		{
			return (ServiceType) getViewport();
		}
		if (serviceType.isAssignableFrom(WeatherStationStampFactory.class))
		{
			return (ServiceType) stampFactory;
		}
		if (serviceType.isAssignableFrom(ListModelPainter.class))
		{
			return (ServiceType) getViewport().getCellPainter();
		}
		// if (serviceType.isAssignableFrom(ScrapMenagerieListSelection.class))
		// {
		// return (ServiceType) getViewport().selection;
		// }
		return super.getService(serviceType);
	}
}
