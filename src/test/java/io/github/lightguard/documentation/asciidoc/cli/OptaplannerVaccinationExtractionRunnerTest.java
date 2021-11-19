package io.github.lightguard.documentation.asciidoc.cli;

import io.github.lightguard.documentation.asciidoc.extension.ReaderPreprocessor;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class OptaplannerVaccinationExtractionRunnerTest extends ExtractionRunnerBase {
    @Test
    public void verifyCorrectSplit() throws Exception {
        executeRunner("docs/optaplanner-vaccination", false);

        // assembly
        var assemblyDir = this.outputDirPath.resolve("assemblies");
        assertThat(assemblyDir).exists();

        final Path vaccinationAssemblyDoc = assemblyDir.resolve("assembly-optaplanner-vaccination.adoc");
        assertThat(vaccinationAssemblyDoc).exists();
        assertThat(Files.lines(vaccinationAssemblyDoc)).contains("include::modules/optaplanner-vaccination/con-vaccination-scheduler.adoc[leveloffset=+1]");
        assertThat(Files.lines(vaccinationAssemblyDoc)).contains("include::modules/optaplanner-vaccination/vaccination-scheduler-download-proc.adoc[leveloffset=+1]");

        // Modules
        var modulesDir = this.outputDirPath.resolve("modules").resolve("optaplanner-vaccination");
        assertThat(modulesDir).exists();
        assertThat(modulesDir.toFile().list()).containsExactlyInAnyOrder("con-pinned-planning-entities.adoc",
                "con-vaccination-contraints.adoc", "con-vaccination-scheduler.adoc", "con-optaplanner-solver.adoc",
                "vaccination-native-proc.adoc", "vaccination-scheduler-package-proc.adoc", "con-continuous-planning.adoc",
                "vaccination-scheduler-download-proc.adoc");
    }

    @Test
    public void verifyRealWorldCorrectSplit() throws Exception {
        executeRunner("docs/optaplanner-real-world", false);

        // assembly
        var assemblyDir = this.outputDirPath.resolve("assemblies");
        assertThat(assemblyDir).exists();

        final Path vaccinationAssemblyDoc = assemblyDir.resolve("assembly-planner-configuration.adoc");
        assertThat(vaccinationAssemblyDoc).exists();
        assertThat(Files.lines(vaccinationAssemblyDoc)).contains("include::modules/optaplanner-real-world/solving-a-problem-proc.adoc[leveloffset=+2]");
        assertThat(Files.lines(vaccinationAssemblyDoc)).contains("include::modules/optaplanner-real-world/logback-proc.adoc[leveloffset=+2]");
        assertThat(Files.lines(vaccinationAssemblyDoc)).doesNotContainSequence(ReaderPreprocessor.SPLITTER_COMMENT);

        // Modules
        var modulesDir = this.outputDirPath.resolve("modules").resolve("optaplanner-real-world");
        assertThat(modulesDir).exists();
        assertThat(modulesDir.toFile().list()).contains("logback-proc.adoc", "solving-a-problem-proc.adoc");
    }

    @Test
    public void issue89LineBreaks() throws Exception {
        var exitCode = executeRunner("docs/issue-89", false);

        assertThat(exitCode).isEqualTo(0);
        var assemblyDoc = assembliesDir.resolve("assembly-optaplanner-vaccination.adoc");
        assertThat(Files.readAllLines(assemblyDoc).get(34)).doesNotContain("ifdef::OPTAPLANNER-COMM[]");
        assertThat(Files.readAllLines(assemblyDoc).get(35)).doesNotContain("endif::OPTAPLANNER-COMM[]");
    }
}
