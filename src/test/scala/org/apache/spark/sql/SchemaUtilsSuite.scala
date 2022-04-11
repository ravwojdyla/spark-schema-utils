package org.apache.spark.sql

import org.apache.spark.sql.catalyst.expressions.{Alias, Literal, NamedExpression}
import org.apache.spark.sql.catalyst.plans.PlanTest
import org.apache.spark.sql.catalyst.plans.logical.{OneRowRelation, Project}
import org.apache.spark.sql.test.SharedSparkSession
import org.apache.spark.sql.types.{IntegerType, Metadata, StructField, StructType}

class SchemaUtilsSuite extends PlanTest with SharedSparkSession {
  test("can update metadata") {
    val id1 = NamedExpression.newExprId
    val id2 = NamedExpression.newExprId
    val plan = Project(
      Stream(Alias(Literal(1), "a")(exprId = id1), Alias(Literal(2), "b")(exprId = id2)),
      OneRowRelation()
    )
    assert(!plan.output.toStructType.fields(0).metadata.contains("test"))
    val ds = Dataset.ofRows(spark, plan)
    val a_metadata = Metadata.fromJson("""{"test": 42}""")
    val new_schema = StructType(
      List(
        StructField("a", IntegerType, nullable = false, a_metadata),
        StructField("b", IntegerType, nullable = false)
      )
    )
    val result = SchemaUtils.apply(ds, new_schema.json)
    val expected = Project(
      Stream(
        Alias(Literal(1), "a")(exprId = id1, explicitMetadata = Some(a_metadata)),
        Alias(Literal(2), "b")(exprId = id2)
      ),
      OneRowRelation()
    )
    assert(
      expected.output.toStructType.json == result.queryExecution.analyzed.output.toStructType.json
    )
    assert(result.queryExecution.analyzed.output.toStructType.fields(0).metadata.contains("test"))
    assert(
      result.queryExecution.analyzed.output.toStructType.fields(0).metadata.getLong("test") == 42
    )
    val missing_metadata = Project(
      Stream(Alias(Literal(1), "a")(exprId = id1), Alias(Literal(2), "b")(exprId = id2)),
      OneRowRelation()
    )
    assert(
      missing_metadata.output.toStructType.json != result.queryExecution.analyzed.output.toStructType.json
    )
  }

}
