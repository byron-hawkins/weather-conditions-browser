package org.hawkinssoftware.ui.util.weather.view;

import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportFocusHandler;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportSelectionHandler;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportSelectionKeyHandler;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.tile.LayoutEntity;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationStamp;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationRegionStamp;

public class WeatherViewerComponents
{
	public enum LayoutKey implements LayoutEntity.Key<LayoutKey>
	{
		WINDOW,
		MAIN_PANEL, // pair:V
		TITLE_PANEL, // unit
		DATA_PANEL, // pair:H
		STATION_NAVIGATION_PANEL, // pair:V
		STATION_REGION_LIST_PANEL, // unit
		STATION_LIST_PANEL, // unit
		STATION_DATA_PANEL, // unit
		TITLE,
		STATION_REGION_LIST,
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

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
	private static class WeatherStationRegionListAssembly extends CellViewportComposite.ScrollPaneAssembly
	{
		@Override
		public void assemble(ScrollPaneComposite<CellViewportComposite<?>> scrollPane)
		{
			super.assemble(scrollPane);

			scrollPane.getViewport().installService(new ListDataModel());
			scrollPane.getViewport().installService(new WeatherStationRegionStamp.Factory());
			scrollPane.getViewport().installService(new CellViewportSelectionHandler());
			scrollPane.getViewport().installService(new CellViewportSelectionKeyHandler());
			scrollPane.getViewport().installHandler(new CellViewportComposite.UpdateHandler<LayoutKey>(LayoutKey.STATION_REGION_LIST_PANEL));
			CellViewportFocusHandler.install(scrollPane.getViewport());
		}
	}

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
	private static class WeatherStationListAssembly extends CellViewportComposite.ScrollPaneAssembly
	{
		@Override
		public void assemble(ScrollPaneComposite<CellViewportComposite<?>> scrollPane)
		{
			super.assemble(scrollPane);

			scrollPane.getViewport().installService(new ListDataModel());
			scrollPane.getViewport().installService(new WeatherStationStamp.Factory());
			scrollPane.getViewport().installService(new CellViewportSelectionHandler());
			scrollPane.getViewport().installService(new CellViewportSelectionKeyHandler());
			scrollPane.getViewport().installHandler(new CellViewportComposite.UpdateHandler<LayoutKey>(LayoutKey.STATION_LIST_PANEL));
			CellViewportFocusHandler.install(scrollPane.getViewport());
		}
	}

	private static class WeatherConditionsPanelAssembly extends TextViewportComposite.ScrollPaneAssembly
	{
		@Override
		public void assemble(ScrollPaneComposite<TextViewportComposite> scrollPane)
		{
			super.assemble(scrollPane);
		}
	}

	public static final TitleLabelAssembly TITLE_LABEL_ASSEMBLY = new TitleLabelAssembly();
	public static final TitleLabelAssembly LIST_LABEL_ASSEMBLY = new TitleLabelAssembly();
	public static final TitleLabelAssembly DATA_LABEL_ASSEMBLY = new TitleLabelAssembly();
	public static final CellViewportComposite.ScrollPaneAssembly STATION_REGION_LIST_ASSEMBLY = new WeatherStationRegionListAssembly();
	public static final CellViewportComposite.ScrollPaneAssembly STATION_LIST_ASSEMBLY = new WeatherStationListAssembly();
	public static final TextViewportComposite.ScrollPaneAssembly STATION_DATA_ASSEMBLY = new WeatherConditionsPanelAssembly();

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
