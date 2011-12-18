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
package org.hawkinssoftware.ui.util.weather.view.regions;

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
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationRegionDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LabelAssembly;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

/**
 * @JTourBusStop 4, WeatherStationRegion list model isolation, WeatherStationRegionView is private to the
 *               StationRegionDomain:
 * @JTourBusStop 4, WeatherStationRegion list selection isolation, WeatherStationRegionView is private to the
 *               StationRegionDomain:
 * 
 *               ...the view itself cannot be seen outside the StationRegionDomain, per the @VisibilityConstraint. A
 *               tangential requirement of this isolation strategy is that all StationRegionDomain members must restrict
 *               access to assembly instances acquired from this view.
 */
@VisibilityConstraint(domains = StationRegionDomain.class)
@DomainRole.Join(membership = { StationRegionDomain.class, TileLayoutDomain.class, TransactionParticipant.class })
public class WeatherStationRegionView
{
	/**
	 * @JTourBusStop 1, WeatherStationRegion list model usage, WeatherStationRegionView.ListAssembly - scrollable region
	 *               list descriptor:
	 * @JTourBusStop 1, WeatherStationRegion list selection response, WeatherStationRegionView.ListAssembly - scrollable
	 *               region list descriptor:
	 * 
	 *               This descriptor is used by the ComponentRegistry to construct the scroll pane with its viewport,
	 *               list model containing WeatherStationRegions, and its basic behaviors.
	 * 
	 * @JTourBusStop 6.1, Usage of @DefinesIdentity in Azia, Isolation of operations - ScrollSlider.Assembly referenced
	 *               within a concrete scroll pane assembly:
	 * 
	 *               This ListAssembly contains a pair of the ScrollSlider.Assembly from tour stop #6. The enclosing
	 *               class WeatherStationRegionView would like to use this ListAssembly without wondering if it may
	 *               start up its own transactions internally.
	 */
	@DomainRole.Join(membership = { WeatherViewerAssemblyDomain.class, ListDataModel.ModelListDomain.class })
	private static class ListAssembly extends CellViewportComposite.ScrollPaneAssembly
	{
		/**
		 * @JTourBusStop 1, WeatherStationRegion list model isolation, CompositionRegistry access limited to
		 *               CompositionElements constructed during assembly( ):
		 * @JTourBusStop 1, WeatherStationRegion list selection isolation, CompositionRegistry access limited to
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
			scrollPane.getViewport().installService(new WeatherStationRegionStamp.Factory());
			scrollPane.getViewport().installService(new CellViewportSelectionHandler());
			scrollPane.getViewport().installService(new CellViewportSelectionKeyHandler());
			scrollPane.getViewport().installHandler(new CellViewportComposite.UpdateHandler<LayoutKey>(LayoutKey.STATION_REGION_LIST_PANEL));
			CellViewportFocusHandler.install(scrollPane.getViewport());
		}
	}

	private final LabelAssembly labelAssembly = new WeatherViewerComponents.LabelAssembly(LayoutKey.STATION_REGION_LABEL_PANEL);
	/**
	 * @JTourBusStop 2, WeatherStationRegion list model isolation, WeatherStationRegionView.listAssembly - scrollable
	 *               region list descriptor is private:
	 * @JTourBusStop 2, WeatherStationRegion list selection isolation, WeatherStationRegionView.listAssembly -
	 *               scrollable region list descriptor is private:
	 * 
	 *               The scrollable region list can be obtained from the ComponentRegistry using this field as a key.
	 *               Its access modifier 'private' makes it unavailable for direct access by other objects.
	 */
	private final ListAssembly listAssembly = new ListAssembly();

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public ModifyLayoutTransaction<LayoutKey>.PairHandle assembleView(ModifyLayoutTransaction<LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<LayoutKey> window)
	{
		ModifyLayoutTransaction<LayoutKey>.PairHandle regionListFrame = transaction.createPairTile(LayoutKey.STATION_REGION_LIST_FRAME, Axis.V);

		ModifyLayoutTransaction<LayoutKey>.UnitHandle regionListLabel = transaction.createUnitTile(LayoutKey.STATION_REGION_LABEL_PANEL);
		ModifyLayoutTransaction<LayoutKey>.UnitHandle regionListPanel = transaction.createUnitTile(LayoutKey.STATION_REGION_LIST_PANEL);
		ModifyLayoutTransaction<LayoutKey>.ComponentHandle regionLabel = transaction.createComponentTile(LayoutKey.STATION_REGION_LABEL);
		ModifyLayoutTransaction<LayoutKey>.ComponentHandle regionList = transaction.createComponentTile(LayoutKey.STATION_REGION_LIST);

		LabelComposite<Label, ?> regionLabelComponent = ComponentRegistry.getInstance().establishComposite(labelAssembly, window);
		// WIP: these backgrounds are kind of a hack
		((LabelPainter<Label>) regionLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
		ChangeTextDirective setLabelText = new ChangeTextDirective(regionLabelComponent.getComponent(), "Regions");
		adHocTransaction.addAction(setLabelText);

		/**
		 * @JTourBusStop 6.2, Usage of @DefinesIdentity in Azia, Isolation of operations - ScrollSlider.Assembly
		 *               constructed within a scroll pane:
		 * 
		 *               In this assembleView() method, the WeatherStationRegionView believes it is constructing all of
		 *               these components within the ModifyLayoutTransaction, which arrived as a formal parameter
		 *               (above). If the transaction fails, there is an implicit expectation that all of these component
		 *               constructions will be rolled back. But suppose the assembly descriptors for the scrollbar track
		 *               and knob could create their own transactions internally; in that case, their construction might
		 *               commit while all other constructions in this method rolled back. The WeatherStationRegionView
		 *               doesn't want to deal with that possibility--it wants the ListAssembly (tour stop #6.1) to be an
		 *               assembly and *only* an assembly. The @DefinesIdentity annotation guarantees exactly that
		 *               isolation characteristic for all subclasses of ComponentAssembly.
		 */
		ScrollPaneComposite<CellViewportComposite<?>> regionListComponent = ComponentRegistry.getInstance().establishComposite(listAssembly, window);

		regionListLabel.setUnit(regionLabel);
		regionListLabel.setPadding(4, 4, 4, 4);
		regionListLabel.setLayoutPolicy(Axis.H, Layout.CENTER);
		regionListLabel.setLayoutPolicy(Axis.V, Layout.FIT);

		regionListPanel.setUnit(regionList);
		regionListPanel.setPadding(4, 4, 4, 4);
		regionListPanel.setLayoutPolicy(Axis.H, Layout.FILL);
		regionListPanel.setLayoutPolicy(Axis.V, Layout.FILL);

		regionListFrame.setFirstTile(regionListLabel);
		regionListFrame.setSecondTile(regionListPanel);
		regionListFrame.setCrossExpansionPolicy(Expansion.FILL);

		regionLabel.setComponent(regionLabelComponent);
		regionList.setComponent(regionListComponent);

		return regionListFrame;
	}

	/**
	 * @JTourBusStop 3, WeatherStationRegion list model isolation, WeatherStationRegionView.listAssembly accessor might
	 *               make it available outside the StationRegionDomain, except that...:
	 * @JTourBusStop 3, WeatherStationRegion list selection isolation, WeatherStationRegionView.listAssembly accessor
	 *               might make it available outside the StationRegionDomain, except that...:
	 * 
	 *               The scrollable region list can be obtained from the ComponentRegistry using this field as a key.
	 *               This accessor might make it available outside the StationRegionDomain, except...
	 */
	public ListAssembly getListAssembly()
	{
		return listAssembly;
	}
}
