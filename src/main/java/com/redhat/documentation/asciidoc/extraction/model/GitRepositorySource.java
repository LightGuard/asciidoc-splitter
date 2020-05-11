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

public class GitRepositorySource implements Source {
    private final String repositoryUrl;
    private final String branch;

    public GitRepositorySource(String repositoryUrl, String branch) {
        this.repositoryUrl = repositoryUrl;
        this.branch = branch;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

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
        GitRepositorySource that = (GitRepositorySource) o;
        return Objects.equals(repositoryUrl, that.repositoryUrl) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryUrl, branch);
    }

    @Override
    public String toString() {
        return "GitRepositorySource{" +
                "repositoryUrl='" + repositoryUrl + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }

    public Path getDirectoryPath() {
        try {
            var tmp = Files.createTempDirectory("asciidoc-splitter");
            TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));
            return Git.cloneRepository().setProgressMonitor(consoleProgressMonitor)
                    .setURI(this.repositoryUrl)
                    .setBranchesToClone(List.of("refs/heads/" + this.branch))
                    .setBranch("refs/heads/" + this.branch)
                    .setDirectory(tmp.toFile())
                    .call().getRepository().getDirectory().toPath().getParent().resolve("doc-content/kogito-docs/src/main/asciidoc/");

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
