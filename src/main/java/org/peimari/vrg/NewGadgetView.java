package org.peimari.vrg;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class NewGadgetView extends Popover {

	public NewGadgetView(final VRGMobileWindow vrgMobileWindow) {
		setWidth("360px");
		setHeight("80%");
		NavigationView navigationView = new NavigationView();
		navigationView.setCaption("Open new gadget");
		setContent(navigationView);
		CssLayout l = new CssLayout();
		l.setMargin(true);
		l.addComponent(new Label("Open new RG with url ('kartat' directory, guessed if script given):"));
		final TextField url = new TextField();
		url.setWidth("100%");
		url.setValue("http://av.nettirasia.com/reitti/cgi-bin/reitti.cgi");
		// url.setValue("file:///Users/Shared/routegadget/reitti/kartat/");
		l.addComponent(url);
		
		Button button = new Button("Open", new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				String root = url.getValue().toString();
				if (root.contains("cgi-bin")) {
					if (root.contains("id=")) {
						// TODO deep linking
					}

					root = root.substring(0, root.indexOf("cgi-bin"));
					root = root + "kartat/";
				}
				try {
					vrgMobileWindow.openGadgetViewer(new URL(root));
					removeFromParent();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		button.setClickShortcut(KeyCode.ENTER, null);
		l.addComponent(button);

		navigationView.setContent(l);
	}

}
