package org.hawkinssoftware.ui.util.weather.control;

import java.awt.Color;
import java.util.List;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
import org.hawkinssoftware.azia.core.layout.Axis;
import org.hawkinssoftware.azia.core.layout.BoundedEntity.Expansion;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.FlyweightCellDomain;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.UserInterfaceHandler;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.cell.transaction.SetSelectedRowDirective;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.model.RowAddress;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel.ModelListWriteDomain;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.paint.basic.text.LabelPainter;
import org.hawkinssoftware.azia.ui.paint.plugin.BackgroundPlugin;
import org.hawkinssoftware.azia.ui.tile.UnitTile.Layout;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationRegionDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

@InvocationConstraint(domains = WeatherViewerControllerDomain.class)
@VisibilityConstraint(domains = { InitializationDomain.class, WeatherViewerControllerDomain.class })
@DomainRole.Join(membership = { WeatherViewerControllerDomain.class, StationRegionDomain.class, ListDataModel.ModelListDomain.class, FlyweightCellDomain.class })
public class WeatherStationRegionsController implements UserInterfaceHandler
{
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherStationRegionsController();
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle assembleView(
			ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<WeatherViewerComponents.LayoutKey> window)
	{
		ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.PairHandle regionListFrame = transaction.createPairTile(
				WeatherViewerComponents.LayoutKey.STATION_REGION_LIST_FRAME, Axis.V);

		ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle regionListLabel = transaction
				.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LABEL_PANEL);
		ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.UnitHandle regionListPanel = transaction
				.createUnitTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LIST_PANEL);
		ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle regionLabel = transaction
				.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LABEL);
		ModifyLayoutTransaction<WeatherViewerComponents.LayoutKey>.ComponentHandle regionList = transaction
				.createComponentTile(WeatherViewerComponents.LayoutKey.STATION_REGION_LIST);

		LabelComposite<Label, ?> regionLabelComponent = ComponentRegistry.getInstance().establishComposite(WeatherViewerComponents.getRegionLabel(), window);
		// WIP: these backgrounds are kind of a hack
		((LabelPainter<Label>) regionLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
		ChangeTextDirective setLabelText = new ChangeTextDirective(regionLabelComponent.getComponent(), "Regions");
		adHocTransaction.addAction(setLabelText);

		ScrollPaneComposite<CellViewportComposite<?>> regionListComponent = ComponentRegistry.getInstance().establishComposite(
				WeatherViewerComponents.getStationRegionList(), window);

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

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static WeatherStationRegionsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherStationRegionsController INSTANCE;

	private final ScrollPaneComposite<CellViewportComposite<?>> regionList;
	private final ListDataModel regionModel;
	private final PopulateListTask populateTask = new PopulateListTask();

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	private WeatherStationRegionsController()
	{
		regionList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.getStationRegionList());
		regionModel = regionList.getViewport().getService(ListDataModel.class);
		regionList.getViewport().installHandler(this);
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void initializeView() throws ConcurrentAccessException
	{
		populateTask.start();
	}

	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if (change.row < 0)
		{
			return;
		}

		RowAddress address = regionList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherStationsController.getInstance().displayStationRegion((WeatherStationRegion) regionModel.get(address));
	}

	@DomainRole.Join(membership = ModelListWriteDomain.class)
	private class PopulateListTask extends UserInterfaceTask
	{
		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = regionModel.createSession(getTransaction(ListDataModelTransaction.class));
			List<WeatherStationRegion> regions = WeatherDataModel.getInstance().getRegions();
			for (WeatherStationRegion region : regions)
			{
				session.add(region);
			}

			regionList.getComponent().requestRepaint();

			return true;
		}
	}
}
