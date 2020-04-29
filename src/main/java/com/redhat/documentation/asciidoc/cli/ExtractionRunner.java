package com.redhat.documentation.asciidoc.cli;

import com.redhat.documentation.asciidoc.Configuration;
import com.redhat.documentation.asciidoc.extraction.Extractor;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(name = "extract", mixinStandardHelpOptions = true, version = "1.0", description = "Create a modular documentation layout from a directory of asciidoc files.")
public class ExtractionRunner implements Runnable {

    @ArgGroup(heading = "Input", exclusive = true, multiplicity = "1")
    InputOptions inputOptions;

    @ArgGroup(heading = "Output", exclusive = true, multiplicity = "1")
    OutputOptions outputOptions;

    static class InputOptions {
        @Option(names = { "-s", "--sourceDir" }, description = "Directory containing the input asciidoc files.")
        File inputDir;

        @ArgGroup(exclusive = false)
        GitInputOptions gitInputOptions;
    }

    static class OutputOptions {
        @Option(names = { "-o", "--outputDir" }, description = "Directory to place generated modules and assemblies.")
        File outputDir;

        @ArgGroup(exclusive = false)
        GitOutputOptions gitOutputOptions;
    }

    static class GitInputOptions {
        @Option(names = { "-sr", "--sourceRepo" }, description = "Git URL to the source repository.", required = true)
        String sourceRepo;

        @Option(names = { "-sb",
                "--sourceBranch" }, defaultValue = "master", description = "Branch in source repository.")
        String sourceBranch;
    }

    static class GitOutputOptions {
        @Option(names = { "-or", "--outputRepo" }, description = "Git URL to the output repository.", required = true)
        String outputRepo;

        @Option(names = { "-ob",
                "--outputBranch" }, defaultValue = "master", description = "Branch in output repository.")
        String outputBranch;
    }

    public static void main(String... args) {
        var exitCode = new CommandLine(new ExtractionRunner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        var config = new Configuration(this.inputOptions.inputDir, this.outputOptions.outputDir);
        var extractor = new Extractor(config);
        extractor.process();
    }

}
