package org.peimari.vrg;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.Competitor;
import org.peimari.util.Util;
import org.peimari.vrg.service.VRGService;
import org.peimari.vrg.service.VRGService.ImageDetails;
import org.vaadin.vol.AnimatedPointVector;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Control;
import org.vaadin.vol.ImageLayer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.Point;
import org.vaadin.vol.PolyLine;
import org.vaadin.vol.Style;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

public class MapController2 extends VerticalLayout implements Component,
		ValueChangeListener, ClickListener {

	private OpenLayersMap map;
	private VectorLayer tracs;
	private CourseLayer course;
	private NativeSelect classSelector;
	private Table competitors;
	private NativeSelect competitionSelector;
	private Button animate;
	private Slider speedSlider;

	public MapController2(OpenLayersMap map) {
		this.map = map;
	}

	@Override
	public void attach() {
		super.attach();
		buildView();
	}

	private void buildView() {
		Button button = new Button("<< Choose gadget");
		button.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				getApplication().close();
			}});
		addComponent(button);
		competitionSelector = new NativeSelect("Competition");
		competitionSelector.setImmediate(true);
		competitionSelector.addListener(this);
		List<Competition> competitions = service.getCompetitions();
		competitionSelector
				.setContainerDataSource(new BeanItemContainer<Competition>(
						Competition.class, competitions));
		addComponent(competitionSelector);

		classSelector = new NativeSelect("Class");
		classSelector.addListener(this);
		classSelector.setImmediate(true);
		addComponent(classSelector);

		competitors = new Table();
		competitors.setSizeFull();
		competitors.setImmediate(true);
		competitors.addListener(this);
		competitors.addGeneratedColumn("routePoints", new ColumnGenerator() {
			public Object generateCell(Table source, Object itemId,
					Object columnId) {
				final Competitor c = (Competitor) itemId;
				if (c.getRoutePoints() == null) {
					return "";
				}
				final String color = getColorForCompetitor(itemId);
				CheckBox checkBox = new CheckBox();
				checkBox.setImmediate(true);
				CssLayout cssLayout = new CssLayout() {
					@Override
					protected String getCss(Component c) {
						return "background: " + color + ";";
					}
				};
				checkBox.addListener(new ClickListener() {
					public void buttonClick(ClickEvent event) {
						toggleRoute(c);
					}
				});
				cssLayout.addComponent(checkBox);
//				cssLayout.addComponent(new Label(color));
				return cssLayout;
			}
		});
		competitors.addGeneratedColumn("time", new ColumnGenerator() {
			public Object generateCell(Table source, Object itemId,
					Object columnId) {
				return Util.formatTime(1000 * (Integer) source.getItem(itemId)
						.getItemProperty(columnId).getValue());
			}
		});

		competitors.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				if (propertyId == null) {
					Competitor c = (Competitor) itemId;
					if (c.getRoutePoints() == null) {
						return "noroute";
					} else if (drawnRoutes.containsKey(c)) {
						return "drawn";
					}
				}
				return null;
			}
		});

		addComponent(competitors);
		setExpandRatio(competitors, 1);

		try {
			Competition competition2 = competitions.get(0);
			competitionSelector.setValue(competition2);

		} catch (Exception e) {
		}

		speedSlider = new Slider("Animation speed");
		speedSlider.setMin(0.5);
		speedSlider.setMax(200);
		speedSlider.setWidth("100%");
		try {
			speedSlider.setValue(10);
		} catch (ValueOutOfBoundsException e) {
		}
		addComponent(speedSlider);
		
		animate = new Button("Animate", (ClickListener) this);
		addComponent(animate);

	}

	protected void toggleRoute(Competitor c) {
		Vector remove = drawnRoutes.remove(c);
		if (remove != null) {
			tracs.removeComponent(remove);
		} else {
			Bounds bounds = new Bounds();
			drawCompetitor(bounds, c);
		}
	}

	protected String getColorForCompetitor(Object itemId) {
		int indexOfId = competitorContainer.indexOfId(itemId);
		String color = Colors.getByIndex(indexOfId);
		return color;
	}

	private HashMap<Competitor, Vector> drawnRoutes = new HashMap<Competitor, Vector>();
	private Competition competition;
	private BeanItemContainer<Competitor> competitorContainer;
	private CompetitionClass competitionClass;
	private VectorLayer animationLayer;
	private URL root;
	private VRGService service;
	
	private void drawMap(Competition c) {
		if (competition.getMap() == null) {
			throw new RuntimeException("No map found");
		}
		long currentTimeMillis = System.currentTimeMillis();
		ImageDetails imageDetails = service.getMapDetails(competition);
		System.err
		.println((System.currentTimeMillis() - currentTimeMillis)
				+ " MS");

		MapHandler.addFile(service.getMapFile(competition.getMap()),
				competition.getMap().getId(), imageDetails.getFormatName());

		ComponentContainer parent2 = (ComponentContainer) map.getParent();
		OpenLayersMap map2 = new OpenLayersMap();
		parent2.replaceComponent(map, map2);
		map = map2;
		map.setSizeFull();

		ImageLayer imageLayer = new ImageLayer(getApplication().getURL()
				.toExternalForm() + "MAPS/" + competition.getMap().getId(),
				imageDetails.getWidth() / 4, imageDetails.getHeight() / 4);
		imageLayer.setBounds(0d, (double) -imageDetails.getHeight(), (double) imageDetails.getWidth(), 0d);
		map.setApiProjection("EPSG:900913");
		map.setJsMapOptions("{numZoomLevels: 5, projection: new OpenLayers.Projection(\"EPSG:900913\"), sphericalMercator: true, maxExtent: new OpenLayers.Bounds(0,-"
				+ 5000 + "," + 5000 + ",0)}");
		map.addLayer(imageLayer);
		map.getControls().clear();
		map.addControl(Control.Navigation);
		map.addControl(Control.MousePosition);
		map.addControl(Control.PanZoomBar);
		map.zoomToExtent(new Bounds(new Point(0, 0), new Point(imageDetails.getWidth(),
				-imageDetails.getHeight())));

		tracs = new VectorLayer();
		course = new CourseLayer();
		map.addLayer(course);
		map.addLayer(tracs);

	}

	protected void setCompetitors(Collection<Competitor> competitors) {
		tracs.removeAllComponents();
		Bounds bounds = new Bounds();
		for (Competitor competitor : competitors) {
			drawCompetitor(bounds, competitor);
		}
		map.zoomToExtent(bounds);
	}

	private void drawCompetitor(Bounds bounds, Competitor competitor) {
		org.peimari.rgdomain.Point[] routePoints = competitor.getRoutePoints();
		PolyLine polyLine = new PolyLine();
		Point[] points = new Point[routePoints.length];
		for (int i = 0; i < points.length; i++) {
			org.peimari.rgdomain.Point p = routePoints[i];
			Point point = points[i] = new Point(p.getLon(), p.getLat());
			bounds.extend(point);
		}
		Style s = new Style();
		s.setStrokeColor(getColorForCompetitor(competitor));
		s.setStrokeWidth(3);
		s.setStrokeOpacity(0.75);
		polyLine.setCustomStyle(s);
		polyLine.setPoints(points);
		tracs.addComponent(polyLine);
		drawnRoutes.put(competitor, polyLine);
	}

	public void valueChange(ValueChangeEvent event) {
		Object value = event.getProperty().getValue();
		if (event.getProperty() == competitionSelector) {
			setCompetition((Competition) value);
		} else if (event.getProperty() == classSelector) {
			setClass((CompetitionClass) value);
		} else if (event.getProperty() == competitors) {
			setCompetitors((Collection<Competitor>) value);
		}
	}

	private void setClass(CompetitionClass value) {
		Bounds bounds = new Bounds();
		if (value == null) {
			return;
		}
		competitionClass = value;
		course.drawCourse(bounds, value);
		map.zoomToExtent(bounds);
		competitorContainer = new BeanItemContainer<Competitor>(
				Competitor.class, value.getCompetitors());
		competitors.setContainerDataSource(competitorContainer);
		competitors.setVisibleColumns(new Object[] { "name", "time",
				"routePoints" });

		while (!drawnRoutes.isEmpty()) {
			Vector vector = drawnRoutes.remove(drawnRoutes.keySet().iterator()
					.next());
			tracs.removeComponent(vector);
		}
	}

	private void setCompetition(Competition value) {
		if (this.competition != null && competition.getId() == value.getId()) {
			return;
		}
		this.competition = service.load(value);
		drawMap(value);
		Collection<CompetitionClass> competitionClasses = competition
				.getCompetitionClasses();
		classSelector
				.setContainerDataSource(new BeanItemContainer<CompetitionClass>(
						CompetitionClass.class, competitionClasses));
		try {
			classSelector.setValue(competitionClasses.iterator().next());
		} catch (Exception e) {
		}
	}

	public void buttonClick(ClickEvent event) {
		if (event.getButton() == animate) {
			runAnimation();
		}
	}

	private void runAnimation() {
		if (animationLayer != null) {
			map.removeLayer(animationLayer);
		}
		animationLayer = new VectorLayer();
		Set<Competitor> keySet = drawnRoutes.keySet();
		double extraspeed = (Double) speedSlider.getValue();
		for (Competitor competitor : keySet) {
			org.peimari.rgdomain.Point[] routePoints = competitor
					.getRoutePoints();
			int[] splitTimes = new int[competitor.getSplitTimes().length + 1];
			System.arraycopy(competitor.getSplitTimes(), 0, splitTimes, 0,
					splitTimes.length - 1);
			splitTimes[splitTimes.length - 1] = competitor.getTime();

			org.peimari.rgdomain.Point[] controlPoints = competitionClass
					.getCourse().getControlPoints();
			int lastControlIdx = 0;
			AnimatedPointVector pv = new AnimatedPointVector();
			Style style = new Style();
			style.setPointRadius(6);
			style.setStrokeWidth(2);
			String colorForCompetitor = getColorForCompetitor(competitor);
			style.setStrokeColor(colorForCompetitor);
			style.setFillColor(colorForCompetitor);
			style.setFillOpacity(0.3);
			pv.setCustomStyle(style);
			animationLayer.addComponent(pv);
			int lastSplitCumuTime = 0;
			for (int i = 1; i < controlPoints.length; i++) {
				double routeLength = 0;
				org.peimari.rgdomain.Point controlPoint = controlPoints[i];
				for (int j = lastControlIdx; j < routePoints.length; j++) {
					org.peimari.rgdomain.Point rp = routePoints[j];
					if (j > lastControlIdx) {
						routeLength += Point2D.distance(rp.getLat(),
								rp.getLon(), routePoints[j - 1].getLat(),
								routePoints[j - 1].getLon());
					}
					if (rp.equals(controlPoint)) {
						try {
							double timeForControl = splitTimes[i]
									- lastSplitCumuTime;
							double speed = routeLength / timeForControl;
							org.peimari.rgdomain.Point lastPoint = controlPoints[i - 1];
							double cumuTimeOnSplit = lastSplitCumuTime;
							for (int k = lastControlIdx; k < j; k++) {
								org.peimari.rgdomain.Point p = routePoints[k];
								double pd = Point2D.distance(p.getLat(),
										p.getLon(), lastPoint.getLat(),
										lastPoint.getLon());
								double dt = pd / speed;
								cumuTimeOnSplit += dt;
								pv.addKeyFrame(
										(int) (1000 / extraspeed * cumuTimeOnSplit),
										new Point(p.getLon(), p.getLat()));
								lastPoint = p;
							}
							lastSplitCumuTime = splitTimes[i];
							lastControlIdx = j;
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}

		}
		map.addLayer(animationLayer);
	}

	public void setRoot(URL baseurl) {
		root = baseurl;
		service = new VRGService(root);
	}

}
