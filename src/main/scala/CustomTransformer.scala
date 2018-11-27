package com.streamsets.spark.scala

import com.streamsets.pipeline.api.Field
import com.streamsets.pipeline.api.Record
import com.streamsets.pipeline.spark.api.SparkTransformer
import com.streamsets.pipeline.spark.api.TransformResult
import org.apache.spark.api.java.JavaPairRDD
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions._

import java.io.Serializable
import java.util

object CustomTransformer {
  val VALUE_PATH = "/credit_card"
  val RESULT_PATH = "/credit_card_type"

  def validateRecord(record: Record) : Boolean = {
    // We need a field to operate on!
    Option(record.get(VALUE_PATH)).exists(_.getValueAsString.length > 0)
  }

  // Return true if creditCard starts with one of prefixList
  def ccPrefixMatches(creditCard: String, prefixList: Array[String]) : Boolean = {
    return !(prefixList.filter(creditCard.startsWith(_)).isEmpty)
  }
}

class CustomTransformer extends SparkTransformer with Serializable {
  val ccTypes = collection.mutable.LinkedHashMap[String, Array[String]]()

  var emptyRDD: JavaRDD[(Record, String)] = _

  override def init(javaSparkContextInstance: JavaSparkContext, params: util.List[String]): Unit = {
    for (param <- params) {
      val keyValue = param.split("=")
      ccTypes += (keyValue(0) -> (if (keyValue.size > 1) keyValue(1).split(",") else Array("")))
    }
  }

  override def transform(recordRDD: JavaRDD[Record]): TransformResult = {
    var rdd = recordRDD.rdd

    // Validate incoming records
    val errors: RDD[(Record, String)] = rdd.mapPartitions(iterator => {
      iterator.filterNot(CustomTransformer.validateRecord(_)).map((_, "Credit card number is missing"))
    })

    val result = rdd.mapPartitions(iterator => {
      iterator.filter(CustomTransformer.validateRecord(_)).map(record => {
        val creditCard: String = record.get(CustomTransformer.VALUE_PATH).getValueAsString
        val matches = ccTypes.filter((ccType) => CustomTransformer.ccPrefixMatches(creditCard, ccType._2))
        record.set(CustomTransformer.RESULT_PATH, Field.create(matches.head._1))
        record
      })
    })

    // return result
    new TransformResult(result.toJavaRDD(), new JavaPairRDD[Record, String](errors))
  }
}