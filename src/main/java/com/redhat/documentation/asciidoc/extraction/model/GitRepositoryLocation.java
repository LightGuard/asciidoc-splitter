package com.redhat.documentation.asciidoc.extraction.model;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Handles git repository source locations
 */
public class GitRepositoryLocation implements Location {
    private static final String ASCIIDOC = "doc-content/kogito-docs/src/main/asciidoc";
    private final String repositoryUrl;
    private final String branch;

    public GitRepositoryLocation(String repositoryUrl, String branch) {
        this.repositoryUrl = repositoryUrl;
        this.branch = branch;
    }

    /**
     * Gets location repository URL
     * @return location repository URL
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    /**
     * Gets location branch
     * @return location branch
     */
    public String getBranch() {
        return branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GitRepositoryLocation that = (GitRepositoryLocation) o;
        return Objects.equals(repositoryUrl, that.repositoryUrl) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryUrl, branch);
    }

    @Override
    public String toString() {
        return "GitRepositoryLocation{" +
                "repositoryUrl='" + repositoryUrl + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }

    /**
     * Gets location to source folder inside cloned repository
     * @return source folder to split inside cloned repository
     */
    public Path getDirectoryPath() {
        try {
            var tmp = Files.createTempDirectory("asciidoc-splitter");
            TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));
            return Git.cloneRepository().setProgressMonitor(consoleProgressMonitor)
                    .setURI(this.repositoryUrl)
                    .setBranchesToClone(List.of("refs/heads/" + this.branch))
                    .setBranch("refs/heads/" + this.branch)
                    .setDirectory(tmp.toFile())
                    .call().getRepository().getDirectory().toPath().getParent().resolve(ASCIIDOC);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
