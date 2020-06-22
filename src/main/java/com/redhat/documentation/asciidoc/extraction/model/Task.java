package com.redhat.documentation.asciidoc.extraction.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Task to handle multiple location and pushable location types.
 */
public class Task {
    private final Location location;
    private final PushableLocation pushableLocation;
    private final Map<String, Object> attributes;

    public Task(Location location, PushableLocation pushableLocation) {
        this(location, pushableLocation, Collections.emptyMap());
    }

    public Task(Location location, PushableLocation pushableLocation, Map<String, Object> attributes) {
        this.location = location;
        this.pushableLocation = pushableLocation;
        this.attributes = Objects.isNull(attributes) ? Collections.emptyMap() : attributes;
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
               Objects.equals(attributes, task.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, pushableLocation, attributes);
    }

    @Override
    public String toString() {
        return "Task{" +
                "location=" + location +
                ", pushableLocation=" + pushableLocation +
                ", attributes=" + attributes +
                '}';
    }
}
