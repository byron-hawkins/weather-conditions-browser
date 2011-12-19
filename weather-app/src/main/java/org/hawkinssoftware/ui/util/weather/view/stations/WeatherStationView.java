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
package org.hawkinssoftware.ui.util.weather.view.stations;

import java.awt.Color;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransactionDomains.TransactionParticipant;
import org.hawkinssoftware.azia.core.layout.Axis;
import org.hawkinssoftware.azia.core.layout.BoundedEntity.Expansion;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportFocusHandler;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportSelectionHandler;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportSelectionKeyHandler;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.paint.basic.text.LabelPainter;
import org.hawkinssoftware.azia.ui.paint.plugin.BackgroundPlugin;
import org.hawkinssoftware.azia.ui.tile.LayoutRegion.TileLayoutDomain;
import org.hawkinssoftware.azia.ui.tile.UnitTile.Layout;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LabelAssembly;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

/**
 * @JTourBusStop 4, WeatherStation list model isolation, WeatherStationView is private to the StationDomain:
 * @JTourBusStop 4, WeatherStation list selection isolation, WeatherStationView is private to the StationDomain:
 * 
 *               ...the view itself cannot be seen outside the StationDomain, per the @VisibilityConstraint. A
 *               tangential requirement of this isolation strategy is that all StationDomain members must restrict
 *               access to assembly instances acquired from this view.
 * 
 * @JTourBusStop 4, StationRegionDomain-StationDomain isolation, WeatherStationView isolated within the StationDomain:
 * 
 *               The functionality for displaying stations in the scrollable list is contained within the StationDomain
 *               by this @VisibilityConstraint.
 * 
 * @JTourBusStop 5, Defining the TileLayoutDomain and its scope, WeatherStationView joins the TileLayoutDomain:
 * 
 *               Client classes may join domains defined in libraries. The TileLayoutDomain and DisplayBoundsDomain are
 *               defined in the Azia library, and this Weather Viewer application class is joining both.
 */
@VisibilityConstraint(domains = StationDomain.class)
@DomainRole.Join(membership = { StationDomain.class, TileLayoutDomain.class, TransactionParticipant.class })
public class WeatherStationView
{
	/**
	 * @JTourBusStop 1, WeatherStation list model usage, WeatherStationView.ListAssembly - scrollable station list
	 *               descriptor:
	 * @JTourBusStop 1, WeatherStation list selection response, WeatherStationView.ListAssembly - scrollable station
	 *               list descriptor:
	 * 
	 *               This descriptor is used by the ComponentRegistry to construct the scroll pane with its viewport,
	 *               its list model containing WeatherStations, and its basic behaviors.
	 * 
	 * @JTourBusStop 7, Application assembly, WeatherViewerAssemblyDomain owns the assembly descriptor for the
	 *               scrollable station list:
	 * 
	 *               Assembly descriptors always belong in the application's assembly domain, which is permitted to be
	 *               defined within a class that is not a member of the assembly domain.
	 */
	@DomainRole.Join(membership = { WeatherViewerAssemblyDomain.class, ListDataModel.ModelListDomain.class })
	private static class ListAssembly extends CellViewportComposite.ScrollPaneAssembly
	{
		/**
		 * @JTourBusStop 1, WeatherStation list model isolation, CompositionRegistry access limited to
		 *               CompositionElements constructed during assembly( ):
		 * @JTourBusStop 1, WeatherStation list selection isolation, CompositionRegistry access limited to
		 *               CompositionElements constructed during assembly( ):
		 * 
		 *               All objects instantiated during execution of this method are granted access to the services of
		 *               the composite, one of which is the ListDataModel. No reference to these auxiliary instances are
		 *               held outside the assembled composite, so they can only be accessed via reference to the
		 *               composite itself.
		 */
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

	private final LabelAssembly labelAssembly = new WeatherViewerComponents.LabelAssembly(LayoutKey.STATION_LIST_LABEL_PANEL);
	/**
	 * @JTourBusStop 2, WeatherStation list model isolation, WeatherStationView.listAssembly - scrollable station list
	 *               descriptor is private:
	 * @JTourBusStop 2, WeatherStation list selection isolation, WeatherStationView.listAssembly - scrollable station
	 *               list descriptor is private:
	 * 
	 *               The scrollable station list can be obtained from the ComponentRegistry using this field as a key.
	 *               Its access modifier 'private' makes it unavailable for direct access by other objects.
	 */
	private final ListAssembly listAssembly = new ListAssembly();

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public ModifyLayoutTransaction<LayoutKey>.PairHandle assembleView(ModifyLayoutTransaction<LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<LayoutKey> window)
	{
		ModifyLayoutTransaction<LayoutKey>.PairHandle stationListFrame = transaction.createPairTile(LayoutKey.STATION_LIST_FRAME, Axis.V);

		ModifyLayoutTransaction<LayoutKey>.UnitHandle stationListLabel = transaction.createUnitTile(LayoutKey.STATION_LIST_LABEL_PANEL);
		ModifyLayoutTransaction<LayoutKey>.UnitHandle stationListPanel = transaction.createUnitTile(LayoutKey.STATION_LIST_PANEL);
		ModifyLayoutTransaction<LayoutKey>.ComponentHandle stationLabel = transaction.createComponentTile(LayoutKey.STATION_LABEL);
		ModifyLayoutTransaction<LayoutKey>.ComponentHandle stationList = transaction.createComponentTile(LayoutKey.STATION_LIST);

		LabelComposite<Label, ?> stationLabelComponent = ComponentRegistry.getInstance().establishComposite(labelAssembly, window);
		((LabelPainter<Label>) stationLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
		ChangeTextDirective setLabelText = new ChangeTextDirective(stationLabelComponent.getComponent(), "Stations");
		adHocTransaction.addAction(setLabelText);

		ScrollPaneComposite<CellViewportComposite<?>> stationListComponent = ComponentRegistry.getInstance().establishComposite(listAssembly, window);

		stationListLabel.setUnit(stationLabel);
		stationListLabel.setPadding(4, 4, 4, 4);
		stationListLabel.setLayoutPolicy(Axis.H, Layout.FILL);
		stationListLabel.setLayoutPolicy(Axis.V, Layout.FIT);

		stationListPanel.setUnit(stationList);
		stationListPanel.setPadding(4, 4, 4, 4);
		stationListPanel.setLayoutPolicy(Axis.H, Layout.FILL);
		stationListPanel.setLayoutPolicy(Axis.V, Layout.FILL);

		stationListFrame.setFirstTile(stationListLabel);
		stationListFrame.setSecondTile(stationListPanel);
		stationListFrame.setCrossExpansionPolicy(Expansion.FILL);

		stationLabel.setComponent(stationLabelComponent);
		stationList.setComponent(stationListComponent);

		return stationListFrame;
	}

	public LabelAssembly getLabelAssembly()
	{
		return labelAssembly;
	}

	/**
	 * @JTourBusStop 3, WeatherStation list model isolation, WeatherStationView.listAssembly accessor might make it
	 *               available outside the StationDomain, except that...:
	 * @JTourBusStop 3, WeatherStation list selection isolation, WeatherStationView.listAssembly accessor might make it
	 *               available outside the StationDomain, except that...:
	 * 
	 *               The scrollable station list can be obtained from the ComponentRegistry using this field as a key.
	 *               This accessor might make it available outside the StationDomain, except...
	 */
	public ListAssembly getListAssembly()
	{
		return listAssembly;
	}
}
