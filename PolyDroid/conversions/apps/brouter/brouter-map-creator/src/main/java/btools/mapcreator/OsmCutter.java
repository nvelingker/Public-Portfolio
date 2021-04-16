/**
 * This program
 * - reads an *.osm from stdin
 * - writes 45*30 degree node tiles + a way file + a rel file
 *
 * @author ab
 */
package btools.mapcreator;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import btools.expressions.BExpressionContextNode;
import btools.expressions.BExpressionContextWay;
import btools.expressions.BExpressionMetaData;

public class OsmCutter extends MapCreatorBase
{
  private long recordCnt;
  private long nodesParsed;
  private long waysParsed;
  private long relsParsed;
  private long changesetsParsed;

  private DataOutputStream wayDos;
  private DataOutputStream cyclewayDos;
  private DataOutputStream restrictionsDos;

  public static void main(String[] args) throws Exception
  {
    System.out.println("*** OsmCutter: cut an osm map in node-tiles + a way file");
    if (args.length != 6 && args.length != 7)
    {
      System.out.println("usage: bzip2 -dc <map> | java OsmCutter <lookup-file> <out-tile-dir> <out-way-file> <out-rel-file> <out-res-file> <filter-profile>");
      System.out.println("or   : java OsmCutter <lookup-file> <out-tile-dir> <out-way-file> <out-rel-file> <out-res-file> <filter-profile> <inputfile> ");
      return;
    }

    new OsmCutter().process(
                   new File( args[0] )
                 , new File( args[1] )
                 , new File( args[2] )
                 , new File( args[3] )
                 , new File( args[4] )
                 , new File( args[5] )
                 , args.length > 6 ? new File( args[6] ) : null
                		 );
  }

  private BExpressionContextWay _expctxWay;
  private BExpressionContextNode _expctxNode;

  private BExpressionContextWay _expctxWayStat;
  private BExpressionContextNode _expctxNodeStat;

  public void process (File lookupFile, File outTileDir, File wayFile, File relFile, File resFile, File profileFile, File mapFile ) throws Exception
  {
    if ( !lookupFile.exists() )
    {
      throw new IllegalArgumentException( "lookup-file: " +  lookupFile + " does not exist" );
    }

    BExpressionMetaData meta = new BExpressionMetaData();

    _expctxWay = new BExpressionContextWay( meta );
    _expctxNode = new BExpressionContextNode( meta );
    meta.readMetaData( lookupFile );
    _expctxWay.parseFile( profileFile, "global" );

    
   _expctxWayStat = new BExpressionContextWay( null );
   _expctxNodeStat = new BExpressionContextNode( null );

    this.outTileDir = outTileDir;
    if ( !outTileDir.isDirectory() ) throw new RuntimeException( "out tile directory " + outTileDir + " does not exist" );

    wayDos = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( wayFile ) ) );
    cyclewayDos = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( relFile ) ) );
    restrictionsDos = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( resFile ) ) );

    // read the osm map into memory
    long t0 = System.currentTimeMillis();
    new OsmParser().readMap( mapFile, this, this, this );
    long t1 = System.currentTimeMillis();
    
    System.out.println( "parsing time (ms) =" + (t1-t0) );

    // close all files
    closeTileOutStreams();
    wayDos.close();
    cyclewayDos.close();
    restrictionsDos.close();

//    System.out.println( "-------- way-statistics -------- " );
//    _expctxWayStat.dumpStatistics();
//    System.out.println( "-------- node-statistics -------- " );
//    _expctxNodeStat.dumpStatistics();

    System.out.println( statsLine() );
  }

  private void checkStats()
  {
    if ( (++recordCnt % 100000) == 0 ) System.out.println( statsLine() );
  }

  private String statsLine()
  {
    return "records read: " + recordCnt + " nodes=" + nodesParsed + " ways=" + waysParsed + " rels=" + relsParsed + " changesets=" + changesetsParsed;
  }


  @Override
  public void nextNode( NodeData n ) throws Exception
  {
    nodesParsed++;
    checkStats();

    if ( n.getTagsOrNull() != null )
    {
      int[] lookupData = _expctxNode.createNewLookupData();
      for( String key : n.getTagsOrNull().keySet() )
      {
        String value = n.getTag( key );
        _expctxNode.addLookupValue( key, value, lookupData );
        _expctxNodeStat.addLookupValue( key, value, null );
      }
      n.description = _expctxNode.encode(lookupData);
    }
    // write node to file
    int tileIndex = getTileIndex( n.ilon, n.ilat );
    if ( tileIndex >= 0 )
    {
      n.writeTo( getOutStreamForTile( tileIndex ) );
    }
  }


  private void generatePseudoTags( HashMap<String,String> map )
  {
    // add pseudo.tags for concrete:lanes and concrete:plates
  
    String concrete = null;
    for( String key : map.keySet() )
    {
      if ( "concrete".equals( key ) )
      {
        return;
      }
      if ( "surface".equals( key ) )
      {
        String value = map.get( key );
        if ( value.startsWith( "concrete:" ) )
        {
          concrete = value.substring( "concrete:".length() );
        }
      }
    }
    if ( concrete != null )
    {
      map.put( "concrete", concrete );
    }
  }


  @Override
  public void nextWay( WayData w ) throws Exception
  {
    waysParsed++;
    checkStats();

    // encode tags
    if ( w.getTagsOrNull() == null ) return;

    generatePseudoTags( w.getTagsOrNull() );

    int[] lookupData = _expctxWay.createNewLookupData();
    for( String key : w.getTagsOrNull().keySet() )
    {
      String value = w.getTag( key );
      _expctxWay.addLookupValue( key, value.replace( ' ', '_' ), lookupData );
      _expctxWayStat.addLookupValue( key, value, null );
    }
    w.description = _expctxWay.encode(lookupData);
    
    if ( w.description == null ) return;

    // filter according to profile
    _expctxWay.evaluate( false, w.description );
    boolean ok = _expctxWay.getCostfactor() < 10000.; 
    _expctxWay.evaluate( true, w.description );
    ok |= _expctxWay.getCostfactor() < 10000.;
    if ( !ok ) return;
    
    w.writeTo( wayDos );
  }

  @Override
  public void nextRelation( RelationData r ) throws Exception
  {
    relsParsed++;
    checkStats();

    String route = r.getTag( "route" );
    // filter out non-cycle relations
    if ( route == null )
    {
      return;
    }

    String network =  r.getTag( "network" );
    if ( network == null ) network = "";
    String state =  r.getTag( "state" );
    if ( state == null ) state = "";
    writeId( cyclewayDos, r.rid );
    cyclewayDos.writeUTF( route );
    cyclewayDos.writeUTF( network );
    cyclewayDos.writeUTF( state );
    for ( int i=0; i<r.ways.size();i++ )
    {
      long wid = r.ways.get(i);
      writeId( cyclewayDos, wid );
    }
    writeId( cyclewayDos, -1 );
  }

  @Override
  public void nextRestriction( RelationData r, long fromWid, long toWid, long viaNid ) throws Exception
  {
    if ( fromWid == 0 || toWid == 0 || viaNid == 0 )
    {
      return;
    }
    String type = r.getTag( "type" );
    if ( type == null || !"restriction".equals( type ) )
    {
      return;
    }
    String restriction = r.getTag( "restriction" );
    if ( restriction == null )
    {
      return;
    }
    boolean isPositive = true;
    if ( restriction.startsWith( "no_" ) )
    {
      isPositive = false;
    }
    else if ( !restriction.startsWith( "only_" ) )
    {
      return;
    }

    short exceptions = 0;
    String except = r.getTag( "except" );
    if ( except != null )
    {
      exceptions |= toBit( "bicycle"      , 0, except );
      exceptions |= toBit( "motorcar"     , 1, except );
      exceptions |= toBit( "agricultural" , 2, except );
      exceptions |= toBit( "forestry"     , 2, except );
      exceptions |= toBit( "psv"          , 3, except );
      exceptions |= toBit( "hgv"          , 4, except );
    }

    // System.out.println( "restriction id = " + r.rid + " isPositive=" + isPositive + " fromWid = " + fromWid + " toWid = " + toWid+ " viaNid = " + viaNid );
    RestrictionData res = new RestrictionData();
    res.isPositive = isPositive;
    res.exceptions = exceptions;
    res.fromWid = fromWid;
    res.toWid = toWid;
    res.viaNid = viaNid;
    res.writeTo( restrictionsDos );
    
  }

  private static short toBit( String tag, int bitpos, String s )
  {
    return (short) ( s.indexOf( tag ) < 0 ? 0 : 1 << bitpos );
  }

  private int getTileIndex( int ilon, int ilat )
  {
     int lon = ilon / 45000000;
     int lat = ilat / 30000000;
     if ( lon < 0 || lon > 7 || lat < 0 || lat > 5 )
     {
       System.out.println( "warning: ignoring illegal pos: " + ilon + "," + ilat );
       return -1;
     }
     return lon*6 + lat;
  }

  protected String getNameForTile( int tileIndex )
  {
    int lon = (tileIndex / 6 ) * 45 - 180;
    int lat = (tileIndex % 6 ) * 30 - 90;
    String slon = lon < 0 ? "W" + (-lon) : "E" + lon;
    String slat = lat < 0 ? "S" + (-lat) : "N" + lat;
    return slon + "_" + slat + ".tls";
  }
}
