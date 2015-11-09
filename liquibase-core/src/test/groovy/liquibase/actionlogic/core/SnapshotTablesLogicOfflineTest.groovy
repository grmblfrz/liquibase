package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.action.core.SnapshotDatabaseObjectsAction
import liquibase.database.OfflineConnection
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.Snapshot
import liquibase.structure.ObjectReference

import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that

class SnapshotTablesLogicOfflineTest extends Specification {

    @Unroll("#featureName: #relatedTo")
    def "can snapshot tables with various relatedTo"() {
        when:
        def database = new MockDatabase()
        def scope = JUnitScope.getInstance(database)

        def snapshot = new Snapshot(scope).addAll([
                new Schema(new ObjectReference("cat1", "schema-a")),
                new Schema(new ObjectReference("cat1", "schema-b")),
                new Table(new ObjectReference("cat1", "schema-a", "table-a-1")),
                new Table(new ObjectReference("cat1", "schema-a", "table2")),
                new Table(new ObjectReference("cat1", "schema-b", "table-b-1")),
                new Table(new ObjectReference("cat1", "schema-b", "table2")),

                new Column(new ObjectReference("cat1", "schema-a", "table-a-1", "col-a-1-x")),
                new Column(new ObjectReference("cat1", "schema-a", "table-a-1", "col2")),
                new Column(new ObjectReference("cat1", "schema-a", "table2", "col-a-2-x")),
                new Column(new ObjectReference("cat1", "schema-a", "table2", "col2")),

                new Column(new ObjectReference("cat1", "schema-b", "table-b-1", "col-b-1-x")),
                new Column(new ObjectReference("cat1", "schema-b", "table-b-1", "col2")),
                new Column(new ObjectReference("cat1", "schema-b", "table2", "col-b-2-x")),
                new Column(new ObjectReference("cat1", "schema-b", "table2", "col2")),
        ])
        database.setConnection(new OfflineConnection("offline:mock", snapshot, new JUnitResourceAccessor()))

        def result = new SnapshotColumnsLogicOffline().execute(new SnapshotDatabaseObjectsAction(Column, relatedTo), scope)

        then:
        that result.asList(Table)*.toString(), containsInAnyOrder(expected)

        where:
        relatedTo                                                                | expected
        new ObjectReference(Schema, "cat1", "schema-a")                           | ["cat1.schema-a.table-a-1", "cat1.schema-a.table2"] as String[]
        new ObjectReference(Schema, "cat1", "schema-b")                           | ["cat1.schema-b.table-b-1", "cat1.schema-b.table2"] as String[]

        new ObjectReference(Table, "cat1", "schema-a", "table-a-1")               | ["cat1.schema-a.table-a-1"] as String[]
        new ObjectReference(Table, "cat1", "schema-a", "table2")                  | ["cat1.schema-a.table2"] as String[]
        new ObjectReference(Table, "cat1", "schema-b", "table-b-1")               | ["cat1.schema-b.table-b-1"] as String[]
        new ObjectReference(Table, "cat1", "schema-b", "table2")                  | ["cat1.schema-b.table2"] as String[]
    }

}
