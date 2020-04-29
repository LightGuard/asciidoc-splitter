package com.redhat.documentation.asciidoc.extraction.model;

import java.util.Objects;

public class Task {
    private final Source source;
    private final Target target;

    public Task(Source source, Target target) {
        this.source = source;
        this.target = target;
    }

    public Source getSource() {
        return source;
    }

    public Target getTarget() {
        return target;
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
        return Objects.equals(source, task.source) &&
                Objects.equals(target, task.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return "Task{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }
}
