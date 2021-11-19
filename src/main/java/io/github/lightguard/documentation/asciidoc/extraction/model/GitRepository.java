package io.github.lightguard.documentation.asciidoc.extraction.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * A Git Repo location.
 */
public class GitRepository implements PushableLocation, AutoCloseable {
    private final String url;
    private final String branch;
    private final String username;
    private final String password;
    private final boolean willPush;
    private Path dirPath;
    private Git gitRepo;

    public GitRepository(String url, String branch, boolean willPush) {
        this(url, branch, "", "", willPush);
    }

    public GitRepository(String url, boolean willPush) {
        this(url, "", "", "", willPush);
    }

    public GitRepository(String url, String branch, String username, String password, boolean willPush) {
        Objects.requireNonNull(url, "URL required");

        this.url = url;
        this.branch = Objects.requireNonNullElse(branch, "");
        this.username = Objects.requireNonNullElse(username, "");
        this.password = Objects.requireNonNullElse(password, "");
        this.willPush = willPush;
    }

    /**
     * Gets pushable location repository URL
     *
     * @return pushable location repository URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets pushable location branch
     *
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
        GitRepository that = (GitRepository) o;
        return Objects.equals(url, that.url) &&
               Objects.equals(branch, that.branch) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, branch, username, password);
    }

    @Override
    public String toString() {
        return "GitRepository{" +
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
            System.out.println("Cloning repository...");

            var uri = new URIish(url);

            var tmp = Files.createTempDirectory("asciidoc-splitter-cloned-repo");
            var cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(this.url);

            // If we have a branch, use it
            if (!branch.isBlank() && !willPush) {
                cloneCommand.setBranchesToClone(List.of("refs/heads/" + this.branch))
                        .setBranch("refs/heads/" + this.branch);
            }

            cloneCommand.setDirectory(tmp.toFile())
                    .setProgressMonitor(new TextProgressMonitor());

            // If we have a username and password for cloning, use it
            if (!username.isBlank() && !password.isBlank())
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));

            gitRepo = cloneCommand.call();
            dirPath = gitRepo.getRepository().getDirectory().toPath().getParent();
        } catch (IOException | GitAPIException e) {
            System.out.println("Error executing git commands: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            System.err.println("Error with url: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds, commits and pushes to the pushable location
     */
    @Override
    public void push() {
        if (Objects.isNull(dirPath)) {
            cloneRepository();
        }

        try {
            var branchExists = gitRepo.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().parallelStream()
                    .anyMatch(ref -> ref.getName().equals(branch));

            var checkOutCommand = gitRepo.checkout().setName(branch);
            // Create the branch if it doesn't exist
            if (!branchExists && willPush) {
                checkOutCommand.setCreateBranch(true).setOrphan(true);
            }
            checkOutCommand.call();

            gitRepo.add().addFilepattern(".").call();
            gitRepo.commit().setMessage("Performed asciidoc split").call();

            var pushCommand = gitRepo.push();

            if (!username.isBlank() && !password.isBlank())
                pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));

            pushCommand.call();
        } catch (GitAPIException e) {
            System.out.println("Error executing git commands: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (Objects.isNull(gitRepo))
            return;

        this.gitRepo.close();
    }
}
