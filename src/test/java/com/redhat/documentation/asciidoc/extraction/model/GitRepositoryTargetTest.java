package com.redhat.documentation.asciidoc.extraction.model;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GitRepositoryTargetTest {
    private static final String REPO_URL = "https://github.com/manaswinidas/Docs-symlink";
    public static final String BRANCH = "test";
    private final GitRepositoryTarget target = new GitRepositoryTarget(REPO_URL, BRANCH);

    @Test
    void getDirectoryPathShouldReturnDirectoryOfClonedRepo() throws IOException {
        var repository = new RepositoryBuilder().setWorkTree(target.getDirectoryPath().toFile()).build();
        assertThat(repository.getConfig().getString("remote", "origin", "url")).isEqualTo(REPO_URL);

        assertThat(Files.isDirectory(target.getDirectoryPath().resolve(".git"))).isTrue();
    }


    @Test
    void pushShouldCommitAndPushFilesToGitRepo() throws IOException, GitAPIException {
        final String file = "TEST_FILE.txt";
        final String contents = "test";
        Files.writeString(target.getDirectoryPath().resolve(file), contents);
        target.push();

        var tmp = Files.createTempDirectory("extraction-runner-tests");
        Git.cloneRepository()
                .setURI(REPO_URL)
                .setBranch(BRANCH)
                .setDirectory(tmp.toFile())
                .call();

        Path filePath = tmp.resolve(file);
        assertThat(Files.isRegularFile(filePath)).isTrue();
        assertThat(Files.readString(filePath)).isEqualTo(contents);
    }
}