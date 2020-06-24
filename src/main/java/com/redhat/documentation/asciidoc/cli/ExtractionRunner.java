package com.redhat.documentation.asciidoc.cli;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.redhat.documentation.asciidoc.extraction.Extractor;
import com.redhat.documentation.asciidoc.extraction.model.GitRepository;
import com.redhat.documentation.asciidoc.extraction.model.LocalDirectoryLocation;
import com.redhat.documentation.asciidoc.extraction.model.Location;
import com.redhat.documentation.asciidoc.extraction.model.PushableLocation;
import com.redhat.documentation.asciidoc.extraction.model.Task;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract", mixinStandardHelpOptions = true, version = "1.0",
        description = "Create a modular documentation layout from a directory of asciidoc files.")
public class ExtractionRunner implements Runnable {

    // Use the JBoss LogManager
    static {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    @ArgGroup(heading = "Input", exclusive = true, multiplicity = "1")
    InputOptions inputOptions;

    @ArgGroup(heading = "Output", exclusive = true, multiplicity = "1")
    OutputOptions outputOptions;

    @Option(names = {"-v", "--verbose"}, description = "Verbose logging", defaultValue = "false")
    boolean verbose;

    @Option(names = {"-a", "--attributes"}, description = "Attributes to pass to asciidoctor")
    Map<String, Object> attributes;

    @Option(names = {"-i", "--ignore"}, description = "Ignore file")
    List<File> ignoreFiles;

    /**
     * Options for the source location of the files.
     */
    static class InputOptions {
        @Option(names = { "-s", "--sourceDir" }, description = "Directory containing the input asciidoc files.")
        File inputDir;

        @ArgGroup(exclusive = false)
        GitInputOptions gitInputOptions;
    }

    /**
     * Options for the output location of the generated files.
     */
    static class OutputOptions {
        @Option(names = { "-o", "--outputDir" }, description = "Directory to place generated modules and assemblies.")
        File outputDir;

        @ArgGroup(exclusive = false)
        GitOutputOptions gitOutputOptions;
    }

    /**
     * Options for the source repo.
     */
    static class GitInputOptions {
        @Option(names = { "-sr", "--sourceRepo" }, description = "Git URL to the source repository.", required = true)
        String sourceRepo;

        @Option(names = { "-sb",
                "--sourceBranch" }, defaultValue = "master", description = "Branch in source repository.")
        String sourceBranch;

        @Option(names = { "-su", "--source-username" }, description = "Source Git Username")
        String userName;

        @Option(names = { "-sp", "--source-password" }, description = "Source Git Password", interactive = true)
        String password;
    }

    /**
     * Options for the output repo.
     */
    static class GitOutputOptions {
        @Option(names = { "-or", "--outputRepo" }, description = "Git URL to the output repository.", required = true)
        String outputRepo;

        @Option(names = { "-ob",
                "--outputBranch" }, defaultValue = "master", description = "Branch in output repository.")
        String outputBranch;

        @Option(names = { "-ou", "--output-username" }, description = "Output Git Username")
        String userName;

        @Option(names = { "-op", "--output-password" }, description = "Output Git Password", interactive = true)
        String password;
    }

    /**
     * Entry into the program
     * @param args CLI options.
     */
    public static void main(String... args) {
        var exitCode = new CommandLine(new ExtractionRunner()).execute(args);
        System.exit(exitCode);
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
                                    inputOptions.gitInputOptions.userName, inputOptions.gitInputOptions.password);

        PushableLocation pushableLocation = outputOptions.outputDir != null
                ? PushableLocation.locationWrapper(new LocalDirectoryLocation(this.outputOptions.outputDir), () -> {})
                : new GitRepository(outputOptions.gitOutputOptions.outputRepo, outputOptions.gitOutputOptions.outputBranch,
                                    outputOptions.gitOutputOptions.userName, outputOptions.gitOutputOptions.password);

        var task = new Task(location, pushableLocation, attributes, ignoreFiles);

        var extractor = new Extractor(task);
        extractor.process();
    }

}
