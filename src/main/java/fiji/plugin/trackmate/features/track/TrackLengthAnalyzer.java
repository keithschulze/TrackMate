package fiji.plugin.trackmate.features.track;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.ImageIcon;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.multithreading.SimpleMultiThreading;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.FeatureModel;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;


@Plugin( type = TrackAnalyzer.class )
public class TrackLengthAnalyzer implements TrackAnalyzer, MultiThreaded {

    public static final String KEY = "Length";

    public static final String TRACK_LENGTH = "TRACK_LENGTH";

    public static final String CONFINEMENT_INDEX = "CONFINEMENT_INDEX";

    public static final List<String> FEATURES = new ArrayList<String>(2);

    public static final Map<String, String> FEATURE_NAMES = new HashMap<String, String>(2);

    public static final Map<String, String> FEATURE_SHORT_NAMES = new HashMap<String, String>(2);

    public static final Map<String, Dimension> FEATURE_DIMENSIONS = new HashMap<String, Dimension>(2);

    public static final Map< String, Boolean > IS_INT = new HashMap< String, Boolean >( 2 );

    static {
        FEATURES.add(TRACK_LENGTH);
        FEATURES.add(CONFINEMENT_INDEX);

        FEATURE_NAMES.put(TRACK_LENGTH, "Track length");
        FEATURE_NAMES.put(CONFINEMENT_INDEX, "Confinement Index");

        FEATURE_SHORT_NAMES.put(TRACK_LENGTH, "Length");
        FEATURE_SHORT_NAMES.put(CONFINEMENT_INDEX, "Confinement");

        FEATURE_DIMENSIONS.put(TRACK_LENGTH, Dimension.LENGTH);
        FEATURE_DIMENSIONS.put(CONFINEMENT_INDEX, Dimension.LENGTH);

        IS_INT.put( TRACK_LENGTH, Boolean.FALSE );
        IS_INT.put( CONFINEMENT_INDEX, Boolean.FALSE );
    }

    private int numThreads;

    private long processingTime;

    public TrackLengthAnalyzer() {
        setNumThreads();
    }

    public void process(final Collection<Integer> trackIDs, final Model model) {


        if (trackIDs.isEmpty()) {return;}

        final ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(trackIDs.size(), false, trackIDs);
        final FeatureModel fm = model.getFeatureModel();

        final Thread[] threads = SimpleMultiThreading.newThreads(numThreads);
        for (int i = 0; i < threads.length; i++) {

            threads[i] = new Thread("TrackLengthAnalyzer thread " + i) {

                public void run() {
                    Integer trackID;
                    while ((trackID = queue.poll()) != null) {
                        final Set<DefaultWeightedEdge> trackEdges = model.getTrackModel().trackEdges(trackID);
                        final Set<Spot> tracks = model.getTrackModel().trackSpots(trackID);
                        double minT = Double.POSITIVE_INFINITY;
                        double maxT = Double.NEGATIVE_INFINITY;
                        Double t;
                        Spot startSpot = null;
                        Spot endSpot = null;

                        double length = 0;

                        for (final DefaultWeightedEdge edge : trackEdges) {
                            final Spot source = model.getTrackModel().getEdgeSource(edge);
                            final Spot target = model.getTrackModel().getEdgeTarget(edge);

                            final Double dist = Math.sqrt(source.squareDistanceTo(target));
                            if (dist == null) continue;
                            length += dist;
                        }

                        for (final Spot spot : tracks) {
                            t = spot.getFeature( Spot.POSITION_T );
                            if ( t < minT )
                            {
                                minT = t;
                                startSpot = spot;
                            }
                            if ( t > maxT )
                            {
                                maxT = t;
                                endSpot = spot;
                            }
                        }

                        Double displacement = Math.sqrt(startSpot.squareDistanceTo(endSpot));
                        Double confinement = displacement/length;

                        fm.putTrackFeature(trackID, TRACK_LENGTH, length);
                        fm.putTrackFeature(trackID, CONFINEMENT_INDEX, confinement);
                    }
                }
            };
        }

        final long start = System.currentTimeMillis();
        SimpleMultiThreading.startAndJoin(threads);
        final long end = System.currentTimeMillis();
        processingTime = end - start;
    }

    public boolean isLocal() {
        return true;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public String getInfoText() {
        return null;
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getKey() {
        return KEY;
    }

    public String getName() {
        return KEY;
    }

    public List<String> getFeatures() {
        return FEATURES;
    }

    public Map<String, String> getFeatureShortNames() {
        return FEATURE_SHORT_NAMES;
    }

    public Map<String, String> getFeatureNames() {
        return FEATURE_NAMES;
    }

    public Map<String, Dimension> getFeatureDimensions() {
        return FEATURE_DIMENSIONS;
    }

    public void setNumThreads() {
        this.numThreads = Runtime.getRuntime().availableProcessors();
    }

    public void setNumThreads(final int numThreads) {
        this.numThreads = numThreads;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public Map< String, Boolean > getIsIntFeature()
    {
        return IS_INT;
    }

    public boolean isManualFeature()
    {
        return false;
    }
}