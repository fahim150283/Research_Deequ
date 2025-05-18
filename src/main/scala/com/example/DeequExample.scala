package com.example

import com.amazon.deequ.VerificationSuite
import com.amazon.deequ.checks.{Check, CheckLevel}
import org.apache.spark.sql.SparkSession

object DeequExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Deequ Data Quality")
      .master("local[*]")
      .getOrCreate()

    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/test/resources/Excel/peaks.csv")

    df.show()
    df.printSchema()

    val check = Check(CheckLevel.Error, "Basic Quality Checks")
      .hasSize(_ > 0)
      .isComplete("id")
      .isUnique("id")
      .isComplete("name")

    val result = VerificationSuite()
      .onData(df)
      .addCheck(check)
      .run()

    result.checkResults.foreach { case (check, result) =>
      println(s"Check: ${check.description}, Status: ${result.status}")
      result.constraintResults.foreach { c =>
        println(s" - ${c.constraint}: ${c.status}")
      }
    }

    spark.stop()
  }
}
