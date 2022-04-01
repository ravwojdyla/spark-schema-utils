package org.apache.spark.sql

import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeMap, AttributeReference}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.types.StructType

object SchemaUtils {

  def updateAttr(a: Attribute, attrMap: AttributeMap[Attribute]): Attribute = {
    attrMap.get(a) match {
      case Some(b) =>
        // TODO (rav): it's probably questionable if it's a good idea to update
        //             nullable property. Also dataType will update nullable as
        //             in the inner fields. We should probably be careful here
        //             and/or assume nullable for everything apart from primary
        //             keys - which is not bad, ala protobuf3.
        AttributeReference(a.name, b.dataType, b.nullable, b.metadata)(b.exprId, a.qualifier)
      case None => a
    }
  }

  def construct_update_attrs(
    outputs: Seq[Attribute],
    update_schema: StructType
  ): Seq[AttributeReference] = {
    assert(
      outputs.length == update_schema.fields.length,
      s"""Current implementation expect compatible schema (order and names of top level fields),
        |the number of top level fields in the output is ${outputs.length}
        |the number of top level fields in the update schema is ${update_schema.fields.length}.
        |""".stripMargin.replace("\n", " ") + s"""Output schema: ${outputs}
        |Update schema: ${update_schema}
        |""".stripMargin
    )
    val mismatch_fields =
      outputs.zipWithIndex.zip(update_schema.fields).filter(e => e._1._1.name != e._2.name)
    assert(
      mismatch_fields.isEmpty,
      s"""Current implementation expect compatible schema (order and names of top level fields),
         |there are some incompatible fields: ${mismatch_fields
        .map(e => s"${e._1._1.name} != ${e._2.name}")
        .mkString(", ")}
         |""".stripMargin.replace("\n", " ") + s"""Output schema: ${outputs}
         |Update schema: ${update_schema}
         |""".stripMargin
    )
    outputs.zip(update_schema.fields).map { case (o, f) =>
      AttributeReference(o.name, f.dataType, o.nullable, f.metadata)(o.exprId, o.qualifier)
    }
  }

  /**
   * Updates given DataFrame with new attributes from the new schema. This function assumes the
   * schemas are compatible. This is something you need to validate before calling this method. This
   * method doesn't eagerly trigger computation.
   */
  def apply(ds: Dataset[Row], new_schema_json: String): Dataset[Row] = {
    val new_schema = StructType.fromString(new_schema_json)
    val outputs = ds.queryExecution.analyzed.output
    val new_attrs = AttributeMap(
      construct_update_attrs(outputs, new_schema).map(i => (i, i.asInstanceOf[Attribute]))
    )
    val new_plan = ds.queryExecution.analyzed.transformExpressions { case a: AttributeReference =>
      updateAttr(a, new_attrs)
    }
    Dataset.ofRows(ds.sparkSession, new_plan)
  }

}
