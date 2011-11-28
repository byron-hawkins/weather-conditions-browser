package org.hawkinssoftware.ui.util.weather.view;

import java.awt.Color;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.layout.Axis;
import org.hawkinssoftware.azia.core.layout.BoundedEntity.Expansion;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.paint.PainterRegistry;
import org.hawkinssoftware.azia.ui.paint.basic.PlainRegionPainter;
import org.hawkinssoftware.azia.ui.paint.plugin.BorderPlugin;
import org.hawkinssoftware.azia.ui.tile.TopTile;
import org.hawkinssoftware.azia.ui.tile.UnitTile.Layout;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

public class WeatherViewerAssembler extends UserInterfaceTask
{
	public DesktopWindow<WeatherViewerComponents.LayoutKey> window;

	@Override
	protected boolean execute()
	{
		try
		{
			@SuppressWarnings("unchecked")
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey> layoutTransaction = getTransaction(ModifyLayoutTransaction.class);
			window = layoutTransaction.createWindow(WeatherViewerComponents.LayoutKey.WINDOW, DesktopWindow.FrameType.CLOSE_BUTTON, "Scrap Menagerie");
			PlainRegionPainter<TopTile<WeatherViewerComponents.LayoutKey>> topTilePainter = (PlainRegionPainter<TopTile<LayoutKey>>) PainterRegistry
					.getInstance().getPainter(window.getTopTile());
			topTilePainter.borderPlugins.insertPlugin(new BorderPlugin.Solid<TopTile<WeatherViewerComponents.LayoutKey>>(Color.black));

			GenericTransaction adHocTransaction = getTransaction(GenericTransaction.class);

			LabelComposite<Label, ?> titleLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.TITLE_LABEL_ASSEMBLY,
					window);
			ChangeTextDirective setLabelText = new ChangeTextDirective(titleLabelComponent.getComponent(), "Weather Viewer");
			adHocTransaction.addAction(setLabelText);

			ScrollPaneComposite<?> stationListComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.STATION_LIST_ASSEMBLY,
					window);

			LabelComposite<Label, ?> dataLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.DATA_LABEL_ASSEMBLY,
					window);
			setLabelText = new ChangeTextDirective(dataLabelComponent.getComponent(), "Station Data");
			adHocTransaction.addAction(setLabelText);

			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle mainPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.MAIN_PANEL, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle titlePanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.TITLE_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle dataPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.DATA_PANEL, Axis.H);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle stationListPanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_LIST_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle stationDataPanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_DATA_PANEL);

			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle titleLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.TITLE);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle stationList = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_LIST);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle stationDataLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_DATA);

			layoutTransaction.getTopHandle().setUnit(mainPanel);
			mainPanel.setFirstTile(titlePanel);
			mainPanel.setSecondTile(dataPanel);
			mainPanel.setCrossExpansionPolicy(Expansion.FILL);

			dataPanel.setFirstTile(stationListPanel);
			dataPanel.setSecondTile(stationDataPanel);
			dataPanel.setCrossExpansionPolicy(Expansion.FILL);

			titlePanel.setUnit(titleLabel);
			titlePanel.setPadding(4, 4, 4, 4);
			titlePanel.setLayoutPolicy(Axis.H, Layout.CENTER);
			titlePanel.setLayoutPolicy(Axis.V, Layout.FIT);

			stationListPanel.setUnit(stationList);
			stationListPanel.setPadding(4, 4, 4, 4);
			stationListPanel.setLayoutPolicy(Axis.H, Layout.FILL);
			stationListPanel.setLayoutPolicy(Axis.V, Layout.FILL);

			stationDataPanel.setUnit(stationDataLabel);
			stationDataPanel.setPadding(4, 4, 4, 4);
			stationDataPanel.setLayoutPolicy(Axis.H, Layout.FILL);
			stationDataPanel.setLayoutPolicy(Axis.V, Layout.FILL);

			titleLabel.setComponent(titleLabelComponent);
			stationList.setComponent(stationListComponent);
			stationDataLabel.setComponent(dataLabelComponent);

			layoutTransaction.assemble();

			return true;
		}
		catch (Throwable t)
		{
			Log.out(Tag.CRITICAL, t, "Failed to assemble the Weather Viewer app.");
			throw new RuntimeException(t);
		}
	}
}