package net.uku3lig.ukubot.subsystems;

import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.uku3lig.ukubot.utils.ClassScanner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SubsystemAdapter implements EventListener {
    @Getter
    private static final Set<Subsystem> subsystems = new HashSet<>();
    private static SubsystemAdapter instance = null;
    private static final Logger logger = LoggerFactory.getLogger(SubsystemAdapter.class);

    public static SubsystemAdapter getInstance() {
        if (instance == null) instance = new SubsystemAdapter();
        return instance;
    }

    private SubsystemAdapter() {
        subsystems.addAll(ClassScanner.findSubsystems());
        logger.info("Found %s subsystems".formatted(subsystems.size()));
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        subsystems.stream().filter(subsystem -> subsystem.enabled).forEach(s -> s.onEvent(event));
    }
}
