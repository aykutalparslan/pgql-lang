/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryType;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;
import oracle.pgql.lang.ir.TableExpression;

public class ModifyQuery extends GraphQuery {

  private static final String PROJECTION_ERROR = "A modify query does not have a SELECT";

  private List<Modification> modifications;

  public ModifyQuery(List<CommonPathExpression> commonPathExpressions, List<Modification> modifications,
      SchemaQualifiedName graphName, List<TableExpression> tableExpressions, Set<QueryExpression> constraints,
      GroupBy groupBy, QueryExpression having, OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    super(commonPathExpressions, graphName, tableExpressions, constraints, groupBy, having, orderBy, limit, offset);
    this.modifications = modifications;
  }

  /**
   * @deprecated use the constructor with a WHERE clause
   */
  @Deprecated
  public ModifyQuery(List<CommonPathExpression> commonPathExpressions, List<Modification> modifications,
      SchemaQualifiedName graphName, List<TableExpression> tableExpressions, GroupBy groupBy, QueryExpression having,
      OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    this(commonPathExpressions, modifications, graphName, tableExpressions, new LinkedHashSet<>(), groupBy, having,
        orderBy, limit, offset);
  }

  /**
   * @deprecated use the constructor with an arbitrary number of table expression in FROM clause
   */
  @Deprecated
  public ModifyQuery(List<CommonPathExpression> commonPathExpressions, List<Modification> modifications,
      SchemaQualifiedName graphName, GraphPattern graphPattern, GroupBy groupBy, QueryExpression having,
      OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    this(commonPathExpressions, modifications, graphName, Collections.singletonList(graphPattern), groupBy, having,
        orderBy, limit, offset);
  }

  @Override
  public QueryType getQueryType() {
    return QueryType.MODIFY;
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.GRAPH_MODIFY;
  }

  public List<Modification> getModifications() {
    return modifications;
  }

  public void setModifications(List<Modification> modifications) {
    this.modifications = modifications;
  }

  @Override
  public Projection getProjection() {
    throw new UnsupportedOperationException(PROJECTION_ERROR);
  }

  @Override
  public void setProjection(Projection projection) {
    throw new UnsupportedOperationException(PROJECTION_ERROR);
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ModifyQuery other = (ModifyQuery) obj;
    if (modifications == null) {
      if (other.modifications != null)
        return false;
    } else if (!modifications.equals(other.modifications))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }

}
