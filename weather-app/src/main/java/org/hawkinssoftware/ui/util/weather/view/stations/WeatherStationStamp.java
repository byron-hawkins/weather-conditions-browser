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
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

public class WeatherStationStamp extends AbstractCellStamp<WeatherStation>
{
	@VisibilityConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static class Factory implements CellStamp.Factory
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
