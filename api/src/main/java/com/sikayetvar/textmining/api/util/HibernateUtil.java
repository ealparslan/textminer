package com.sikayetvar.textmining.api.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public enum HibernateUtil {

    INSTANCE;
    private static SessionFactory sessionFactory = null;

    private synchronized SessionFactory initialiseSessionFactory() {

        if (sessionFactory == null) {
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            configuration.getProperties().setProperty("hibernate.connection.url", com.sikayetvar.textmining.api.util.Configuration.DATABASE_CONNECTION_URL);
            configuration.getProperties().setProperty("hibernate.connection.username", com.sikayetvar.textmining.api.util.Configuration.DATABASE_USERNAME);
            configuration.getProperties().setProperty("hibernate.connection.password", com.sikayetvar.textmining.api.util.Configuration.DATABASE_PASSWORD);
            sessionFactory = configuration.buildSessionFactory();
        }
        return sessionFactory;
    }

    public Session getSession() {
        Session hibernateSession;

        if (sessionFactory == null) {
            hibernateSession = initialiseSessionFactory().openSession();

        } else {
            hibernateSession = sessionFactory.openSession();
        }
        return hibernateSession;
    }

    public static void destroy() {
        sessionFactory.close();
    }
}