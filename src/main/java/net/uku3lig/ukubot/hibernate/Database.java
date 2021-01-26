package net.uku3lig.ukubot.hibernate;

import net.uku3lig.ukubot.utils.DockerSecrets;
import net.uku3lig.ukubot.utils.ClassScanner;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.io.Serializable;
import java.util.*;

public class Database {
    private static SessionFactory factory = null;
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    public static SessionFactory getFactory() {
        if (factory == null) init();
        return factory;
    }

    public static void init() {
        if (factory != null) return;

        try {
            Configuration cfg = new Configuration()
                    .configure("hibernate.cfg.xml");

            ClassScanner.findEntities("net.uku3lig.ukubot")
                    .forEach(cfg::addAnnotatedClass);

            if (DockerSecrets.getSecret("db_pwd").isPresent())
                cfg.setProperty("hibernate.connection.password", DockerSecrets.getSecret("db_pwd").get());

            final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
            factory = cfg.buildSessionFactory(registry);
        } catch (Exception e) {
            logger.error("Execption encountered while trying to read start db connection");
            e.printStackTrace();
            Runtime.getRuntime().exit(3);
        }
    }

    private static boolean isEntity(Object o) {
        return isEntity(o.getClass());
    }

    private static boolean isEntity(Class<?> klass) {
        return klass.isAnnotationPresent(Entity.class);
    }

    @SafeVarargs
    public static <T> boolean saveOrUpdate(T... entities) {
        if (entities.length == 0 || !isEntity(entities[0])) return false;
        try (Session s = factory.openSession()){
            s.beginTransaction();
            Arrays.stream(entities).forEach(s::saveOrUpdate);
            s.getTransaction().commit();
            return true;
        } catch (Exception e) {
            logger.error("An error happened while trying to saveOrUpdate a " + entities[0].getClass().getSimpleName() +
                    ": " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    @SafeVarargs
    public static <T> boolean delete(T... entities) {
        if (entities.length == 0 || !isEntity(entities[0])) return false;
        try (Session s = factory.openSession()){
            s.beginTransaction();
            Arrays.stream(entities).forEach(s::delete);
            s.getTransaction().commit();
            return true;
        } catch (Exception e) {
            logger.error("An error happened while trying to delete a " + entities[0].getClass().getSimpleName() +
                    ": " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    public static <T> Optional<T> getById(Class<T> klass, Serializable id) {
        if (!isEntity(klass)) return Optional.empty();
        try (Session s = factory.openSession()) {
            s.beginTransaction();
            Optional<T> o = Optional.ofNullable(s.get(klass, id));
            s.getTransaction().commit();
            return o;
        } catch (Exception e) {
            logger.error("An error happened while trying to get a " + klass.getSimpleName() +
                    ": " + e.getClass().getSimpleName());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> Collection<T> getAll(Class<T> klass) {
        if (!isEntity(klass)) return Collections.emptySet();
        try (Session s = factory.openSession()) {
            return s.createQuery("SELECT a FROM %s a".formatted(klass.getSimpleName()), klass).getResultList();
        }
    }
}
