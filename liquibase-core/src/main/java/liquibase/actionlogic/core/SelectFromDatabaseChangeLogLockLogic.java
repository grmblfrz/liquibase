package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.SelectDataAction;
import liquibase.action.core.SelectFromDatabaseChangeLogLockAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectReference;

public class SelectFromDatabaseChangeLogLockLogic extends AbstractActionLogic<SelectFromDatabaseChangeLogLockAction> {

    @Override
    protected Class<SelectFromDatabaseChangeLogLockAction> getSupportedAction() {
        return SelectFromDatabaseChangeLogLockAction.class;
    }

    @Override
    public ValidationErrors validate(SelectFromDatabaseChangeLogLockAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("selectColumns", action);
    }

    @Override
    public ActionResult execute(SelectFromDatabaseChangeLogLockAction action, Scope scope) throws ActionPerformException {
        final Database database = scope.getDatabase();

        return new DelegateResult(
                (SelectDataAction) new SelectDataAction(
                        new ObjectReference(database.getLiquibaseCatalogName(),
                                database.getLiquibaseSchemaName(),
                                database.getDatabaseChangeLogTableName()),
                        action.selectColumns)
        );
    }

}
