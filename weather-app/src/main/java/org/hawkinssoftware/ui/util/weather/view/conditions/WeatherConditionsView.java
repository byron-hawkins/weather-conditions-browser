package org.hawkinssoftware.ui.util.weather.view.conditions;

import java.awt.Color;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransactionDomains.TransactionParticipant;
import org.hawkinssoftware.azia.core.layout.Axis;
import org.hawkinssoftware.azia.core.layout.BoundedEntity.Expansion;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.paint.basic.text.LabelPainter;
import org.hawkinssoftware.azia.ui.paint.plugin.BackgroundPlugin;
import org.hawkinssoftware.azia.ui.tile.LayoutRegion.TileLayoutDomain;
import org.hawkinssoftware.azia.ui.tile.UnitTile.Layout;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LabelAssembly;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

/**
 * @JTourBusStop 3, StationReport display isolation, WeatherConditionsView is private to the StationConditionsDomain::
 * 
 *               ...the view itself cannot be seen outside the StationConditionsDomain, per the @VisibilityConstraint. A
 *               tangential requirement of this isolation strategy is that all StationConditionDomain members must
 *               restrict access to assembly instances acquired from this view.
 * 
 * @JTourBusStop 4, StationDomain-StationConditionsDomain isolation, WeatherConditionsView isolated within the
 *               StationConditionsDomain:
 * 
 *               The functionality for displaying weather reports in the scrollable text area is contained within the
 *               StationConditionsDomain by this @VisibilityConstraint.
 */
@VisibilityConstraint(domains = StationConditionsDomain.class)
@DomainRole.Join(membership = { StationConditionsDomain.class, TileLayoutDomain.class, TransactionParticipant.class })
public class WeatherConditionsView
{
	/**
	 * @JTourBusStop 1, StationReport display, WeatherConditionsView.TextAreaAssembly - scrollable text area descriptor:
	 * 
	 *               This descriptor is used by the ComponentRegistry to construct the scroll pane with its viewport
	 *               with its basic behaviors, which include displaying a plain text string.
	 */
	private class TextAreaAssembly extends TextViewportComposite.ScrollPaneAssembly
	{
		@Override
		public void assemble(ScrollPaneComposite<TextViewportComposite> scrollPane)
		{
			super.assemble(scrollPane);

			WeatherConditionsFormatContributor.install(scrollPane.getViewport());
		}
	}

	private final LabelAssembly labelAssembly = new LabelAssembly(LayoutKey.STATION_DATA_LABEL_PANEL);

	/**
	 * @JTourBusStop 1, StationReport display isolation, WeatherConditionsView hides the text area lookup handle:
	 * 
	 *               Modifying the station report text requires access to the scroll pane viewport, which is available
	 *               from the ComponentRegistry using this handle.
	 */
	private final TextAreaAssembly textAreaAssembly = new TextAreaAssembly();

	/**
	 * @JTourBusStop 5, Application assembly, WeatherConditionsController invokes WeatherConditionsView.assembleView( ):
	 * 
	 *               This assembly method is an extension of the tangent mentioned in the last tour stop. The @InvocationConstraint
	 *               does not ordinarily allow a controller to invoke it, but because the calling method is constrained
	 *               to the same domain, the invocation is allowed by proxy. The same observations apply to the parallel
	 *               assembleView() methods in the other views.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public ModifyLayoutTransaction<LayoutKey>.PairHandle assembleView(ModifyLayoutTransaction<LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<LayoutKey> window)
	{
		ModifyLayoutTransaction<LayoutKey>.PairHandle stationDataFrame = transaction.createPairTile(LayoutKey.STATION_DATA_FRAME, Axis.V);
		ModifyLayoutTransaction<LayoutKey>.UnitHandle stationDataLabel = transaction.createUnitTile(LayoutKey.STATION_DATA_LABEL_PANEL);
		ModifyLayoutTransaction<LayoutKey>.UnitHandle stationDataPanel = transaction.createUnitTile(LayoutKey.STATION_DATA_PANEL);
		ModifyLayoutTransaction<LayoutKey>.ComponentHandle dataLabel = transaction.createComponentTile(LayoutKey.DATA_LABEL);
		ModifyLayoutTransaction<LayoutKey>.ComponentHandle stationData = transaction.createComponentTile(LayoutKey.STATION_DATA);

		ScrollPaneComposite<TextViewportComposite> stationConditionsComponent = ComponentRegistry.getInstance().establishComposite(textAreaAssembly, window);

		LabelComposite<Label, ?> dataLabelComponent = ComponentRegistry.getInstance().establishComposite(labelAssembly, window);
		((LabelPainter<Label>) dataLabelComponent.getPainter()).setBackground(new BackgroundPlugin.Solid<Label>(new Color(0xDDDDDD)));
		ChangeTextDirective setLabelText = new ChangeTextDirective(dataLabelComponent.getComponent(), "Weather Conditions");
		adHocTransaction.addAction(setLabelText);

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

		dataLabel.setComponent(dataLabelComponent);
		stationData.setComponent(stationConditionsComponent);

		return stationDataFrame;
	}

	/**
	 * @JTourBusStop 2, StationReport display isolation, WeatherConditionsView.textAreaAssembly accessor might make it
	 *               available outside the StationConditionsDomain, except that...:
	 * 
	 *               The station report text area can be obtained from the ComponentRegistry using this field as a key.
	 *               This accessor might make it available outside the StationDomain, except...
	 */
	public TextAreaAssembly getTextAreaAssembly()
	{
		return textAreaAssembly;
	}
}
