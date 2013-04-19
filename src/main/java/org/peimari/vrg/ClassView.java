package org.peimari.vrg;

import java.net.URLEncoder;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.Competitor;
import org.peimari.vrg.service.VRGService;

import com.vaadin.addon.touchkit.ui.HorizontalComponentGroup;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationListener;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;

public class ClassView extends NavigationView implements NavigationListener {

	private VRGService service;

	private Set<Competitor> selected = new HashSet<Competitor>();

	private CompetitionClass cClass;

	private MapView map;

	private Competition competition;

	public ClassView(final CompetitionClass cClass,
			final Competition competition, final VRGService service) {
		this.service = service;
		this.cClass = cClass;
		this.competition = competition;

		setCaption(cClass.getName());

		NavigationButton c = new NavigationButton(prepareMapView());
		c.setStyleName("forward");
		setRightComponent(c);

	}

	public void buildContent() {
		VerticalComponentGroup verticalComponentGroup = new VerticalComponentGroup();

		Collection<Competitor> allCompetitors = cClass.getCompetitors();
		for (final Competitor c : allCompetitors) {
			boolean hasRoute = c.getRoutePoints() != null;
			if (hasRoute) {
				final Switch comSelector = new Switch();
				comSelector.setCaption(c.getName());
				comSelector.setValue(selected.contains(c));
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
				verticalComponentGroup.addComponent(comSelector);
			} else {
				HorizontalComponentGroup g = new HorizontalComponentGroup();
				g.setCaption(c.getName());
				Button button = new Button("Draw route");
				button.setWidth("100px");
				button.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						getNavigationManager().navigateTo(
								new DrawRouteView(cClass, competition, service,
										c));
					}
				});
				g.addComponent(button);
				verticalComponentGroup.addComponent(g);
			}
		}
		
		
		String url = URLEncoder.encode(service.getRoot().toExternalForm());
		
		Label label = new Label("#"+url+ ";" + competition.getId() + ";"+cClass.getId() + ";-1;" );
		label.setCaption("Deep link:");
		verticalComponentGroup.addComponent(label);

		setContent(verticalComponentGroup);
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
	
	@Override
	protected void onBecomingVisible() {
		super.onBecomingVisible();
		buildContent();
	}

}
