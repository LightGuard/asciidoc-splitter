package com.redhat.documentation.asciidoc.cli;

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

        // Modules
        var modulesDir = this.outputDirPath.resolve("modules").resolve("optaplanner-vaccination");
        assertThat(modulesDir).exists();
        assertThat(modulesDir.toFile().list()).containsExactlyInAnyOrder("con-pinned-planning-entities.adoc",
                "con-vaccination-contraints.adoc", "con-vaccination-scheduler.adoc", "con-optaplanner-solver.adoc",
                "vaccination-native-proc.adoc", "vaccination-scheduler-package-proc.adoc", "con-continuous-planning.adoc",
                "vaccination-scheduler-download-proc.adoc");
    }
}
