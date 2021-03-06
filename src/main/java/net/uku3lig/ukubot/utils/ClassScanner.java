package net.uku3lig.ukubot.utils;

import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.console.ConsoleCommand;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ConfigurationBuilder;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassScanner {
    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    public static Set<Command> findCommands() {
        return findSubtypes(Command.class);
    }

    public static Set<Subsystem> findSubsystems() {
        return findSubtypes(Subsystem.class);
    }

    public static Set<ConsoleCommand> findConsoleCommands() {
        return findSubtypes(ConsoleCommand.class);
    }

    public static <T> Set<T> findSubtypes(Class<T> parent) {
        return new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(parent.getPackageName()))
                .filterInputsBy(new FilterBuilder().includePackage(parent.getPackageName()))
                .setScanners(new SubTypesScanner()))
                .getSubTypesOf(parent).parallelStream()
                .filter(klass -> !(klass.isInterface() || Modifier.isAbstract(klass.getModifiers())))
                .map(klass -> {
                    try {
                        Constructor<? extends T> c;
                        try {
                            c = klass.getConstructor();
                        } catch (Exception e) {
                            c = klass.getDeclaredConstructor();
                        }
                        c.setAccessible(true);
                        return c.newInstance();
                    } catch (ExceptionInInitializerError e) {
                        logger.error("Class " + klass.getName() + " threw an exception during instantiation:");
                        e.getCause().printStackTrace();
                        return null;
                    } catch (Exception e) {
                        logger.error("Class " + klass.getName() + " does not seem to have a working constructor");
                        e.printStackTrace();
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<Class<?>> findEntities(String pkg) {
        return findAnnotations(Entity.class, pkg);
    }

    public static Set<Class<?>> findAnnotations(Class<? extends Annotation> annotation, String pkg) {
        return new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(pkg))
                .filterInputsBy(new FilterBuilder().includePackage(pkg)))
                //.setScanners(new TypeAnnotationsScanner()))
                .getTypesAnnotatedWith(annotation);
    }
}
