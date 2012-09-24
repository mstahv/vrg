package org.peimari.vrg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.Competitor;
import org.peimari.vrg.service.VRGService;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationListener;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;

public class ClassView extends NavigationView implements NavigationListener {

	private VRGService service;

	private Set<Competitor> selected = new HashSet<Competitor>();

	private CompetitionClass cClass;

	private MapView map;

	private Competition competition;

	public ClassView(CompetitionClass cClass, final Competition competition,
			VRGService service) {
		this.service = service;
		this.cClass = cClass;
		this.competition = competition;

		setCaption(cClass.getName());

		VerticalComponentGroup verticalComponentGroup = new VerticalComponentGroup();

		Collection<Competitor> allCompetitors = cClass.getCompetitors();
		for (final Competitor c : allCompetitors) {
			final Switch comSelector = new Switch();
			comSelector.setCaption(c.getName());
			comSelector.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					if (comSelector.booleanValue()) {
						selected.add(c);
					} else {
						selected.remove(c);
					}
				}
			});
			comSelector.setEnabled(c.getRoutePoints() != null);
			verticalComponentGroup.addComponent(comSelector);
		}

		setContent(verticalComponentGroup);

		NavigationButton c = new NavigationButton(prepareMapView());
		c.setStyleName("forward");
		setRightComponent(c);

	}

	@Override
	public void attach() {
		super.attach();
		getNavigationManager().addListener(this);
		MapView prepareMapView = prepareMapView();
		prepareMapView.setPreviousComponent(this);
		getNavigationManager().setNextComponent(prepareMapView);
	}
	
	@Override
	public void detach() {
		getNavigationManager().removeListener(this);
		super.detach();
	}

	private MapView prepareMapView() {
		if (map == null) {
			map = new MapView(cClass, competition, service);
		}
		return map;
	}
	
	@Override
	public void navigate(NavigationEvent event) {
		if (getNavigationManager().getCurrentComponent() == map) {
			map.drawRoutes(selected);
		}
	}

}
