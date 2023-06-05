/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.BaseElementTable;
import oracle.pgql.lang.ddl.propertygraph.BaseGraph;
import oracle.pgql.lang.ddl.propertygraph.CreatePropertyGraph;
import oracle.pgql.lang.ddl.propertygraph.CreateSuperPropertyGraph;
import oracle.pgql.lang.ddl.propertygraph.EdgeTable;
import oracle.pgql.lang.ddl.propertygraph.Key;
import oracle.pgql.lang.ddl.propertygraph.Label;
import oracle.pgql.lang.ddl.propertygraph.Property;
import oracle.pgql.lang.ddl.propertygraph.VertexTable;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.PgqlStatement;

import static oracle.pgql.lang.CommonTranslationUtil.getList;
import static oracle.pgql.lang.CommonTranslationUtil.getSchemaQualifiedName;
import static oracle.pgql.lang.CommonTranslationUtil.getSomeValue;
import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.isNone;
import static oracle.pgql.lang.CommonTranslationUtil.translateExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranslateCreatePropertyGraph {

  private static int CREATE_PROPERTY_GRAPH_NAME = 0;

  private static int CREATE_PROPERTY_GRAPH_BASE_GRAPHS = 1;

  private static int CREATE_PROPERTY_GRAPH_VERTEX_TABLES = 2;

  private static int CREATE_PROPERTY_GRAPH_EDGE_TABLES = 3;

  private static int CREATE_PROPERTY_GRAPH_OPTIONS = 4;

  private static int BASE_GRAPHS_GRAPHS_LIST = 0;

  private static int BASE_GRAPH_NAME = 0;

  private static int BASE_GRAPH_ELEMENT_TABLES = 1;

  private static int BASE_GRAPH_ELEMENT_TABLES_TABLES_LIST = 0;

  private static int BASE_ELEMENT_TABLE_NAME = 0;

  private static int BASE_ELEMENT_TABLE_ALIAS = 1;

  private static int BASE_GRAPH_ELEMENT_TABLES_TABLES_EXCEPT = 0;

  private static int BASE_GRAPH_ELEMENT_TABLES_TABLES_EXCEPT_LIST = 0;

  private static int VERTEX_TABLES_TABLES_LIST = 0;

  private static int EDGE_TABLES_TABLES_LIST = 0;

  private static int VERTEX_TABLE_NAME = 0;

  private static int VERTEX_TABLE_ALIAS = 1;

  private static int VERTEX_TABLE_KEY = 2;

  private static int VERTEX_TABLE_LABEL_AND_PROPERTIES = 3;

  private static int EDGE_TABLE_NAME = 0;

  private static int EDGE_TABLE_ALIAS = 1;

  private static int EDGE_TABLE_KEY = 2;

  private static int KEY_CLAUSE_COLUMN_NAMES = 0;

  private static int EDGE_TABLE_SOURCE_VERTEX_TABLE = 3;

  private static int EDGE_TABLE_DESTINATION_VERTEX_TABLE = 4;

  private static int EDGE_TABLE_LABEL_AND_PROPERTIES = 5;

  private static int SOURCE_VERTEX_KEY = 0;

  private static int DESTINATION_VERTEX_KEY = 0;

  private static int REFERENCED_VERTEX_TABLE = 1;

  private static int REFERENCED_VERTEX_TABLE_NAME = 0;

  private static int REFERENCED_VERTEX_TABLE_COLUMN_LIST = 1;

  private static int LABEL_AND_PROPERTIES_CLAUSE_LABEL_AND_PROPERTIES_LIST = 0;

  private static int LABEL_AND_PROPERTIES_LABEL_CLAUSE = 0;

  private static int LABEL_AND_PROPERTIES_PROPERTIES_CLAUSE = 1;

  private static int PROPERTIES_CLAUSE_PROPERTIES_LIST = 0;

  private static int PROPERTIES_ARE_ALL_COLUMNS_EXCEPT_PART = 1;

  private static int EXCEPT_COLUMNS_LIST = 0;

  private static int EXP_AS_VAR_EXP = 0;

  private static int EXP_AS_VAR_VAR = 1;

  protected static PgqlStatement translateCreatePropertyGraph(IStrategoTerm ast) throws PgqlException {

    IStrategoTerm graphNameT = ast.getSubterm(CREATE_PROPERTY_GRAPH_NAME);

    SchemaQualifiedName graphName = getSchemaQualifiedName(graphNameT);

    List<BaseGraph> baseGraphs = getBaseGraphs(ast.getSubterm(CREATE_PROPERTY_GRAPH_BASE_GRAPHS));

    List<VertexTable> vertexTables = getVertexTables(ast.getSubterm(CREATE_PROPERTY_GRAPH_VERTEX_TABLES));

    List<EdgeTable> edgeTables = getEdgeTables(ast.getSubterm(CREATE_PROPERTY_GRAPH_EDGE_TABLES), vertexTables);

    IStrategoTerm optionsT = ast.getSubterm(CREATE_PROPERTY_GRAPH_OPTIONS);

    List<String> options = isNone(optionsT) ? null : getOptions(optionsT);

    CreatePropertyGraph cpg = baseGraphs == null ? new CreatePropertyGraph(graphName, vertexTables, edgeTables)
        : new CreateSuperPropertyGraph(graphName, baseGraphs, vertexTables, edgeTables);
    cpg.setOptions(options);
    return cpg;
  }

  private static List<BaseGraph> getBaseGraphs(IStrategoTerm baseGraphsT) {
    if (isNone(baseGraphsT)) {
      return null;
    }

    IStrategoTerm baseGraphsListT = getSomeValue(baseGraphsT).getSubterm(BASE_GRAPHS_GRAPHS_LIST);
    List<BaseGraph> result = new ArrayList<>();
    for (IStrategoTerm baseGraphT : baseGraphsListT) {
      IStrategoTerm graphNameT = baseGraphT.getSubterm(BASE_GRAPH_NAME);
      SchemaQualifiedName graphName = getSchemaQualifiedName(graphNameT);

      IStrategoTerm optionalBaseElementTablesT = baseGraphT.getSubterm(BASE_GRAPH_ELEMENT_TABLES);
      List<BaseElementTable> baseElementTables = null;
      List<String> allElementTablesExcept = null;
      if (!isNone(optionalBaseElementTablesT)) {
        IStrategoAppl baseElementTablesT = (IStrategoAppl) getSomeValue(optionalBaseElementTablesT);
        String constructorName = baseElementTablesT.getConstructor().getName();
        switch (constructorName) {
          case "ElementTables":
            IStrategoTerm elementTablesT = baseElementTablesT.getSubterm(BASE_GRAPH_ELEMENT_TABLES_TABLES_LIST);
            baseElementTables = new ArrayList<>();
            for (IStrategoTerm baseElementTableT : elementTablesT) {
              String referencedTableName = getString(baseElementTableT.getSubterm(BASE_ELEMENT_TABLE_NAME));

              IStrategoTerm elementTableAliasT = baseElementTableT.getSubterm(BASE_ELEMENT_TABLE_ALIAS);
              String tableAlias = getString(elementTableAliasT);

              baseElementTables.add(new BaseElementTable(referencedTableName, tableAlias));
            }
            break;
          case "AllElementTables":
            IStrategoTerm exceptElementTablesT = baseElementTablesT.getSubterm(BASE_GRAPH_ELEMENT_TABLES_TABLES_EXCEPT);

            if (!isNone(exceptElementTablesT)) {
              allElementTablesExcept = new ArrayList<String>();
              for (IStrategoTerm exceptElementTableT : getSomeValue(exceptElementTablesT)
                  .getSubterm(BASE_GRAPH_ELEMENT_TABLES_TABLES_EXCEPT_LIST)) {
                String exceptElementTable = getString(exceptElementTableT);
                allElementTablesExcept.add(exceptElementTable);
              }
            }
            break;
          default:
            throw new IllegalArgumentException("Unknown constructor name: " + constructorName);
        }
      }

      result.add(new BaseGraph(graphName, baseElementTables, allElementTablesExcept));
    }
    return result;
  }

  private static List<VertexTable> getVertexTables(IStrategoTerm vertexTablesT) throws PgqlException {
    IStrategoTerm vertexTablesListT = vertexTablesT.getSubterm(VERTEX_TABLES_TABLES_LIST);
    List<VertexTable> result = new ArrayList<>();
    for (IStrategoTerm vertexTableT : vertexTablesListT) {
      IStrategoTerm tableNameT = vertexTableT.getSubterm(VERTEX_TABLE_NAME);
      SchemaQualifiedName tableName = getSchemaQualifiedName(tableNameT);
      String tableAlias = getString(vertexTableT.getSubterm(VERTEX_TABLE_ALIAS));

      Key vertexKey = getKey(vertexTableT.getSubterm(VERTEX_TABLE_KEY));
      List<Label> labels = getLabels(vertexTableT.getSubterm(VERTEX_TABLE_LABEL_AND_PROPERTIES));
      result.add(new VertexTable(tableName, tableAlias, vertexKey, labels));
    }
    return result;
  }

  private static List<EdgeTable> getEdgeTables(IStrategoTerm edgeTablesT, List<VertexTable> vertexTables)
      throws PgqlException {
    IStrategoTerm edgeTablesListT = edgeTablesT.getSubterm(EDGE_TABLES_TABLES_LIST);
    List<EdgeTable> result = new ArrayList<>();
    for (IStrategoTerm edgeTableT : edgeTablesListT) {
      IStrategoTerm tableNameT = edgeTableT.getSubterm(EDGE_TABLE_NAME);
      SchemaQualifiedName tableName = getSchemaQualifiedName(tableNameT);
      String tableAlias = getString(edgeTableT.getSubterm(EDGE_TABLE_ALIAS));

      Key edgeKey = getKey(edgeTableT.getSubterm(EDGE_TABLE_KEY));

      IStrategoTerm sourceVertexTableT = edgeTableT.getSubterm(EDGE_TABLE_SOURCE_VERTEX_TABLE);
      Key edgeSourceKey = getKey(sourceVertexTableT.getSubterm(SOURCE_VERTEX_KEY));
      IStrategoTerm referencedSourceVertexTable = sourceVertexTableT.getSubterm(REFERENCED_VERTEX_TABLE);
      IStrategoTerm sourceVertexTableNameT = referencedSourceVertexTable.getSubterm(REFERENCED_VERTEX_TABLE_NAME);
      String sourceVertexTableName = getSchemaQualifiedName(sourceVertexTableNameT).getName();
      VertexTable sourceVertexTable = getVertexTable(vertexTables, sourceVertexTableName);
      Key sourceVertexKey = getKey(referencedSourceVertexTable.getSubterm(REFERENCED_VERTEX_TABLE_COLUMN_LIST));

      IStrategoTerm destinationVertexTableT = edgeTableT.getSubterm(EDGE_TABLE_DESTINATION_VERTEX_TABLE);
      Key edgeDestinationKey = getKey(destinationVertexTableT.getSubterm(DESTINATION_VERTEX_KEY));
      IStrategoTerm referencedDestinationVertexTable = destinationVertexTableT.getSubterm(REFERENCED_VERTEX_TABLE);
      IStrategoTerm destinationVertexTableNameT = referencedDestinationVertexTable
          .getSubterm(REFERENCED_VERTEX_TABLE_NAME);
      String destinationVertexTableName = getSchemaQualifiedName(destinationVertexTableNameT).getName();
      VertexTable destinationVertexTable = getVertexTable(vertexTables, destinationVertexTableName);
      Key destinationVertexKey = getKey(
          referencedDestinationVertexTable.getSubterm(REFERENCED_VERTEX_TABLE_COLUMN_LIST));

      List<Label> labels = getLabels(edgeTableT.getSubterm(EDGE_TABLE_LABEL_AND_PROPERTIES));
      result.add(new EdgeTable(tableName, tableAlias, edgeKey, sourceVertexTable, edgeSourceKey, sourceVertexKey,
          destinationVertexTable, edgeDestinationKey, destinationVertexKey, labels));
    }
    return result;
  }

  private static VertexTable getVertexTable(List<VertexTable> vertexTables, String tableAlias) {
    for (VertexTable vertexTable : vertexTables) {
      if (vertexTable.getTableAlias().equals(tableAlias)) {
        return vertexTable;
      }
    }

    // create a dummy vertex table for the case we reference a vertex table that is defined in a base graph rather than
    // in the current statement
    return new VertexTable(new SchemaQualifiedName(null, tableAlias), null, null);
  }

  private static Key getKey(IStrategoTerm keyClauseT) {
    if (isNone(keyClauseT)) {
      return null;
    }
    keyClauseT = getSomeValue(keyClauseT).getSubterm(KEY_CLAUSE_COLUMN_NAMES);
    List<String> columnNames = new ArrayList<>();
    for (IStrategoTerm columnReference : keyClauseT) {
      columnNames.add(getString(columnReference));
    }
    return new Key(columnNames);
  }

  private static List<Label> getLabels(IStrategoTerm labelAndPropertiesClauseT) throws PgqlException {
    IStrategoTerm labelAndPropertiesListT = labelAndPropertiesClauseT
        .getSubterm(LABEL_AND_PROPERTIES_CLAUSE_LABEL_AND_PROPERTIES_LIST);
    List<Label> result = new ArrayList<>();
    for (IStrategoTerm labelAndPropertiesT : labelAndPropertiesListT) {
      String labelName = getString(labelAndPropertiesT.getSubterm(LABEL_AND_PROPERTIES_LABEL_CLAUSE));
      Label label = getLabel(labelName, labelAndPropertiesT.getSubterm(LABEL_AND_PROPERTIES_PROPERTIES_CLAUSE));
      result.add(label);
    }
    return result;
  }

  private static Label getLabel(String labelName, IStrategoTerm propertiesClauseT) throws PgqlException {
    if (isNone(propertiesClauseT)) {
      return null;
    } else {
      IStrategoTerm propertiesSpecificationT = getSomeValue(propertiesClauseT);
      String propertiesSpecificationType = ((IStrategoAppl) propertiesSpecificationT).getConstructor().getName();
      switch (propertiesSpecificationType) {
        case "PropertyExpressions":
          IStrategoTerm propertiesListT = propertiesSpecificationT.getSubterm(PROPERTIES_CLAUSE_PROPERTIES_LIST);
          List<Property> properties = new ArrayList<>();
          for (IStrategoTerm expAsVarT : propertiesListT) {
            TranslationContext translationContext = new TranslationContext(Collections.emptyMap(),
                Collections.emptySet(), Collections.emptyMap());
            QueryExpression valueExpression = translateExp(expAsVarT.getSubterm(EXP_AS_VAR_EXP), translationContext);
            IStrategoTerm propertyNameT = expAsVarT.getSubterm(EXP_AS_VAR_VAR);
            String propertyName;
            if (isNone(propertyNameT)) {
              // error recovery: normally a column name has to be provided for complex expressions. If it is not
              // provided then we allow it to be null here and rely on the error message generated by Spoofax
              propertyName = null;
            } else {
              propertyName = getString(expAsVarT.getSubterm(EXP_AS_VAR_VAR));
            }
            properties.add(new Property(valueExpression, propertyName));
          }
          return new Label(labelName, properties);
        case "PropertiesAreAllColumns":
          IStrategoTerm exceptColumnsT = propertiesSpecificationT.getSubterm(PROPERTIES_ARE_ALL_COLUMNS_EXCEPT_PART);
          boolean propertiesAreAllColumns = true;
          if (isNone(exceptColumnsT)) {
            return new Label(labelName, propertiesAreAllColumns);
          } else {
            IStrategoTerm exceptColumnsListT = getSomeValue(exceptColumnsT).getSubterm(EXCEPT_COLUMNS_LIST);
            List<String> columnNames = new ArrayList<>();
            for (IStrategoTerm columnNameT : exceptColumnsListT) {
              columnNames.add(getString(columnNameT));
            }
            return new Label(labelName, propertiesAreAllColumns, columnNames);
          }
        default:
          throw new IllegalArgumentException(propertiesSpecificationType);
      }
    }
  }

  private static List<String> getOptions(IStrategoTerm optionsT) throws PgqlException {
    IStrategoTerm optionsListT = getList(optionsT);
    List<String> options = new ArrayList<>();
    for (IStrategoTerm optionT : optionsListT) {
      options.add(getString(optionT));
    }
    return options;
  }
}
