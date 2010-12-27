package fiji.plugin.trackmate.gui;

import static fiji.plugin.trackmate.gui.TrackMateFrame.SMALL_FONT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fiji.plugin.trackmate.Feature;
import fiji.plugin.trackmate.FeatureThreshold;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotImp;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class ThresholdGuiPanel extends ActionListenablePanel implements ChangeListener {
	
	private static final long serialVersionUID = 1307749013344373051L;
	private final ChangeEvent CHANGE_EVENT = new ChangeEvent(this);
	/** Will be set to the value of the {@link JPanelSpotColorGUI}. */
	public ActionEvent COLOR_FEATURE_CHANGED = null;
	
	private static final String ADD_ICON = "images/add.png";
	private static final String REMOVE_ICON = "images/delete.png";
	


	private JPanel jPanelBottom;
	private JPanelSpotColorGUI jPanelSpotColorGUI;

	private JScrollPane jScrollPaneThresholds;
	private JButton jButtonRemoveThreshold;
	private JPanel jPanelAllThresholds;
	private JButton jButtonAddThreshold;
	private JPanel jPanelButtons;

	private Stack<ThresholdPanel<Feature>> thresholdPanels = new Stack<ThresholdPanel<Feature>>();
	private Stack<Component> struts = new Stack<Component>();
	private Collection<? extends Collection<? extends Spot>> spots;
	private EnumMap<Feature, double[]> featureValues = new EnumMap<Feature, double[]>(Feature.class);
	private int newFeatureIndex;
	
	private List<FeatureThreshold> featureThresholds = new ArrayList<FeatureThreshold>();
	private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	
	
	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * CONSTRUCTOR
	 */
	
	public ThresholdGuiPanel(Collection<List<Spot>> spots, Feature selectedFeature) {
		super();
		newFeatureIndex = selectedFeature.ordinal();
		setSpots(spots);
		initGUI();
		if (null != spots)
			addThresholdPanel();
	}

	public ThresholdGuiPanel(Collection<List<Spot>> allSpots) {
		this(allSpots, Feature.values()[0]);
	}
	
	public ThresholdGuiPanel() {
		this(null);
	}
	
	/*
	 * PUBLIC METHODS
	 */
	
	/**
	 * Set the Spot collection displayed in this GUI.
	 * <p>
	 * Calling this method causes the individual threshold panel to be all 
	 * removed.
	 */
	public void setSpots(Collection<? extends Collection<? extends Spot>> spots) {
		for(ThresholdPanel<Feature> tp : thresholdPanels)
			jPanelAllThresholds.remove(tp);
		for(Component strut : struts)
			jPanelAllThresholds.remove(strut);
		if (null != jPanelAllThresholds)
			jPanelAllThresholds.repaint();
		this.spots = spots;
		prepareDataArrays();
	}
	
	/**
	 * Return the values of feature from all spots, for reuse somewhere else. Should
	 * be called only after the spots field has been set by {@link #setSpots(Collection)}.
	 */
	public EnumMap<Feature, double[]> getFeatureValues() {
		return featureValues;
	}
	
	
	/**
	 * Called when one of the {@link ThresholdPanel} is changed by the user.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		featureThresholds = new ArrayList<FeatureThreshold>(thresholdPanels.size());
		for (ThresholdPanel<Feature> tp : thresholdPanels) {
			featureThresholds.add(new FeatureThreshold(tp.getKey(), new Float(tp.getThreshold()), tp.isAboveThreshold()));
		}
		fireThresholdChanged(e);
	}

	/**
	 * Return the thresholds currently set by this GUI.
	 */
	public List<FeatureThreshold> getFeatureThresholds() {
		return featureThresholds;
	}
	
	
	/**
	 * Return the feature selected in the "Set color by feature" comb-box. 
	 * Return <code>null</code> if the item "Default" is selected.
	 */
	public Feature getColorByFeature() {
		return jPanelSpotColorGUI.setColorByFeature;
	}
	
	/**
	 * Add an {@link ChangeListener} to this panel. The {@link ChangeListener} will
	 * be notified when a change happens to the thresholds displayed by this panel, whether
	 * due to the slider being move, the auto-threshold button being pressed, or
	 * the combo-box selection being changed.
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	
	/**
	 * Remove a ChangeListener from this panel. 
	 * @return true if the listener was in listener collection of this instance.
	 */
	public boolean removeChangeListener(ChangeListener listener) {
		return changeListeners.remove(listener);
	}
	
	public Collection<ChangeListener> getChangeListeners() {
		return changeListeners;
	}
	
	/*
	 * PRIVATE METHODS
	 */
		
	private void fireThresholdChanged(ChangeEvent e) {
		for (ChangeListener cl : changeListeners) 
			cl.stateChanged(e);
	}
	
	private void prepareDataArrays() {
		if (null == spots)
			return;
		int index;
		Float val;
		boolean noDataFlag = true;
		// Get the total quantity of spot we have
		int spotNumber = 0;
		for(Collection<? extends Spot> collection : spots)
			spotNumber += collection.size();
		
		for(Feature feature : Feature.values()) {
			// Make a double array to comply to JFreeChart histograms
			double[] values = new double[spotNumber];
			index = 0;
			for(Collection<? extends Spot> collection : spots) {
				for (Spot spot : collection) {
					val = spot.getFeature(feature);
					if (null == val)
						continue;
					values[index] = val; 
					index++;
					noDataFlag = false;
				}
				if (noDataFlag)
					featureValues.put(feature, null);
				else 
					featureValues.put(feature, values);
			}
		}
	}
	
	
	public void addThresholdPanel() {
		addThresholdPanel(Feature.values()[newFeatureIndex]);		
	}
	
	public void addThresholdPanel(Feature feature) {
		if (null == featureValues)
			return;
		ThresholdPanel<Feature> tp = new ThresholdPanel<Feature>(featureValues, feature);
		tp.addChangeListener(this);
		newFeatureIndex++;
		if (newFeatureIndex >= Feature.values().length) 
			newFeatureIndex = 0;
		Component strut = Box.createVerticalStrut(5);
		struts.push(strut);
		thresholdPanels.push(tp);
		jPanelAllThresholds.add(tp);
		jPanelAllThresholds.add(strut);
		jPanelAllThresholds.revalidate();
		stateChanged(CHANGE_EVENT);
	}
	
	private void removeThresholdPanel() {
		try {
			ThresholdPanel<Feature> tp = thresholdPanels.pop();
			tp.addChangeListener(this);
			Component strut = struts.pop();
			jPanelAllThresholds.remove(strut);
			jPanelAllThresholds.remove(tp);
			jPanelAllThresholds.repaint();
			fireThresholdChanged(CHANGE_EVENT);
		} catch (EmptyStackException ese) {	}
	}
	
	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(270, 500));
			{
				jScrollPaneThresholds = new JScrollPane();
				this.add(jScrollPaneThresholds, BorderLayout.CENTER);
				jScrollPaneThresholds.setPreferredSize(new java.awt.Dimension(250, 389));
				jScrollPaneThresholds.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				jScrollPaneThresholds.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				{
					jPanelAllThresholds = new JPanel();
					BoxLayout jPanelAllThresholdsLayout = new BoxLayout(jPanelAllThresholds, BoxLayout.Y_AXIS);
					jPanelAllThresholds.setLayout(jPanelAllThresholdsLayout);
					jScrollPaneThresholds.setViewportView(jPanelAllThresholds);
				}
			}
			{
				jPanelBottom = new JPanel();
				BorderLayout jPanelBottomLayout = new BorderLayout();
				jPanelBottom.setLayout(jPanelBottomLayout);
				this.add(jPanelBottom, BorderLayout.SOUTH);
				jPanelBottom.setPreferredSize(new java.awt.Dimension(270, 71));
				{
					jPanelButtons = new JPanel();
					jPanelBottom.add(jPanelButtons, BorderLayout.NORTH);
					BoxLayout jPanelButtonsLayout = new BoxLayout(jPanelButtons, javax.swing.BoxLayout.X_AXIS);
					jPanelButtons.setLayout(jPanelButtonsLayout);
					jPanelButtons.setPreferredSize(new java.awt.Dimension(270, 22));
					jPanelButtons.setSize(270, 25);
					jPanelButtons.setMaximumSize(new java.awt.Dimension(32767, 25));
					{
						jPanelButtons.add(Box.createHorizontalStrut(5));
						jButtonAddThreshold = new JButton();
						jPanelButtons.add(jButtonAddThreshold);
						jButtonAddThreshold.setIcon(new ImageIcon(getClass().getResource(ADD_ICON)));
						jButtonAddThreshold.setFont(SMALL_FONT);
						jButtonAddThreshold.setPreferredSize(new java.awt.Dimension(24, 24));
						jButtonAddThreshold.setSize(24, 24);
						jButtonAddThreshold.setMinimumSize(new java.awt.Dimension(24, 24));
						jButtonAddThreshold.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								addThresholdPanel();
							}
						});
					}
					{
						jPanelButtons.add(Box.createHorizontalStrut(5));
						jButtonRemoveThreshold = new JButton();
						jPanelButtons.add(jButtonRemoveThreshold);
						jButtonRemoveThreshold.setIcon(new ImageIcon(getClass().getResource(REMOVE_ICON)));
						jButtonRemoveThreshold.setFont(SMALL_FONT);
						jButtonRemoveThreshold.setPreferredSize(new java.awt.Dimension(24, 24));
						jButtonRemoveThreshold.setSize(24, 24);
						jButtonRemoveThreshold.setMinimumSize(new java.awt.Dimension(24, 24));
						jButtonRemoveThreshold.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								removeThresholdPanel();
							}
						});
						jPanelButtons.add(Box.createHorizontalGlue());
					}
				}
				{
					jPanelSpotColorGUI = new JPanelSpotColorGUI(this);
					COLOR_FEATURE_CHANGED = jPanelSpotColorGUI.COLOR_FEATURE_CHANGED;
					jPanelSpotColorGUI.featureValues = featureValues;
					jPanelBottom.add(jPanelSpotColorGUI, BorderLayout.CENTER);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * MAIN METHOD
	 */
	
	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	 * @throws IOException 
	*/
	public static void main(String[] args) throws IOException {
		// Generate fake Spot data
		final int NSPOT = 100;
		final Random rn = new Random();
		ArrayList<Spot> spots = new ArrayList<Spot>(NSPOT);
		Spot spot;
		for (int i = 0; i < NSPOT; i++) {
			spot = new SpotImp(new float[] {0, 0, 0});
			for (Feature feature : Feature.values())
				spot.putFeature(feature, (float) (rn.nextGaussian()+5));
			spots.add(spot);
		}
		
		// Generate GUI
		ThresholdGuiPanel gui = new ThresholdGuiPanel();
		JFrame frame = new JFrame();
		frame.getContentPane().add(gui);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		System.out.println("Type <Enter> to ad spots to this");
		System.in.read();
		List<Collection<Spot>> allSpots = new ArrayList<Collection<Spot>>();
		allSpots.add(spots);
		gui.setSpots(allSpots);
		
	}
}
