package org.hawkinssoftware.ui.util.weather.view.stations;

import org.hawkinssoftware.azia.ui.component.cell.CellViewport;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.paint.basic.cell.ListModelCellViewport;
import org.hawkinssoftware.azia.ui.paint.basic.cell.ListModelPainter;

public class WeatherStationListViewport extends CellViewportComposite<ListModelPainter> implements ListDataModel.ComponentContext
{
	public static class Assembly extends CellViewport.Assembly<ListModelPainter, WeatherStationListViewport>
	{
		public Assembly()
		{
			super(ListModelCellViewport.class, WeatherStationListViewport.class);
		}
		
		@Override
		protected ListModelPainter createCellPainter()
		{
			return new ListModelPainter();
		}
	}

	public WeatherStationListViewport(CellViewport component)
	{
		super(component);
	}
}
