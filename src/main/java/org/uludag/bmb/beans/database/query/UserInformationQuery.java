package org.uludag.bmb.beans.database.query;

import org.uludag.bmb.beans.constants.Constants.TABLES;
import org.uludag.bmb.beans.database.sharing.UserInformation;
import org.uludag.bmb.factory.query.QueryFactory;

public class UserInformationQuery extends Query implements QueryFactory {

    public UserInformationQuery(String queryName) {
        super(queryName);
    }

    private final static String insert = "IF EXISTS (SELECT * FROM " + TABLES.userInformation
            + " WHERE email=?)"
            + " UPDATE " + TABLES.userInformation + " SET publicKey=? WHERE email=?"
            + " ELSE INSERT INTO " + TABLES.userInformation + " (email, publicKey) "
            + " VALUES(?,?)";
    private final static String getAll = "SELECT * FROM " + TABLES.userInformation;
    private final static String getByEmail = "SELECT * FROM " + TABLES.userInformation + " WHERE email=?";

    @Override
    public String getQuery() {
        switch (queryName) {
            case "insert":
                return insert;
            case "getAll":
                return getAll;
            case "getByEmail":
                return getByEmail;
        }
        return null;
    }

    @Override
    public Class<?> getClassType() {
        return UserInformation.class;
    }

}
