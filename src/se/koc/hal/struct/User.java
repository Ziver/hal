package se.koc.hal.struct;

import zutil.db.bean.DBBean;

/**
 * Created by Ziver on 2015-12-03.
 */
@DBBean.DBTable("user")
public class User extends DBBean{

    private String name;
    private String address;
    private boolean external;


}
