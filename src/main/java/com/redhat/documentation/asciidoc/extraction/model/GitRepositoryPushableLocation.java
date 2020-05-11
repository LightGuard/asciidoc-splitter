package com.redhat.documentation.asciidoc.extraction.model;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Handles git repository target locations
 */
public class GitRepositoryPushableLocation implements PushableLocation {
    private final String url;
    private final String branch;
    private final String username;
    private final char[] password;
    private Path dirPath;

    public GitRepositoryPushableLocation(String url, String branch, String username, char[] password) {

        this.url = url;
        this.branch = branch;
    }

    /**
     * Gets pushable location repository URL
     * @return pushable location repository URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets pushable location branch
     * @return pushable location branch
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
        GitRepositoryPushableLocation that = (GitRepositoryPushableLocation) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, branch);
    }

    @Override
    public String toString() {
        return "GitRepositoryPushableLocation{" +
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

    /**
     * Gets path to the cloned pushable location
     */
    private void cloneRepository() {
        try {
            var tmp = Files.createTempDirectory("asciidoc-splitter");

            this.dirPath = Git.cloneRepository()
                    .setURI(this.url)
                    .setBranchesToClone(List.of("refs/heads/" + this.branch))
                    .setBranch("refs/heads/" + this.branch)
                    .setDirectory(tmp.toFile())
                    .call().getRepository().getDirectory().toPath().getParent();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds, commits and pushes to the pushable location
     */
    @Override
    public void push() {
        try (Git git = Git.open(dirPath.toFile())) {
            git.add().addFilepattern(".").call();

            git.commit().setMessage("commit message").call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
