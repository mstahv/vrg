package org.peimari.vrg;

import java.util.Collection;

import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.vrg.service.VRGService;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class CompetitionView extends NavigationView {

	public CompetitionView(Competition unloaded, final VRGService service) {
		
		final Competition competition = service.load(unloaded);
		setCaption(competition.getName());
		
		VerticalComponentGroup verticalComponentGroup = new VerticalComponentGroup();
		
		Collection<CompetitionClass> competitionClasses = competition.getCompetitionClasses();
		for (final CompetitionClass cClass : competitionClasses) {
			NavigationButton navigationButton = new NavigationButton();
			navigationButton.setCaption(cClass.getName());
			navigationButton.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					getNavigationManager().navigateTo(new ClassView(cClass, competition, service));
				}
			});
			verticalComponentGroup.addComponent(navigationButton);
		}
		
		setContent(verticalComponentGroup);
	}

}
