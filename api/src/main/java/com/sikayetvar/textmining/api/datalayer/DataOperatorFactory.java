package com.sikayetvar.textmining.api.datalayer;

public class DataOperatorFactory {
    public enum Databases {
        MYSQL,
        REDIS
    }

    public static DataOperator getDataOperator(Databases database) {
        switch (database) {
            case MYSQL:
                return MysqlDataOperator.getInstance();

            case REDIS:
                return RedisDataOperator.getInstance();

            default:
                throw new UnsupportedOperationException("Database [" + database + "] is not implemented");
        }
    }

    public static DataOperator getDataOperator(String database) {
        return getDataOperator(Databases.valueOf(database));
    }
}
