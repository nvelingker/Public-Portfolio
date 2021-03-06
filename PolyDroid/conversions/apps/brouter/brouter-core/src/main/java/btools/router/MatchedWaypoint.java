/**
 * Information on matched way point
 *
 * @author ab
 */
package btools.router;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import btools.mapaccess.OsmNode;

final class MatchedWaypoint
{
  public OsmNode node1;
  public OsmNode node2;
  public OsmNodeNamed crosspoint;
  public OsmNodeNamed waypoint;
  public double radius;
  public boolean hasUpdate;

  public void writeToStream( DataOutput dos ) throws IOException
  {
    dos.writeInt( node1.ilat );
    dos.writeInt( node1.ilon );
    dos.writeInt( node2.ilat );
    dos.writeInt( node2.ilon );
    dos.writeInt( crosspoint.ilat );
    dos.writeInt( crosspoint.ilon );
    dos.writeInt( waypoint.ilat );
    dos.writeInt( waypoint.ilon );
    dos.writeDouble( radius );
  }

  public static MatchedWaypoint readFromStream( DataInput dis ) throws IOException
  {
	MatchedWaypoint mwp = new MatchedWaypoint();
	mwp.node1 = new OsmNode();
	mwp.node2 = new OsmNode();
	mwp.crosspoint = new OsmNodeNamed();
	mwp.waypoint = new OsmNodeNamed();
	
	mwp.node1.ilat = dis.readInt();
	mwp.node1.ilon = dis.readInt();
	mwp.node2.ilat = dis.readInt();
	mwp.node2.ilon = dis.readInt();
	mwp.crosspoint.ilat = dis.readInt();
	mwp.crosspoint.ilon = dis.readInt();
	mwp.waypoint.ilat = dis.readInt();
	mwp.waypoint.ilon = dis.readInt();
	mwp.radius = dis.readDouble();
	return mwp;
  }

}
