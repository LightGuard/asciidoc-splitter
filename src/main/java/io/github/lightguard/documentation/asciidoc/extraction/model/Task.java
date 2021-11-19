package io.github.lightguard.documentation.asciidoc.extraction.model;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Task to handle multiple location and pushable location types.
 */
public class Task {
    private final Location location;
    private final PushableLocation pushableLocation;
    private final Map<String, Object> attributes;
    private final List<File> ignoreFiles;
    private final boolean pv2;

    public Task(Location location, PushableLocation pushableLocation) {
        this(location, pushableLocation, Collections.emptyMap(), Collections.emptyList(), false);
    }

    public Task(Location location, PushableLocation pushableLocation, Map<String, Object> attributes, List<File> ignoreFiles, boolean pv2) {
        this.location = location;
        this.pushableLocation = pushableLocation;
        this.attributes = Objects.isNull(attributes) ? Collections.emptyMap() : attributes;
        this.ignoreFiles = Objects.isNull(ignoreFiles) ? Collections.emptyList() : ignoreFiles;
        this.pv2 = pv2;
    }

    /**
     * Gets location
     * @return location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets pushable location
     * @return pushable location
     */
    public PushableLocation getPushableLocation() {
        return pushableLocation;
    }


    public Map<String, Object> getAttributes() {
        return Map.copyOf(attributes);
    }

    public List<File> getIgnoreFiles() {
        return List.copyOf(ignoreFiles);
    }

    public boolean isPv2() {
        return pv2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equals(location, task.location) &&
               Objects.equals(pushableLocation, task.pushableLocation) &&
               Objects.equals(pv2, task.pv2) &&
               Objects.equals(attributes, task.attributes) && Objects.equals(ignoreFiles, task.ignoreFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, pushableLocation, attributes, ignoreFiles, pv2);
    }

    @Override
    public String toString() {
        return "Task{" +
               "location=" + location +
               ", pushableLocation=" + pushableLocation +
               ", attributes=" + attributes +
               ", ignoreFiles=" + ignoreFiles +
               ", pv2=" + pv2 +
               '}';
    }
}
