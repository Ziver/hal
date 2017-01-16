package se.hal.struct.dso;

import se.hal.intf.HalTrigger;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.log.LogUtil;
import zutil.parser.json.JSONParser;
import zutil.parser.json.JSONWriter;
import zutil.ui.Configurator;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A intermediate class for loading HalTrigger objects from DB
 */
public abstract class ObjectDSO<T> extends DBBean{
    private static final Logger logger = LogUtil.getLogger();

    // DB parameters
    private String type;
    private String config;

    // Local parameters
    private transient T cachedObj;



    @Override
    protected void postUpdateAction() {
        if (type != null && !type.isEmpty()) {
            if (cachedObj == null) {
                try {
                    Class clazz = Class.forName(type);
                    cachedObj = (T) clazz.newInstance();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable instantiate class: " + type, e);
                }
            }

            if (config != null && !config.isEmpty()) {
                Configurator<T> configurator = new Configurator<>(cachedObj);
                configurator.setValues(JSONParser.read(config));
                configurator.applyConfiguration();
            }
        }
    }

    @Override
    public void save(DBConnection db) throws SQLException {
        if (cachedObj == null)
            this.config = null;
        else {
            Configurator<T> configurator = new Configurator<>(cachedObj);
            this.config = JSONWriter.toString(configurator.getValuesAsNode());
        }
        super.save(db);
    }



    public T getObject(){
        return cachedObj;
    }

    public void setObject(T obj){
        this.cachedObj = obj;
    }

}
