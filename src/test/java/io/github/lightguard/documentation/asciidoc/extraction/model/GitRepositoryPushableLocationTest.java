package io.github.lightguard.documentation.asciidoc.extraction.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import io.github.lightguard.documentation.asciidoc.extraction.DeletionFileVisitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.util.SystemReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitRepositoryPushableLocationTest {
    @BeforeAll
    static void clearConfig() throws IOException, ConfigInvalidException {
        SystemReader.getInstance().getUserConfig().clear();
    }

    @Test
    void testCloningARepo() throws Exception{
        var repo = new GitRepository("https://github.com/lightguard/asciidoc-splitter.git", false);
        var path = repo.getDirectoryPath();

        assertThat(path.resolve(".git")).exists();

        // clean-up
        repo.close();
        Files.walkFileTree(path, new DeletionFileVisitor());
    }

    @Test
    void testPush() throws Exception {
        // Create bare repository
        File remoteDir = File.createTempFile("remote", "");
        remoteDir.delete();
        remoteDir.mkdirs();
        Git origin = Git.init().setDirectory(remoteDir).call();

        // Seed repo so there is a HEAD
        Files.writeString(remoteDir.toPath().resolve("first.txt"), "First file", StandardOpenOption.CREATE_NEW);
        origin.add().addFilepattern("*.txt").call();
        origin.getRepository().getConfig().clear();
        origin.commit().setMessage("Seeding the repo").call();

        var branchName = "new-branch";
        var gitRepo = new GitRepository(origin.getRepository().getDirectory().toString(), branchName, true);
        var gitRepoDir = gitRepo.getDirectoryPath();
        assertThat(gitRepoDir.resolve(".git")).exists();

        // Make a change
        Files.writeString(gitRepoDir.resolve("new-file.txt"), "Hello out there!", StandardOpenOption.CREATE_NEW);
        gitRepo.push();

        // Now check to see that the origin has the new branch
        assertThat(origin.branchList().setContains(branchName).call()).hasSize(1);

        // Clean-up
        origin.close();
        Files.walkFileTree(remoteDir.toPath(), new DeletionFileVisitor());
        gitRepo.close();
        Files.walkFileTree(gitRepoDir, new DeletionFileVisitor());
    }
}
