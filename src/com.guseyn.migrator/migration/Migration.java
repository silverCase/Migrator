package migration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import stages.DetectingCodeSegmentsByMigrationRules;
import storage.MemoryStorage;
import storage.MigrationRule;

public class Migration {
    static String pathClone = Paths.get(".").toAbsolutePath().normalize().toString() + "/Clone/Process/";

    public static List<MigrationRule> migrationRulesWithoutVersion(int isValid) {
        List<MigrationRule> migrationRules = MemoryStorage.migrationRules.stream().filter(migrationRule -> migrationRule.isValid == isValid).sorted(
            Comparator.comparingInt(migrationRule -> migrationRule.frequency)).collect(
            Collectors.toList());
        Collections.reverse(migrationRules);
        return migrationRules;
    }

}
