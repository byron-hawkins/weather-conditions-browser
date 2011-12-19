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

import org.hawkinssoftware.azia.core.action.UserInterfaceActor;
import org.hawkinssoftware.azia.core.action.UserInterfaceActorDelegate;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
import org.hawkinssoftware.azia.core.layout.Axis;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.FlyweightCellDomain;
import org.hawkinssoftware.azia.input.MouseInputEvent.Button;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.UserInterfaceHandler;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.cell.handler.CellViewportSelectionHandler;
import org.hawkinssoftware.azia.ui.component.cell.transaction.SetSelectedRowDirective;
import org.hawkinssoftware.azia.ui.component.composition.CompositionElement;
import org.hawkinssoftware.azia.ui.component.composition.CompositionRegistry;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.input.MouseAware.EventPass;
import org.hawkinssoftware.azia.ui.model.RowAddress;
import org.hawkinssoftware.azia.ui.paint.InstancePainter;
import org.hawkinssoftware.azia.ui.paint.InstancePainter.TextMetrics.BoundsType;
import org.hawkinssoftware.azia.ui.paint.basic.cell.AbstractCellStamp;
import org.hawkinssoftware.azia.ui.paint.basic.cell.CellStamp;
import org.hawkinssoftware.azia.ui.paint.canvas.Canvas;
import org.hawkinssoftware.azia.ui.paint.canvas.Size;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.util.UnknownEnumConstantException;
import org.hawkinssoftware.rns.core.validation.ValidateRead;
import org.hawkinssoftware.rns.core.validation.ValidateWrite;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

/**
 * @JTourBusStop 5, StationRegionDomain-StationDomain isolation, WeatherStationStamp isolated within the StationDomain:
 * 
 *               This stamp could be used to construct another scrollable station list, but the package-private
 *               declaration makes it only visible to the WeatherStationView, which we just saw is itself restricted to
 *               the StationDomain. Having isolated all station list rendering within the StationDomain, the only way
 *               for outside classes to collaborate with this functionality is to go through the
 *               WeatherStationsController.
 * 
 * @JTourBusStop 4, Homogenous initialization using @InitializationAspect, Client code also inherits the
 *               CompositionElement pointcut:
 * 
 *               The CompositionElement is defined in the Azia library, and its @InitializationAspect is applied to all
 *               implementors, even those within client code. This stamp paints a weather station in a scrollable list
 *               of the Weather Viewer application, and it is automatically registered in the CompositionRegistry on
 *               constructor exit, just like the Azia library's ButtonComposite, and every other implementor.
 */
@ValidateRead
@ValidateWrite
@VisibilityConstraint(domains = WeatherViewerAssemblyDomain.class)
@DomainRole.Join(membership = StationDomain.class)
class WeatherStationStamp extends AbstractCellStamp<WeatherStation>
{
	/**
	 * @JTourBusStop 8, Application assembly, WeatherViewerAssemblyDomain owns the WeatherStationStamp.Factory:
	 * 
	 *               The stamp factory is installed in a CellViewport and produces weather station stamps at its
	 *               request. The stamps are used to draw station names in the scrollable station list. This
	 *               instantiation functionality belongs in the assembly domain, even though the stamps it produces are
	 *               in the StationDomain.
	 */
	@VisibilityConstraint(domains = WeatherViewerAssemblyDomain.class)
	static class Factory implements CellStamp.Factory
	{
		private final CellStamp<WeatherStation> stamp = new WeatherStationStamp();

		@SuppressWarnings("unchecked")
		public <DataType> CellStamp<DataType> getStamp(RowAddress address, DataType datum)
		{
			return (CellStamp<DataType>) stamp;
		}
	}

	public static final CellPluginKey<CellHandler> CELL_PLUGIN_KEY = new CellPluginKey<CellHandler>();

	private Size size = Size.EMPTY;

	private CellViewportComposite<?> viewport;

	private CellViewportSelectionHandler selection;

	@Override
	public void compositionCompleted()
	{
		super.compositionCompleted();

		ScrollPaneComposite<CellViewportComposite<?>> historyList = CompositionRegistry.getComposite(ScrollPaneComposite.getGenericClass());
		viewport = historyList.getViewport();
		selection = historyList.getService(CellViewportSelectionHandler.class);
	}

	private String getStampText(WeatherStation station)
	{
		return station.name;
	}

	protected void interactiveCellCreated(InteractiveCell cell)
	{
		cell.addPlugin(new CellHandler(cell));
	}

	@Override
	protected void paint(RowAddress address, WeatherStation datum, InteractiveCell interactiveCell)
	{
		Canvas c = Canvas.get();

		if (selection.getSelectedRow() == address.row)
		{
			c.pushColor(WeatherViewerComponents.SELECTION_BACKGROUND);
			c.g.fillRect(0, 0, c.span().width, c.span().height);
		}

		c.pushColor(Color.black);
		c.g.drawString(getStampText(datum), 0, CellStamp.TEXT_BASELINE);
	}

	@Override
	public int getSpan(Axis axis, WeatherStation datum)
	{
		switch (axis)
		{
			case H:
				size = InstancePainter.TextMetrics.INSTANCE.getSize(getStampText(datum), BoundsType.TEXT);
				return size.width;
			case V:
				// TODO: agent gags on this constant when not qualified by the interface name
				return CellStamp.ROW_HEIGHT;
			default:
				throw new UnknownEnumConstantException(axis);
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	@ValidateRead
	@ValidateWrite
	@VisibilityConstraint(types = WeatherStationStamp.class)
	@DomainRole.Join(membership = { FlyweightCellDomain.class })
	public static class CellHandler implements CellPlugin, UserInterfaceHandler, CompositionElement.Initializing, UserInterfaceActorDelegate
	{
		private final InteractiveCell cell;
		private CellViewportComposite<?> viewport;

		public CellHandler(InteractiveCell cell)
		{
			this.cell = cell;
			cell.installHandler(this);
		}

		@Override
		public void compositionCompleted()
		{
			// WIP: it's very annoying that this cell needs to know which composite it's registered under. I want the
			// viewport, and would prefer to simply ask for it, but the stamp factory was instantiated under the scroll
			// pane composite, so that is the only thing I can see from here.
			viewport = CompositionRegistry.getComposite(ScrollPaneComposite.getGenericClass()).getViewport();
		}

		/**
		 * @JTourBusStop 4.4, Virtual encapsulation in an Azia user interface transaction, MouseEventTransaction
		 *               propagated through client components:
		 * 
		 *               ...until it reaches a client component, such as this list cell representing a WeatherStation in
		 *               the Weather Viewer application. If the native mouse event was a left button press, this cell
		 *               will contribute a SetSelectedRowDirective, indicating that the list row represented by this
		 *               cell should now be selected.
		 */
		public void mouseEvent(EventPass pass, PendingTransaction transaction)
		{
			if (!ComponentRegistry.getInstance().getFocusHandler().windowHasFocus(this))
			{
				return;
			}

			if (pass.event().getButtonPress() == Button.LEFT)
			{
				transaction.contribute(new SetSelectedRowDirective(viewport, cell.cellContext.getAddress().row));
			}
		}

		@Override
		public UserInterfaceActor getActor()
		{
			return cell.getActor();
		}

		@Override
		public CellPluginKey<? extends CellPlugin> getKey()
		{
			return CELL_PLUGIN_KEY;
		}
	}
}
