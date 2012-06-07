package org.peimari.vrg;

import com.vaadin.Application;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class VRGApplication extends Application
{
    @Override
    public void init()
    {
    	setMainWindow(new VRGWindow());
    	getMainWindow().addURIHandler(MapHandler.instance);
    }
    
}
