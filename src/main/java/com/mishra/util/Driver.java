package com.mishra.util;

import com.mishra.cgdata.CGMData;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Test com.mishra.util.Driver for the ORM
 * Created by Scott on 1/20/2016.
 */
public class Driver {
    List<CGMData> cgData = new ArrayList<CGMData>();
    Session session;
    SessionFactory sessionFactory;
    Connection conn;

    public Driver() throws Exception {
        //create connection
        System.out.println("connection made");
        setUp();
    }

    protected void setUp() throws Exception {
        System.out.println("Set Up Method Start");
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        System.out.println("creating session factory");
        try {
            sessionFactory = new MetadataSources( registry )
                    .buildMetadata().buildSessionFactory();
            System.out.println("Finished session factory creation");
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            e.printStackTrace();
            StandardServiceRegistryBuilder.destroy( registry );
            System.out.println("couldn't create session factory");
        }

    }

    public void close(){
        sessionFactory.close();
    }

    public void addData(CGMData data){
        cgData.add(data);
    }

    public Session openSession(){
        return sessionFactory.openSession();
    }

    public void getAllData(Session session){
        session.beginTransaction();
        List data = session.createQuery("From CGMData").list();
        for (Object aData : data) {
            CGMData cgmData = (CGMData) aData;
            addData(cgmData);
        }
    }

    public List<CGMData> getCgData() {
        return cgData;
    }

    public static void main(String[] args) {
        System.out.println("Staring the one to one mapping");
        Driver driver = null;
        try {
            driver = new Driver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Opening the session");
        Session session = driver.openSession();
        if(session != null) {
            driver.getAllData(session);
        }
        else{
            System.out.println("Session was null");
            driver.close();
            System.exit(1);
        }

        System.out.println("Total data points: " + driver.getCgData().size());

        driver.close();
    }
}
