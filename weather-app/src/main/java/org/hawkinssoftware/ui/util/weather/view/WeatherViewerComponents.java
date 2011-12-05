package org.hawkinssoftware.ui.util.weather.view;

import java.awt.Color;

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
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationRegionDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationRegionStamp;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationStamp;

public class WeatherViewerComponents
{
	public enum LayoutKey implements LayoutEntity.Key<LayoutKey>
	{
		WINDOW,
		MAIN_PANEL, // pair:V
		TITLE_PANEL, // unit
		DATA_PANEL, // pair:H
		STATION_NAVIGATION_PANEL, // pair:V
		STATION_REGION_LIST_FRAME, // pair:V
		STATION_REGION_LABEL_PANEL, // unit
		STATION_REGION_LIST_PANEL, // unit
		STATION_LIST_FRAME, // pair:V
		STATION_LIST_LABEL_PANEL, // unit
		STATION_LIST_PANEL, // unit
		STATION_DATA_FRAME, // pair:V
		STATION_DATA_LABEL_PANEL, // unit
		STATION_DATA_PANEL, // unit
		TITLE,
		STATION_REGION_LABEL,
		STATION_REGION_LIST,
		STATION_LABEL,
		STATION_LIST,
		DATA_LABEL,
		STATION_DATA;

		@Override
		public String getName()
		{
			return name();
		}
	}

	private static class LabelAssembly extends Label.Assembly
	{
		private final LayoutKey key;

		LabelAssembly(LayoutKey key)
		{
			this.key = key;
		}

		@Override
		public void assemble(LabelComposite<Label, ?> label)
		{
			super.assemble(label);

			label.installHandler(new LabelComposite.UpdateHandler<LayoutKey>(key));
		}
	}

	@DomainRole.Join(membership = { WeatherViewerAssemblyDomain.class, ListDataModel.ModelListDomain.class })
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

	@DomainRole.Join(membership = { WeatherViewerAssemblyDomain.class, ListDataModel.ModelListDomain.class })
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

	private static final LabelAssembly TITLE_LABEL_ASSEMBLY = new LabelAssembly(LayoutKey.TITLE_PANEL);
	private static final LabelAssembly REGION_LABEL_ASSEMBLY = new LabelAssembly(LayoutKey.STATION_REGION_LABEL_PANEL);
	private static final LabelAssembly STATION_LABEL_ASSEMBLY = new LabelAssembly(LayoutKey.STATION_LIST_LABEL_PANEL);
	private static final LabelAssembly DATA_LABEL_ASSEMBLY = new LabelAssembly(LayoutKey.STATION_DATA_LABEL_PANEL);
	private static final CellViewportComposite.ScrollPaneAssembly STATION_REGION_LIST_ASSEMBLY = new WeatherStationRegionListAssembly();
	private static final CellViewportComposite.ScrollPaneAssembly STATION_LIST_ASSEMBLY = new WeatherStationListAssembly();
	private static final TextViewportComposite.ScrollPaneAssembly STATION_DATA_ASSEMBLY = new WeatherConditionsPanelAssembly();

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static LabelAssembly getTitleLabel()
	{
		return TITLE_LABEL_ASSEMBLY;
	}

	@InvocationConstraint(domains = { StationRegionDomain.class, WeatherViewerAssemblyDomain.class })
	public static LabelAssembly getRegionLabel()
	{
		return REGION_LABEL_ASSEMBLY;
	}

	@InvocationConstraint(domains = { StationDomain.class, WeatherViewerAssemblyDomain.class })
	public static LabelAssembly getStationLabel()
	{
		return STATION_LABEL_ASSEMBLY;
	}

	@InvocationConstraint(domains = { StationConditionsDomain.class, WeatherViewerAssemblyDomain.class })
	public static LabelAssembly getConditionsLabel()
	{
		return DATA_LABEL_ASSEMBLY;
	}

	@InvocationConstraint(domains = { StationRegionDomain.class, WeatherViewerAssemblyDomain.class })
	public static CellViewportComposite.ScrollPaneAssembly getStationRegionList()
	{
		return STATION_REGION_LIST_ASSEMBLY;
	}

	@InvocationConstraint(domains = { StationDomain.class, WeatherViewerAssemblyDomain.class })
	public static CellViewportComposite.ScrollPaneAssembly getStationList()
	{
		return STATION_LIST_ASSEMBLY;
	}

	@InvocationConstraint(domains = { StationConditionsDomain.class, WeatherViewerAssemblyDomain.class })
	public static TextViewportComposite.ScrollPaneAssembly getConditionsPanel()
	{
		return STATION_DATA_ASSEMBLY;
	}

	public static final Color SELECTION_BACKGROUND = new Color(0xEEFFDD);

	private static final WeatherViewerComponents INSTANCE = new WeatherViewerComponents();

	public static WeatherViewerComponents getInstance()
	{
		return INSTANCE;
	}

	private WeatherViewerDialog dialog;

	@InvocationConstraint(domains = InitializationDomain.class)
	public WeatherViewerDialog getDialog()
	{
		return dialog;
	}

	@InvocationConstraint(domains = InitializationDomain.class)
	public void setDialog(WeatherViewerDialog dialog)
	{
		this.dialog = dialog;
	}
}
