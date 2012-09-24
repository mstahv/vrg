package org.peimari.vrg;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.peimari.rgdomain.Competition;
import org.peimari.vrg.service.VRGService;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class GadgetView extends NavigationView {

	private VRGService service;
	private VerticalComponentGroup verticalComponentGroup;

	public GadgetView(URL baseurl) {
		setCaption(baseurl.getHost());
		service = new VRGService(baseurl);
		
		Button button = new Button("Clear cache");
		button.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				service.clearCache();
				buildList();
			}
		});
		setRightComponent(button);
		
		verticalComponentGroup = new VerticalComponentGroup();
		
		buildList();
		
		setContent(verticalComponentGroup);
		
	}
	
	static DateFormat sdf = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

	private void buildList() {
		verticalComponentGroup.removeAllComponents();
		List<Competition> competitions = service.getCompetitions();
		for (final Competition competition : competitions) {
			NavigationButton navigationButton = new NavigationButton();
			navigationButton.setCaption(competition.getName());
			navigationButton.setDescription(sdf.format(competition.getDate()));
			navigationButton.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					getNavigationManager().navigateTo(new CompetitionView(competition, service));
				}
			});
			verticalComponentGroup.addComponent(navigationButton);
		}
	}

}
