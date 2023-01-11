package se.hal.struct;

import se.hal.page.api.AlertApiEndpoint;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.parser.DataNode;
import zutil.ui.UserMessageManager.UserMessage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Class represents a physical room.
 */
public class Room extends DBBean {

    private transient UserMessage roomAlert;

    private String name = "";

    @DBColumn("map_x")
    private double mapX = 0;
    @DBColumn("map_y")
    private double mapY = 0;
    @DBColumn("map_width")
    private double mapWidth = 10.0;
    @DBColumn("map_height")
    private double mapHeight = 10.0;

    public static List<Room> getRooms(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM room" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Room.class, db) );
    }

    public static Room getRoom(DBConnection db, int id) throws SQLException {
        return DBBean.load(db, Room.class, id);
    }


    public UserMessage getRoomAlert() {
        return roomAlert;
    }
    public void clearRoomAlert() {
        setRoomAlert(null);
    }
    public void setRoomAlert(UserMessage roomAlert) {
        this.roomAlert = roomAlert;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getMapX() {
        return mapX;
    }
    public double getMapY() {
        return mapY;
    }
    public double getMapWidth() {
        return mapWidth;
    }
    public double getMapHeight() {
        return mapHeight;
    }

    public void setMapCoordinates(double x, double y, double width, double height) {
        this.mapX = x;
        this.mapY = y;
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public DataNode getDataNode() {
        DataNode rootNode = new DataNode(DataNode.DataType.Map);
        rootNode.set("id", getId());
        rootNode.set("name", getName());

        DataNode mapNode = rootNode.set("map", DataNode.DataType.Map);
        mapNode.set("x", getMapX());
        mapNode.set("y", getMapY());
        mapNode.set("width", getMapWidth());
        mapNode.set("height", getMapHeight());

        if (roomAlert != null) {
            DataNode alertNode = AlertApiEndpoint.getUserMessageDataNode(roomAlert);
            rootNode.set("alert", alertNode);
        }

        return rootNode;
    }
}
