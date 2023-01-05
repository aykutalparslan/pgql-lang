/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public interface TableExpression {

  TableExpressionType getTableExpressionType();

  void accept(QueryExpressionVisitor v);
}
