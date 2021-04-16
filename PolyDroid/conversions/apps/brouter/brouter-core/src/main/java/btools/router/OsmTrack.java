/**
 * Container for a track
 *
 * @author ab
 */
package btools.router;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import btools.mapaccess.OsmPos;
import btools.util.CompactLongMap;
import btools.util.FrozenLongMap;

public final class OsmTrack
{
  // csv-header-line
  private static final String MESSAGES_HEADER = "Longitude\tLatitude\tElevation\tDistance\tCostPerKm\tElevCost\tTurnCost\tNodeCost\tInitialCost\tWayTags\tNodeTags";

  public MatchedWaypoint endPoint;
  public long[] nogoChecksums;
  public long profileTimestamp;
  public boolean isDirty;

  public boolean showspeed;

  private static class OsmPathElementHolder
  {
    public OsmPathElement node;
    public OsmPathElementHolder nextHolder;
  }

  public ArrayList<OsmPathElement> nodes = new ArrayList<OsmPathElement>();

  private CompactLongMap<OsmPathElementHolder> nodesMap;

  private CompactLongMap<OsmPathElementHolder> detourMap;

  private VoiceHintList voiceHints;

  public String message = null;
  public ArrayList<String> messageList = null;

  public String name = "unset";

  public void addNode( OsmPathElement node )
  {
    nodes.add( 0, node );
  }

  public void registerDetourForId( long id, OsmPathElement detour )
  {
    if ( detourMap == null )
    {
      detourMap = new CompactLongMap<OsmPathElementHolder>();
    }
    OsmPathElementHolder nh = new OsmPathElementHolder();
    nh.node = detour;
    OsmPathElementHolder h = detourMap.get( id );
    if ( h != null )
    {
      while ( h.nextHolder != null )
      {
        h = h.nextHolder;
      }
      h.nextHolder = nh;
    }
    else
    {
      detourMap.fastPut( id, nh );
    }
  }

  public void copyDetours( OsmTrack source )
  {
    detourMap = source.detourMap == null ? null : new FrozenLongMap<OsmPathElementHolder>( source.detourMap );
  }

  public void buildMap()
  {
    nodesMap = new CompactLongMap<OsmPathElementHolder>();
    for ( OsmPathElement node : nodes )
    {
      long id = node.getIdFromPos();
      OsmPathElementHolder nh = new OsmPathElementHolder();
      nh.node = node;
      OsmPathElementHolder h = nodesMap.get( id );
      if ( h != null )
      {
        while (h.nextHolder != null)
        {
          h = h.nextHolder;
        }
        h.nextHolder = nh;
      }
      else
      {
        nodesMap.fastPut( id, nh );
      }
    }
    nodesMap = new FrozenLongMap<OsmPathElementHolder>( nodesMap );
  }

  private ArrayList<String> aggregateMessages()
  {
    ArrayList<String> res = new ArrayList<String>();
    MessageData current = null;
    for ( OsmPathElement n : nodes )
    {
      if ( n.message != null && n.message.wayKeyValues != null )
      {
        MessageData md = n.message.copy();
        if ( current != null )
        {
          if ( current.nodeKeyValues != null || !current.wayKeyValues.equals( md.wayKeyValues ) )
          {
            res.add( current.toMessage() );
          }
          else
          {
            md.add( current );
          }
        }
        current = md;
      }
    }
    if ( current != null )
    {
      res.add( current.toMessage() );
    }
    return res;
  }

  /**
   * writes the track in binary-format to a file
   * 
   * @param filename
   *          the filename to write to
   */
  public void writeBinary( String filename ) throws Exception
  {
    DataOutputStream dos = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( filename ) ) );

    endPoint.writeToStream( dos );
    dos.writeInt( nodes.size() );
    for ( OsmPathElement node : nodes )
    {
      node.writeToStream( dos );
    }
    dos.writeLong( nogoChecksums[0] );
    dos.writeLong( nogoChecksums[1] );
    dos.writeLong( nogoChecksums[2] );
    dos.writeBoolean( isDirty );
    dos.writeLong( profileTimestamp );
    dos.close();
  }

  public static OsmTrack readBinary( String filename, OsmNodeNamed newEp, long[] nogoChecksums, long profileChecksum, StringBuilder debugInfo )
  {
    OsmTrack t = null;
    if ( filename != null )
    {
      File f = new File( filename );
      if ( f.exists() )
      {
        try
        {
          DataInputStream dis = new DataInputStream( new BufferedInputStream( new FileInputStream( f ) ) );
          MatchedWaypoint ep = MatchedWaypoint.readFromStream( dis );
          int dlon = ep.waypoint.ilon - newEp.ilon;
          int dlat = ep.waypoint.ilat - newEp.ilat;
          boolean targetMatch = dlon < 20 && dlon > -20 && dlat < 20 && dlat > -20;
          if ( debugInfo != null )
          {
            debugInfo.append( "target-delta = " + dlon + "/" + dlat + " targetMatch=" + targetMatch );
          }
          if ( targetMatch )
          {
            t = new OsmTrack();
            t.endPoint = ep;
            int n = dis.readInt();
            OsmPathElement last_pe = null;
            for ( int i = 0; i < n; i++ )
            {
              OsmPathElement pe = OsmPathElement.readFromStream( dis );
              pe.origin = last_pe;
              last_pe = pe;
              t.nodes.add( pe );
            }
            t.cost = last_pe.cost;
            t.buildMap();

            // check cheecksums, too
            long[] al = new long[3];
            long pchecksum = 0;
            try
            {
              al[0] = dis.readLong();
              al[1] = dis.readLong();
              al[2] = dis.readLong();
            }
            catch (EOFException eof) { /* kind of expected */ }
            try
            {
              t.isDirty = dis.readBoolean();
            }
            catch (EOFException eof) { /* kind of expected */ }
            try
            {
              pchecksum = dis.readLong();
            }
            catch (EOFException eof) { /* kind of expected */ }
            boolean nogoCheckOk = Math.abs( al[0] - nogoChecksums[0] ) <= 20
                               && Math.abs( al[1] - nogoChecksums[1] ) <= 20
                               && Math.abs( al[2] - nogoChecksums[2] ) <= 20;
            boolean profileCheckOk = pchecksum == profileChecksum;

            if ( debugInfo != null )
            {
              debugInfo.append( " nogoCheckOk=" + nogoCheckOk + " profileCheckOk=" + profileCheckOk );
              debugInfo.append( " al=" + formatLongs(al) + " nogoChecksums=" + formatLongs(nogoChecksums) );
            }
            if ( !(nogoCheckOk && profileCheckOk) ) return null;
          }
          dis.close();
        }
        catch (Exception e)
        {
          throw new RuntimeException( "Exception reading rawTrack: " + e );
        }
      }
    }
    return t;
  }

  private static String formatLongs( long[] al )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( '{' );
    for( long l : al )
    {
      sb.append( l );
      sb.append( ' ' );
    }
    sb.append( '}' );
    return sb.toString();
  }


  public void addNodes( OsmTrack t )
  {
    for ( OsmPathElement n : t.nodes )
      addNode( n );
    buildMap();
  }

  public boolean containsNode( OsmPos node )
  {
    return nodesMap.contains( node.getIdFromPos() );
  }

  public OsmPathElement getLink( long n1, long n2 )
  {
    OsmPathElementHolder h = nodesMap.get( n2 );
    while (h != null)
    {
      OsmPathElement e1 = h.node.origin;
      if ( e1 != null && e1.getIdFromPos() == n1 )
      {
        return h.node;
      }
      h = h.nextHolder;
    }
    return null;
  }

  public void appendTrack( OsmTrack t )
  {
    int ourSize = nodes.size();
    float t0 = ourSize > 0 ? nodes.get(ourSize - 1 ).getTime() : 0; 
    float e0 = ourSize > 0 ? nodes.get(ourSize - 1 ).getEnergy() : 0; 
    for ( int i = 0; i < t.nodes.size(); i++ )
    {
      if ( i > 0 || ourSize == 0 )
      {
        OsmPathElement e = t.nodes.get( i );
        e.setTime( e.getTime() + t0 );
        e.setEnergy( e.getEnergy() + e0 );
        nodes.add( e );
      }
    }

    if ( t.voiceHints != null )
    {
      if ( voiceHints == null )
      {
        voiceHints = t.voiceHints;
      }
      else
      {
        voiceHints.list.addAll( t.voiceHints.list );
      }
    }

    distance += t.distance;
    ascend += t.ascend;
    plainAscend += t.plainAscend;
    cost += t.cost;
    energy += t.energy;

    showspeed |= t.showspeed;
  }

  public int distance;
  public int ascend;
  public int plainAscend;
  public int cost;
  public int energy;

  /**
   * writes the track in gpx-format to a file
   * 
   * @param filename
   *          the filename to write to
   */
  public void writeGpx( String filename ) throws Exception
  {
    BufferedWriter bw = new BufferedWriter( new FileWriter( filename ) );
    formatAsGpx( bw );
    bw.close();
  }

  public String formatAsGpx()
  {
    try
    {
      StringWriter sw = new StringWriter( 8192 );
      BufferedWriter bw = new BufferedWriter( sw );
      formatAsGpx( bw );
      bw.close();
      return sw.toString();
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public String formatAsGpx( BufferedWriter sb ) throws IOException
  {
    int turnInstructionMode = voiceHints != null ? voiceHints.turnInstructionMode : 0;

    sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
    for ( int i = messageList.size() - 1; i >= 0; i-- )
    {
      String message = messageList.get( i );
      if ( i < messageList.size() - 1 )
        message = "(alt-index " + i + ": " + message + " )";
      if ( message != null )
        sb.append( "<!-- " ).append( message ).append( " -->\n" );
    }

    if ( turnInstructionMode == 4 ) // comment style
    {
      sb.append( "<!-- $transport-mode$").append( voiceHints.getTransportMode() ).append( "$ -->\n" );
      sb.append( "<!--          cmd    idx        lon        lat d2next  geometry -->\n" );
      sb.append( "<!-- $turn-instruction-start$\n" );
      for( VoiceHint hint: voiceHints.list )
      {
        sb.append( String.format( "     $turn$%6s;%6d;%10s;%10s;%6d;%s$\n", hint.getCommandString(), hint.indexInTrack,
                                  formatILon( hint.ilon ), formatILat( hint.ilat ), (int)(hint.distanceToNext), hint.formatGeometry() ) );
      }
      sb.append( "    $turn-instruction-end$ -->\n" );
    }
    sb.append( "<gpx \n" );
    sb.append( " xmlns=\"http://www.topografix.com/GPX/1/1\" \n" );
    sb.append( " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" );
    if ( turnInstructionMode == 2 ) // locus style
    {
      sb.append( " xmlns:locus=\"http://www.locusmap.eu\" \n" );
    }
    sb.append( " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n" );

    if ( turnInstructionMode == 3)
    {
      sb.append(" creator=\"OsmAndRouter\" version=\"1.1\">\n" );
    }
    else
    {
      sb.append( " creator=\"BRouter-1.4.11\" version=\"1.1\">\n" );
    }

    if ( turnInstructionMode == 3) // osmand style
    {
      sb.append(" <rte>\n");
      for( VoiceHint hint: voiceHints.list )
      {
        sb.append("  <rtept lat=\"").append( formatILat( hint.ilat ) ).append( "\" lon=\"" )
          .append( formatILon( hint.ilon ) ).append( "\">\n" )
          .append ( "   <desc>" ).append( hint.getMessageString() ).append( "</desc>\n   <extensions>\n   <turn>" )
          .append( hint.getCommandString() ).append("</turn>\n   <turn-angle>").append( "" + hint.angle )
          .append("</turn-angle>\n   <offset>").append( "" + hint.indexInTrack ).append("</offset>\n  </extensions>\n </rtept>\n");
      }
      sb.append("</rte>\n");
    }

    if ( turnInstructionMode == 2 ) // locus style
    {
      float lastRteTime = 0.f;

      for( VoiceHint hint: voiceHints.list )
      {
        sb.append( " <wpt lon=\"" ).append( formatILon( hint.ilon ) ).append( "\" lat=\"" )
          .append( formatILat( hint.ilat ) ).append( "\">" )
          .append( hint.selev == Short.MIN_VALUE ? "" : "<ele>" + (hint.selev / 4.) + "</ele>" )
          .append( "<name>" ).append( hint.getMessageString() ).append( "</name>" )
          .append( "<extensions><locus:rteDistance>" ).append( "" + hint.distanceToNext ).append( "</locus:rteDistance>" );
        float rteTime = hint.getTime();
        if ( rteTime != lastRteTime ) // add timing only if available
        {
          double t = rteTime - lastRteTime;
          double speed = hint.distanceToNext / t;
          sb.append( "<locus:rteTime>" ).append( "" + t ).append( "</locus:rteTime>" )
            .append( "<locus:rteSpeed>" ).append( "" + speed ).append( "</locus:rteSpeed>" );
          lastRteTime = rteTime;
        }
        sb.append( "<locus:rtePointAction>" ).append( "" + hint.getLocusAction() ).append( "</locus:rtePointAction></extensions>" )
          .append( "</wpt>\n" );
      }
    }
    if ( turnInstructionMode == 5 ) // gpsies style
    {
      for( VoiceHint hint: voiceHints.list )
      {
        sb.append( " <wpt lon=\"" ).append( formatILon( hint.ilon ) ).append( "\" lat=\"" )
          .append( formatILat( hint.ilat ) ).append( "\">" )
          .append( "<name>" ).append( hint.getMessageString() ).append( "</name>" )
          .append( "<sym>" ).append( hint.getSymbolString().toLowerCase() ).append( "</sym>" )
          .append( "<type>" ).append( hint.getSymbolString() ).append( "</type>" )
          .append( "</wpt>\n" );
      }
    }
    sb.append( " <trk>\n" );
    sb.append( "  <name>" ).append( name ).append( "</name>\n" );
    if ( turnInstructionMode == 1 ) // trkpt/sym style
    {
      sb.append( "  <type>" ).append( voiceHints.getTransportMode() ).append( "</type>\n" );
    }

    if ( turnInstructionMode == 2 )
    {
      sb.append( "  <extensions><locus:rteComputeType>" ).append( "" + voiceHints.getLocusRouteType() ).append( "</locus:rteComputeType></extensions>\n" );
      sb.append( "  <extensions><locus:rteSimpleRoundabouts>1</locus:rteSimpleRoundabouts></extensions>\n" );
    }

    sb.append( "  <trkseg>\n" );

    for ( int idx = 0; idx < nodes.size(); idx++ )
    {
      OsmPathElement n = nodes.get(idx);
      String sele = n.getSElev() == Short.MIN_VALUE ? "" : "<ele>" + n.getElev() + "</ele>";
      if ( turnInstructionMode == 1 ) // trkpt/sym style
      {
        for ( VoiceHint hint : voiceHints.list )
        {
          if ( hint.indexInTrack == idx )
          {
            sele += "<sym>" + hint.getCommandString() + "</sym>";
          }
        }
      }
      sb.append( "   <trkpt lon=\"" ).append( formatILon( n.getILon() ) ).append( "\" lat=\"" )
          .append( formatILat( n.getILat() ) ).append( "\">" ).append( sele ).append( "</trkpt>\n" );
    }

    sb.append( "  </trkseg>\n" );
    sb.append( " </trk>\n" );
    sb.append( "</gpx>\n" );

    return sb.toString();
  }

  public void writeKml( String filename ) throws Exception
  {
    BufferedWriter bw = new BufferedWriter( new FileWriter( filename ) );

    bw.write( formatAsKml() );
    bw.close();
  }

  public String formatAsKml()
  {
    StringBuilder sb = new StringBuilder( 8192 );

    sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );

    sb.append( "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" );
    sb.append( "  <Document>\n" );
    sb.append( "    <name>KML Samples</name>\n" );
    sb.append( "    <open>1</open>\n" );
    sb.append( "    <distance>3.497064</distance>\n" );
    sb.append( "    <traveltime>872</traveltime>\n" );
    sb.append( "    <description>To enable simple instructions add: 'instructions=1' as parameter to the URL</description>\n" );
    sb.append( "    <Folder>\n" );
    sb.append( "      <name>Paths</name>\n" );
    sb.append( "      <visibility>0</visibility>\n" );
    sb.append( "      <description>Examples of paths.</description>\n" );
    sb.append( "      <Placemark>\n" );
    sb.append( "        <name>Tessellated</name>\n" );
    sb.append( "        <visibility>0</visibility>\n" );
    sb.append( "        <description><![CDATA[If the <tessellate> tag has a value of 1, the line will contour to the underlying terrain]]></description>\n" );
    sb.append( "        <LineString>\n" );
    sb.append( "          <tessellate>1</tessellate>\n" );
    sb.append( "         <coordinates> " );

    for ( OsmPathElement n : nodes )
    {
      sb.append( formatILon( n.getILon() ) ).append( "," ).append( formatILat( n.getILat() ) ).append( "\n" );
    }

    sb.append( "          </coordinates>\n" );
    sb.append( "        </LineString>\n" );
    sb.append( "      </Placemark>\n" );
    sb.append( "    </Folder>\n" );
    sb.append( "  </Document>\n" );
    sb.append( "</kml>\n" );

    return sb.toString();
  }

  public List<String> iternity;

  public String formatAsGeoJson()
  {
    StringBuilder sb = new StringBuilder( 8192 );

    sb.append( "{\n" );
    sb.append( "  \"type\": \"FeatureCollection\",\n" );
    sb.append( "  \"features\": [\n" );
    sb.append( "    {\n" );
    sb.append( "      \"type\": \"Feature\",\n" );
    sb.append( "      \"properties\": {\n" );
    sb.append( "        \"creator\": \"BRouter-1.1\",\n" );
    sb.append( "        \"name\": \"" ).append( name ).append( "\",\n" );
    sb.append( "        \"track-length\": \"" ).append( distance ).append( "\",\n" );
    sb.append( "        \"filtered ascend\": \"" ).append( ascend ).append( "\",\n" );
    sb.append( "        \"plain-ascend\": \"" ).append( plainAscend ).append( "\",\n" );
    sb.append( "        \"total-time\": \"" ).append( getTotalSeconds() ).append( "\",\n" );
    sb.append( "        \"total-energy\": \"" ).append( energy ).append( "\",\n" );
    sb.append( "        \"cost\": \"" ).append( cost ).append( "\",\n" );
    sb.append( "        \"messages\": [\n" );
    sb.append( "          [\"" ).append( MESSAGES_HEADER.replaceAll( "\t", "\", \"" ) ).append( "\"],\n" );
    for ( String m : aggregateMessages() )
    {
      sb.append( "          [\"" ).append( m.replaceAll( "\t", "\", \"" ) ).append( "\"],\n" );
    }
    sb.deleteCharAt( sb.lastIndexOf( "," ) );
    sb.append( "        ]\n" );

    sb.append( "      },\n" );

    if ( iternity != null )
    {
      sb.append( "      \"iternity\": [\n" );
      for ( String s : iternity )
      {
        sb.append( "        \"" ).append( s ).append( "\",\n" );
      }
      sb.deleteCharAt( sb.lastIndexOf( "," ) );
      sb.append( "        ],\n" );
    }
    sb.append( "      \"geometry\": {\n" );
    sb.append( "        \"type\": \"LineString\",\n" );
    sb.append( "        \"coordinates\": [\n" );

    OsmPathElement nn = null;
    for ( OsmPathElement n : nodes )
    {
      String sele = n.getSElev() == Short.MIN_VALUE ? "" : ", " + n.getElev();
      if ( showspeed ) // hack: show speed instead of elevation
      {
        int speed = 0;
        if ( nn != null )
        {
          int dist = n.calcDistance( nn );
          float dt = n.getTime()-nn.getTime();
          if ( dt != 0.f )
          {
            speed = (int)((3.6f*dist)/dt + 0.5);
          }
        }
        sele = ", " + speed;
      }
      sb.append( "          [" ).append( formatILon( n.getILon() ) ).append( ", " ).append( formatILat( n.getILat() ) )
          .append( sele ).append( "],\n" );
      nn = n;
    }
    sb.deleteCharAt( sb.lastIndexOf( "," ) );

    sb.append( "        ]\n" );
    sb.append( "      }\n" );
    sb.append( "    }\n" );
    sb.append( "  ]\n" );
    sb.append( "}\n" );

    return sb.toString();
  }

  private int getTotalSeconds()
  {
    float s = nodes.size() < 2 ? 0 : nodes.get( nodes.size()-1 ).getTime() - nodes.get( 0 ).getTime();
    return (int)(s + 0.5);
  }

  public String getFormattedTime()
  {
    return format1( getTotalSeconds()/60. ) + "m";
  }

  public String getFormattedEnergy()
  {
    return format1( energy/3600000. ) + "kwh";
  }

  private static String formatILon( int ilon )
  {
    return formatPos(  ilon - 180000000 );
  }

  private static String formatILat( int ilat )
  {
    return formatPos(  ilat - 90000000 );
  }

  private static String formatPos( int p )
  {
    boolean negative = p < 0;
    if ( negative )
      p = -p;
    char[] ac = new char[12];
    int i = 11;
    while (p != 0 || i > 3)
    {
      ac[i--] = (char) ( '0' + ( p % 10 ) );
      p /= 10;
      if ( i == 5 )
        ac[i--] = '.';
    }
    if ( negative )
      ac[i--] = '-';
    return new String( ac, i + 1, 11 - i );
  }
  
  private String format1( double n )
  {
    String s = "" + (long)(n*10 + 0.5);
    int len = s.length();
    return s.substring( 0, len-1 ) + "." + s.charAt( len-1 );
  }

  public void dumpMessages( String filename, RoutingContext rc ) throws Exception
  {
    BufferedWriter bw = filename == null ? null : new BufferedWriter( new FileWriter( filename ) );
    writeMessages( bw, rc );
  }

  public void writeMessages( BufferedWriter bw, RoutingContext rc ) throws Exception
  {
    dumpLine( bw, MESSAGES_HEADER );
    for ( String m : aggregateMessages() )
    {
      dumpLine( bw, m );
    }
    if ( bw != null )
      bw.close();
  }

  private void dumpLine( BufferedWriter bw, String s ) throws Exception
  {
    if ( bw == null )
    {
      System.out.println( s );
    }
    else
    {
      bw.write( s );
      bw.write( "\n" );
    }
  }

  public void readGpx( String filename ) throws Exception
  {
    File f = new File( filename );
    if ( !f.exists() )
      return;
    BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) );

    for ( ;; )
    {
      String line = br.readLine();
      if ( line == null )
        break;

      int idx0 = line.indexOf( "<trkpt lon=\"" );
      if ( idx0 >= 0 )
      {
        idx0 += 12;
        int idx1 = line.indexOf( '"', idx0 );
        int ilon = (int) ( ( Double.parseDouble( line.substring( idx0, idx1 ) ) + 180. ) * 1000000. + 0.5 );
        int idx2 = line.indexOf( " lat=\"" );
        if ( idx2 < 0 )
          continue;
        idx2 += 6;
        int idx3 = line.indexOf( '"', idx2 );
        int ilat = (int) ( ( Double.parseDouble( line.substring( idx2, idx3 ) ) + 90. ) * 1000000. + 0.5 );
        nodes.add( OsmPathElement.create( ilon, ilat, (short) 0, null, false ) );
      }
    }
    br.close();
  }

  public boolean equalsTrack( OsmTrack t )
  {
    if ( nodes.size() != t.nodes.size() )
      return false;
    for ( int i = 0; i < nodes.size(); i++ )
    {
      OsmPathElement e1 = nodes.get( i );
      OsmPathElement e2 = t.nodes.get( i );
      if ( e1.getILon() != e2.getILon() || e1.getILat() != e2.getILat() )
        return false;
    }
    return true;
  }

  public void processVoiceHints( RoutingContext rc )
  {
    voiceHints = new VoiceHintList();
    voiceHints.setTransportMode( rc.carMode, rc.bikeMode );
    voiceHints.turnInstructionMode = rc.turnInstructionMode;

    if ( detourMap == null )
    {
      return;
    }
    int nodeNr = nodes.size() - 1;
    OsmPathElement node = nodes.get( nodeNr );
    List<VoiceHint> inputs = new ArrayList<VoiceHint>();
    while (node != null)
    {
      if ( node.origin != null )
      {
        VoiceHint input = new VoiceHint();
        inputs.add( input );
        input.ilat = node.origin.getILat();
        input.ilon = node.origin.getILon();
        input.selev = node.origin.getSElev();
        input.indexInTrack = --nodeNr;
        input.goodWay = node.message;
        input.oldWay = node.origin.message == null ? node.message : node.origin.message;

        OsmPathElementHolder detours = detourMap.get( node.origin.getIdFromPos() );
        if ( detours != null )
        {
          OsmPathElementHolder h = detours;
          while (h != null)
          {
            OsmPathElement e = h.node;
            input.addBadWay( startSection( e, node.origin ) );
            h = h.nextHolder;
          }
        }
      }
      node = node.origin;
    }

    VoiceHintProcessor vproc = new VoiceHintProcessor( rc.turnInstructionCatchingRange, rc.turnInstructionRoundabouts );
    List<VoiceHint> results = vproc.process( inputs );
    for( VoiceHint hint : results )
    {
      voiceHints.list.add( hint );
    }
  }


  private MessageData startSection( OsmPathElement element, OsmPathElement root )
  {
    OsmPathElement e = element;
    int cnt = 0;
    while( e != null && e.origin != null )
    {
      if ( e.origin.getILat() == root.getILat() && e.origin.getILon() == root.getILon() )
      {
        return e.message;
      }
      e = e.origin;
      if ( cnt++ == 1000000 )
      {
        throw new IllegalArgumentException( "ups: " + root + "->" + element );
      }
    }
    return null;
  }
}
