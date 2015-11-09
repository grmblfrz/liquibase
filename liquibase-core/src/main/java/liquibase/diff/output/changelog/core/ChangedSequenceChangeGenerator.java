package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterSequenceChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.List;

public class ChangedSequenceChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) changedObject;

        List<Change> changes = new ArrayList<Change>();

        if (differences.isDifferent("incrementBy")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setIncrementBy(sequence.incrementBy);
            changes.add(change);
        }

        if (differences.isDifferent("maxValue")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setMaxValue(sequence.maxValue);
            changes.add(change);
        }

        if (differences.isDifferent("ordered")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setOrdered(sequence.ordered);
            changes.add(change);
        }

        if (differences.isDifferent("cacheSize")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setCacheSize(sequence.cacheSize);
            changes.add(change);
        }

        if (differences.isDifferent("willCycle")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setWillCycle(sequence.willCycle);
            changes.add(change);
        }

        if (changes.size() == 0) {
            return null;
        } else {
            return changes.toArray(new Change[changes.size()]);
        }
    }

    protected AlterSequenceChange createAlterSequenceChange(Sequence sequence, DiffOutputControl control) {
        AlterSequenceChange change = new AlterSequenceChange();
        if (control.getIncludeCatalog()) {
            change.setCatalogName(sequence.getContainer().container.name);
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(sequence.getContainer().name);
        }
        change.setSequenceName(sequence.name);
        return change;
    }
}
