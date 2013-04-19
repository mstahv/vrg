package org.peimari.vrg;

import java.net.URL;

import org.peimari.vrg.service.VRGService;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class InitialView extends NavigationView implements Component {

	private Button newGadget = new Button("New");
	private Button about = new Button("About");

	private VerticalComponentGroup verticalComponentGroup;

	public InitialView(final VRGMobileWindow vrgMobileWindow) {
		setCaption("Köyhänmiehenhärveli");
		setLeftComponent(about);
		newGadget.setStyleName("green");
		setRightComponent(newGadget);

		newGadget.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				new NewGadgetView(vrgMobileWindow).showRelativeTo(newGadget);
			}
		});
		
		setLeftComponent(about);
		about.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				new AboutView(vrgMobileWindow).showRelativeTo(about);
			}
		});

		CssLayout l = new CssLayout();
		l.setWidth("100%");

		verticalComponentGroup = new VerticalComponentGroup();
		verticalComponentGroup.setCaption("Previously used gadgets");

		reloadList();

		l.addComponent(verticalComponentGroup);

		l.addComponent(new Label(
				"<div style='font-size: x-small; text-align:right;'>"
						+ SessionCounter.getAndIncrement() + " sessions.</div>",
				Label.CONTENT_XHTML));
		
		
		Button button = new Button("Open in 'desktop mode'");
		button.setStyleName(Button.STYLE_LINK);
		button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().showNotification("TODO");
				
			}
		});
		l.addComponent(button);

		setContent(l);

	}

	public void reloadList() {
		verticalComponentGroup.removeAllComponents();
		URL[] latestUrls = VRGService.latestUrls();
		for (int i = latestUrls.length - 1; i > -1; i--) {
			final URL u = latestUrls[i];
			NavigationButton navigationButton = new NavigationButton(
					u.getHost());
			navigationButton.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					getNavigationManager().navigateTo(new GadgetView(u));
				}
			});
			verticalComponentGroup.addComponent(navigationButton);
		}
		if(latestUrls.length == 0) {
			verticalComponentGroup.addComponent(new Label("No previous gadgets"));
		}
	}

}
