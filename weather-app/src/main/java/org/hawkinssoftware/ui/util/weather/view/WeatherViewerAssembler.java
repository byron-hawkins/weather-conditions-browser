/*
 * Copyright (c) 2011 HawkinsSoftware
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Byron Hawkins of HawkinsSoftware
 */
package org.hawkinssoftware.ui.util.weather.view;

import java.awt.Color;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.layout.Axis;
import org.hawkinssoftware.azia.core.layout.BoundedEntity.Expansion;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
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
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.control.WeatherConditionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationRegionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationsController;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

/**
 * @JTourBusStop 2, Application assembly, WeatherViewerAssembler constructs and configures application components:
 */
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
			window = layoutTransaction.createWindow(WeatherViewerComponents.LayoutKey.WINDOW, DesktopWindow.FrameType.CLOSE_BUTTON,
					"NOAA Weather Stations Viewer");
			PlainRegionPainter<TopTile<WeatherViewerComponents.LayoutKey>> topTilePainter = (PlainRegionPainter<TopTile<LayoutKey>>) PainterRegistry
					.getInstance().getPainter(window.getTopTile());
			topTilePainter.borderPlugins.insertPlugin(new BorderPlugin.Solid<TopTile<WeatherViewerComponents.LayoutKey>>(Color.black));

			WeatherConditionsController.initialize();
			WeatherStationsController.initialize();
			WeatherStationRegionsController.initialize();

			GenericTransaction adHocTransaction = getTransaction(GenericTransaction.class);

			LabelComposite<Label, ?> titleLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.getTitleLabel(), window);
			ChangeTextDirective setLabelText = new ChangeTextDirective(titleLabelComponent.getComponent(), "Weather Viewer");
			adHocTransaction.addAction(setLabelText);

			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle mainPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.MAIN_PANEL, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle titlePanel = layoutTransaction
					.createUnitTile(WeatherViewerComponents.LayoutKey.TITLE_PANEL);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle dataPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.DATA_PANEL, Axis.H);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle stationNavigationPanel = layoutTransaction.createPairTile(
					WeatherViewerComponents.LayoutKey.STATION_NAVIGATION_PANEL, Axis.V);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle regionListFrame = WeatherStationRegionsController.getInstance().assembleView(
					layoutTransaction, adHocTransaction, window);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle stationListFrame = WeatherStationsController.getInstance().assembleView(
					layoutTransaction, adHocTransaction, window);
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle stationDataFrame = WeatherConditionsController.getInstance().assembleView(
					layoutTransaction, adHocTransaction, window);

			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle titleLabel = layoutTransaction
					.createComponentTile(WeatherViewerComponents.LayoutKey.TITLE);

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

			titleLabel.setComponent(titleLabelComponent);

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
