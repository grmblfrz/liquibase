package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ActionStatus;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataTypeTranslatorFactory;
import liquibase.util.StringClauses;

import java.math.BigInteger;

public class AddAutoIncrementLogic extends AbstractSqlBuilderLogic<AddAutoIncrementAction> {

    public enum Clauses {
        autoIncrement, dataType
    }

    @Override
    protected Class<AddAutoIncrementAction> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase().supportsAutoIncrement();
    }

    @Override
    public ValidationErrors validate(AddAutoIncrementAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField("columnName", action);
        validationErrors.checkForRequiredField("columnDataType", action);

        if (!validationErrors.hasErrors()) {
            if (action.columnName.asList().size() < 2) {
                validationErrors.addError("Table name is required");
            }
        }

        return validationErrors;
    }

    @Override
    public ActionStatus checkStatus(AddAutoIncrementAction action, Scope scope) {
        ActionStatus result = new ActionStatus();
        try {
            Column column = scope.getSingleton(SnapshotFactory.class).get(action.columnName, scope);
            if (column == null) return result.unknown("Column '"+action.columnName+"' does not exist");


            result.assertApplied(column.isAutoIncrement(), "Column '"+action.columnName+"' is not auto-increment");

//            if (column.autoIncrementInformation != null) {
//                result.assertCorrect(action, column.autoIncrementInformation, "startWith");
//                result.assertCorrect(action, column.autoIncrementInformation, "incrementBy");
//            }

            return result;
        } catch (Exception e) {
            return result.unknown(e);

        }
    }

    @Override
    public ActionResult execute(AddAutoIncrementAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.columnName,
                generateSql(action, scope)));
    }

    protected StringClauses generateSql(AddAutoIncrementAction action, Scope scope) {

        Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses();
        clauses.append(Clauses.dataType, scope.getSingleton(DataTypeTranslatorFactory.class).getTranslator(scope).toSql(action.columnDataType, scope));
        clauses.append(Clauses.autoIncrement, generateAutoIncrementClause(action.autoIncrementInformation));

        return clauses;
    }

    public StringClauses generateAutoIncrementClause(Column.AutoIncrementInformation autoIncrementInformation) {
        StringClauses clauses = new StringClauses().append("marker", "GENERATED BY DEFAULT AS IDENTITY");

        StringClauses autoIncrementDetails = null;
        if (autoIncrementInformation != null) {
            autoIncrementDetails = new StringClauses("(", " ", ")");
            if (autoIncrementInformation.startWith != null) {
                autoIncrementDetails.append("startWith", "START WITH "+autoIncrementInformation.startWith);
            }
            if (autoIncrementInformation.incrementBy != null) {
                autoIncrementDetails.append("incrementBy", "INCREMENT BY "+autoIncrementInformation.incrementBy);
            }
        }
        clauses.append("details", autoIncrementDetails);

        return clauses;

    }


}
