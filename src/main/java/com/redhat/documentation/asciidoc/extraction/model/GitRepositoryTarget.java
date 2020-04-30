package com.redhat.documentation.asciidoc.extraction.model;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class GitRepositoryTarget implements Target {
    private final String url;
    private final String branch;
    private Path dirPath;

    public GitRepositoryTarget(String url, String branch) {

        this.url = url;
        this.branch = branch;
    }

    public String getUrl() {
        return url;
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
        GitRepositoryTarget that = (GitRepositoryTarget) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, branch);
    }

    @Override
    public String toString() {
        return "GitRepositoryTarget{" +
                "outputRepo='" + url + '\'' +
                ", outputBranch='" + branch + '\'' +
                '}';
    }

    @Override
    public Path getDirectoryPath() {
        if (dirPath == null) {
            cloneRepository();
        }

        return dirPath;

    }

    private void cloneRepository() {
        try {
            var tmp = Files.createTempDirectory("asciidoc-splitter");

            this.dirPath = Git.cloneRepository()
                    .setURI(this.url)
                    .setBranchesToClone(List.of("refs/heads/" + this.branch))
                    .setBranch("refs/heads/" + this.branch)
                    .setDirectory(tmp.toFile())
                    .call().getRepository().getDirectory().toPath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void push() {

    }
}
