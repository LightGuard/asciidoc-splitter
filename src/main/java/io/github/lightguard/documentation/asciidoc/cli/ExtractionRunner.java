package io.github.lightguard.documentation.asciidoc.cli;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import io.github.lightguard.documentation.asciidoc.extraction.Extractor;
import io.github.lightguard.documentation.asciidoc.extraction.model.GitRepository;
import io.github.lightguard.documentation.asciidoc.extraction.model.LocalDirectoryLocation;
import io.github.lightguard.documentation.asciidoc.extraction.model.Location;
import io.github.lightguard.documentation.asciidoc.extraction.model.PushableLocation;
import io.github.lightguard.documentation.asciidoc.extraction.model.Task;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract", mixinStandardHelpOptions = true, version = "1.0",
        description = "Create a modular documentation layout from a directory of asciidoc files.")
public class ExtractionRunner implements Runnable, CommandLine.IExitCodeGenerator {

    // Use the JBoss LogManager
    static {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    @ArgGroup(heading = "Input", exclusive = true, multiplicity = "1")
    InputOptions inputOptions;

    @ArgGroup(heading = "Output", exclusive = true, multiplicity = "1")
    OutputOptions outputOptions;

    @Option(names = {"-v"}, description = "Verbose logging", defaultValue = "false")
    boolean verbose;

    @Option(names = {"-a"}, split = "\\|",
            description = "Key=Value pairs to set as attributes to asciidoctor. Multiples separated by '|': 'key1=v1|key2=v2'")
    Map<String, Object> attributes;

    @Option(names = {"-i"}, split = ",", description = "Ignore file, multiples separated by ','")
    List<File> ignoreFiles;

    @Option(names = {"--pantheonV2"}, description = "Enable Pantheon V2 compatible output.", defaultValue = "false")
    boolean pv2;

    int exitCode;

    /**
     * Options for the source location of the files.
     */
    static class InputOptions {
        @Option(names = {"-s"}, description = "Directory containing the input asciidoc files.")
        File inputDir;

        @ArgGroup(exclusive = false)
        GitInputOptions gitInputOptions;
    }

    /**
     * Options for the output location of the generated files.
     */
    static class OutputOptions {
        @Option(names = {"-o"}, description = "Directory to place generated modules and assemblies.")
        File outputDir;

        @ArgGroup(exclusive = false)
        GitOutputOptions gitOutputOptions;
    }

    /**
     * Options for the source repo.
     */
    static class GitInputOptions {
        @Option(names = {"-sr"}, description = "Git URL to the source repository.", required = true)
        String sourceRepo;

        @Option(names = {"-sb"}, defaultValue = "master", description = "Branch in source repository.")
        String sourceBranch;

        @Option(names = {"-su"}, description = "Source Git Username")
        String userName;

        @Option(names = {"-sp"}, description = "Source Git Password", interactive = true)
        String password;
    }

    /**
     * Options for the output repo.
     */
    static class GitOutputOptions {
        @Option(names = {"-or"}, description = "Git URL to the output repository.", required = true)
        String outputRepo;

        @Option(names = {"-ob"}, defaultValue = "master", description = "Branch in output repository.")
        String outputBranch;

        @Option(names = {"-ou"}, description = "Output Git Username")
        String userName;

        @Option(names = {"-op"}, description = "Output Git Password", interactive = true)
        String password;
    }

    /**
     * Entry into the program
     *
     * @param args CLI options.
     */
    public static void main(String... args) {
        var exitCode = new CommandLine(new ExtractionRunner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * Runs the program.
     */
    @Override
    public void run() {
        // Setup verbose logging if needed
        if (verbose) {
            var rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.setLevel(Level.FINE);
            Arrays.stream(rootLogger.getHandlers()).forEach(handler -> handler.setLevel(Level.FINE));
        }

        var logger = LogManager.getLogManager().getLogger(ExtractionRunner.class.getName());

        Location location = inputOptions.inputDir != null
                ? new LocalDirectoryLocation(this.inputOptions.inputDir)
                : new GitRepository(inputOptions.gitInputOptions.sourceRepo, inputOptions.gitInputOptions.sourceBranch,
                inputOptions.gitInputOptions.userName, inputOptions.gitInputOptions.password, false);

        PushableLocation pushableLocation = outputOptions.outputDir != null
                ? PushableLocation.locationWrapper(new LocalDirectoryLocation(this.outputOptions.outputDir), () -> {
        })
                : new GitRepository(outputOptions.gitOutputOptions.outputRepo, outputOptions.gitOutputOptions.outputBranch,
                outputOptions.gitOutputOptions.userName, outputOptions.gitOutputOptions.password, true);

        var task = new Task(location, pushableLocation, attributes, ignoreFiles, pv2);

        var extractor = new Extractor(task);

        this.exitCode = extractor.process();
    }
}
