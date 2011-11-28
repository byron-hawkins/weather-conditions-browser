package org.hawkinssoftware.ui.util.weather.view;

import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.tile.LayoutEntity;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationStamp;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationStateStamp;

public class WeatherViewerComponents
{
	public enum LayoutKey implements LayoutEntity.Key<LayoutKey>
	{
		WINDOW,
		MAIN_PANEL, // pair:V
		TITLE_PANEL, // unit
		DATA_PANEL, // pair:H
		STATION_NAVIGATION_PANEL, // pair:V
		STATION_STATE_LIST_PANEL, // unit
		STATION_LIST_PANEL, // unit
		STATION_DATA_PANEL, // unit
		TITLE,
		STATION_STATE_LIST,
		STATION_LIST,
		STATION_DATA;

		@Override
		public String getName()
		{
			return name();
		}
	}

	private static class TitleLabelAssembly extends Label.Assembly
	{
		@Override
		public void assemble(LabelComposite<Label, ?> enclosure)
		{
			super.assemble(enclosure);
		}
	}

	private static class WeatherStationStateListAssembly extends ScrollPaneComposite.Assembly
	{
		@Override
		public void assemble(ScrollPaneComposite<CellViewportComposite<?>> scrollPane)
		{
			super.assemble(scrollPane);

			scrollPane.getViewport().installService(new ListDataModel());
			scrollPane.getViewport().installService(new WeatherStationStateStamp.Factory());
		}
	}

	private static class WeatherStationListAssembly extends ScrollPaneComposite.Assembly
	{
		@Override
		public void assemble(ScrollPaneComposite<CellViewportComposite<?>> scrollPane)
		{
			super.assemble(scrollPane);

			scrollPane.getViewport().installService(new ListDataModel());
			scrollPane.getViewport().installService(new WeatherStationStamp.Factory());
		}
	}

	public static final TitleLabelAssembly TITLE_LABEL_ASSEMBLY = new TitleLabelAssembly();
	public static final TitleLabelAssembly LIST_LABEL_ASSEMBLY = new TitleLabelAssembly();
	public static final TitleLabelAssembly DATA_LABEL_ASSEMBLY = new TitleLabelAssembly();
	public static final ScrollPaneComposite.Assembly STATION_STATE_LIST_ASSEMBLY = new WeatherStationStateListAssembly();
	public static final ScrollPaneComposite.Assembly STATION_LIST_ASSEMBLY = new WeatherStationListAssembly();

	private static final WeatherViewerComponents INSTANCE = new WeatherViewerComponents();

	public static WeatherViewerComponents getInstance()
	{
		return INSTANCE;
	}

	private WeatherViewerDialog dialog;

	public WeatherViewerDialog getDialog()
	{
		return dialog;
	}

	public void setDialog(WeatherViewerDialog dialog)
	{
		this.dialog = dialog;
	}
}
