package fiji.plugin.trackmate.features.spot;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.view.HyperSliceImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin( type = SpotAnalyzerFactory.class, enabled = false )
public class SpotContrastAnalyzerFactory< T extends RealType< T > & NativeType< T >> implements SpotAnalyzerFactory< T >
{

	/*
	 * FIELDS
	 */

	/** The single feature key name that this analyzer computes. */
	public static final String KEY = "CONTRAST";

	private static final ArrayList< String > FEATURES = new ArrayList< String >( 1 );

	private static final HashMap< String, String > FEATURE_NAMES = new HashMap< String, String >( 1 );

	private static final HashMap< String, String > FEATURE_SHORT_NAMES = new HashMap< String, String >( 1 );

	private static final HashMap< String, Dimension > FEATURE_DIMENSIONS = new HashMap< String, Dimension >( 1 );

	private static final Map< String, Boolean > IS_INT = new HashMap< String, Boolean >( 1 );
	static
	{
		FEATURES.add( KEY );
		FEATURE_NAMES.put( KEY, "Contrast" );
		FEATURE_SHORT_NAMES.put( KEY, "Contrast" );
		FEATURE_DIMENSIONS.put( KEY, Dimension.NONE );
		IS_INT.put( KEY, Boolean.FALSE );
	}

	/*
	 * METHODS
	 */

	@Override
	public final SpotContrastAnalyzer< T > getAnalyzer( final Model model, final ImgPlus< T > img, final int frame, final int channel )
	{
		final ImgPlus< T > imgC = HyperSliceImgPlus.fixChannelAxis( img, channel );
		final ImgPlus< T > imgCT = HyperSliceImgPlus.fixTimeAxis( imgC, frame );
		final Iterator< Spot > spots = model.getSpots().iterator( frame, false );
		return new SpotContrastAnalyzer< T >( imgCT, spots );
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public List< String > getFeatures()
	{
		return FEATURES;
	}

	@Override
	public Map< String, String > getFeatureShortNames()
	{
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map< String, String > getFeatureNames()
	{
		return FEATURE_NAMES;
	}

	@Override
	public Map< String, Dimension > getFeatureDimensions()
	{
		return FEATURE_DIMENSIONS;
	}

	@Override
	public String getInfoText()
	{
		return null;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return KEY;
	}

	@Override
	public Map< String, Boolean > getIsIntFeature()
	{
		return IS_INT;
	}

	@Override
	public boolean isManualFeature()
	{
		return false;
	}

}
