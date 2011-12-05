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
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.paint.PainterRegistry;
import org.hawkinssoftware.azia.ui.paint.basic.PlainRegionPainter;
import org.hawkinssoftware.azia.ui.paint.basic.text.LabelPainter;
import org.hawkinssoftware.azia.ui.paint.plugin.BackgroundPlugin;
import org.hawkinssoftware.azia.ui.paint.plugin.BorderPlugin;
import org.hawkinssoftware.azia.ui.tile.TopTile;
import org.hawkinssoftware.azia.ui.tile.UnitTile.Layout;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

@DomainRole.Join(membership = WeatherViewerAssemblyDomain.class)
public class WeatherViewerAssembler extends UserInterfaceTask
{
	private DesktopWindow<WeatherViewerComponents.LayoutKey> window;

	@InvocationConstraint(domains = InitializationDomain.class)
	public DesktopWindow<WeatherViewerComponents.LayoutKey> getWindow()
	{
		return window;
	}

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

			ScrollPaneComposite<CellViewportComposite<?>> regionListComponent = ComponentRegistry.getInstance().establishComposite(
					WeatherViewerComponents.getStationRegionList(), window);
			ScrollPaneComposite<CellViewportComposite<?>> stationListComponent = ComponentRegistry.getInstance().establishComposite(
					WeatherViewerComponents.getStationList(), window);
			ScrollPaneComposite<TextViewportComposite> stationConditionsComponent = ComponentRegistry.getInstance().establishComposite(
					WeatherViewerComponents.getConditionsPanel(), window);

			LabelComposite<Label, ?> titleLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.getTitleLabel(), window);
			ChangeTextDirective setLabelText = new ChangeTextDirective(titleLabelComponent.getComponent(), "Weather Viewer");
			adHocTransaction.addAction(setLabelText);

			LabelComposite<Label, ?> regionLabelComponent = ComponentRegistry.getInstance()
					.establishComposite(WeatherViewerComponents.getRegionLabel(), window);
			// WIP: these backgrounds are kind of a hack
			((LabelPainter<Label>) regionLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
			setLabelText = new ChangeTextDirective(regionLabelComponent.getComponent(), "Regions");
			adHocTransaction.addAction(setLabelText);

			LabelComposite<Label, ?> stationLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.getStationLabel(),
					window);
			((LabelPainter<Label>) stationLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
			setLabelText = new ChangeTextDirective(stationLabelComponent.getComponent(), "Stations");
			adHocTransaction.addAction(setLabelText);

			LabelComposite<Label, ?> dataLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.getConditionsLabel(),
					window);
			((LabelPainter<Label>) dataLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
			setLabelText = new ChangeTextDirective(dataLabelComponent.getComponent(), "Weather Conditions");
			adHocTransaction.addAction(setLabelText);

			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle mainPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.MAIN_PANEL, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle titlePanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.TITLE_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle dataPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.DATA_PANEL, Axis.H);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle stationNavigationPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.STATION_NAVIGATION_PANEL, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle regionListFrame = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.STATION_REGION_LIST_FRAME, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle regionListLabel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LABEL_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle regionListPanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LIST_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle stationListFrame = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.STATION_LIST_FRAME, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle stationListLabel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_LIST_LABEL_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle stationListPanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_LIST_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle stationDataFrame = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.STATION_DATA_FRAME, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle stationDataLabel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_DATA_LABEL_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle stationDataPanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_DATA_PANEL);

			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle titleLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.TITLE);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle regionLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LABEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle regionList = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LIST);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle stationLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_LABEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle stationList = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_LIST);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle dataLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.DATA_LABEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle stationData = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_DATA);

			layoutTransaction.getTopHandle().setUnit(mainPanel);
			mainPanel.setFirstTile(titlePanel);
			mainPanel.setSecondTile(dataPanel);
			mainPanel.setCrossExpansionPolicy(Expansion.FILL);

			dataPanel.setFirstTile(stationNavigationPanel);
			dataPanel.setSecondTile(stationDataFrame);
			dataPanel.setCrossExpansionPolicy(Expansion.FILL);

			titlePanel.setUnit(titleLabel);
			titlePanel.setPadding(4, 4, 4, 4);
			titlePanel.setLayoutPolicy(Axis.H, Layout.CENTER);
			titlePanel.setLayoutPolicy(Axis.V, Layout.FIT);

			stationNavigationPanel.setFirstTile(regionListFrame);
			stationNavigationPanel.setSecondTile(stationListFrame);
			stationNavigationPanel.setCrossExpansionPolicy(Expansion.FILL);

			regionListFrame.setFirstTile(regionListLabel);
			regionListFrame.setSecondTile(regionListPanel);
			regionListFrame.setCrossExpansionPolicy(Expansion.FILL);

			regionListLabel.setUnit(regionLabel);
			regionListLabel.setPadding(4, 4, 4, 4);
			regionListLabel.setLayoutPolicy(Axis.H, Layout.CENTER);
			regionListLabel.setLayoutPolicy(Axis.V, Layout.FIT);

			regionListPanel.setUnit(regionList);
			regionListPanel.setPadding(4, 4, 4, 4);
			regionListPanel.setLayoutPolicy(Axis.H, Layout.FILL);
			regionListPanel.setLayoutPolicy(Axis.V, Layout.FILL);

			stationListFrame.setFirstTile(stationListLabel);
			stationListFrame.setSecondTile(stationListPanel);
			stationListFrame.setCrossExpansionPolicy(Expansion.FILL);

			stationListLabel.setUnit(stationLabel);
			stationListLabel.setPadding(4, 4, 4, 4);
			stationListLabel.setLayoutPolicy(Axis.H, Layout.FILL);
			stationListLabel.setLayoutPolicy(Axis.V, Layout.FIT);

			stationListPanel.setUnit(stationList);
			stationListPanel.setPadding(4, 4, 4, 4);
			stationListPanel.setLayoutPolicy(Axis.H, Layout.FILL);
			stationListPanel.setLayoutPolicy(Axis.V, Layout.FILL);

			stationDataFrame.setFirstTile(stationDataLabel);
			stationDataFrame.setSecondTile(stationDataPanel);
			stationDataFrame.setCrossExpansionPolicy(Expansion.FILL);

			stationDataLabel.setUnit(dataLabel);
			stationDataLabel.setPadding(4, 4, 4, 4);
			stationDataLabel.setLayoutPolicy(Axis.H, Layout.CENTER);
			stationDataLabel.setLayoutPolicy(Axis.V, Layout.FIT);

			stationDataPanel.setUnit(stationData);
			stationDataPanel.setPadding(4, 4, 4, 4);
			stationDataPanel.setLayoutPolicy(Axis.H, Layout.FILL);
			stationDataPanel.setLayoutPolicy(Axis.V, Layout.FILL);

			titleLabel.setComponent(titleLabelComponent);
			regionLabel.setComponent(regionLabelComponent);
			regionList.setComponent(regionListComponent);
			stationLabel.setComponent(stationLabelComponent);
			stationList.setComponent(stationListComponent);
			dataLabel.setComponent(dataLabelComponent);
			stationData.setComponent(stationConditionsComponent);

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
