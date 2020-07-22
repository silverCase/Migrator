package migration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import storage.MemoryStorage;
import storage.MigrationRule;

public class Migration {
    public static List<MigrationRule> migrationRulesWithoutVersion(int isValid) {
        List<MigrationRule> migrationRules = MemoryStorage.migrationRules.stream().filter(migrationRule -> migrationRule.isValid == isValid).sorted(
            Comparator.comparingInt(migrationRule -> migrationRule.frequency)).collect(
            Collectors.toList());
        Collections.reverse(migrationRules);
        return migrationRules;
    }
}
