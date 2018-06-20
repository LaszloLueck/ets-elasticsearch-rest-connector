package de.kaufhof.ets.elasticsearchrestconnector.core.client.model.aggregations.result

import play.api.libs.json._

case class AggregationResults(aggregationList: List[AggregationResult])

object AggregationResults {
  implicit val reads: Reads[AggregationResults] = new Reads[AggregationResults] {
    override def reads(json: JsValue): JsResult[AggregationResults] = {
      (json \ "aggregations").asOpt[JsObject].map{aggs =>
        val result = aggs.value.map(renderAggregationResult)
        JsSuccess(AggregationResults(aggregationList = result.toList))
      }.getOrElse(JsError("no aggregation found in result"))
    }
  }

  def renderAggregationResult(tpl: (String, JsValue)): AggregationResult = {
    val content: JsObject = tpl._2.as[JsObject]
    val elements: List[AggregateFieldElement] = renderAggregateFieldElement(content)
    AggregationResult(
      AggregationResultElement(
        key = tpl._1,
        count = (content \ "doc_count").asOpt[Long].getOrElse(elements.headOption.map(_.buckets.size.toLong).getOrElse(0L))
      ),
      fieldElement = elements
    )
  }


  def renderAggregateFieldElement(jso: JsObject): List[AggregateFieldElement] = {
    val elements: Iterable[AggregateFieldElement] = jso.value.flatMap {
      case (key: String, elements: JsObject) =>
        Some(AggregateFieldElement(key, renderAggregateBucketResult(elements)))
      case (key: String, elements: JsArray) if key == "buckets" =>
        Some(AggregateFieldElement(key, renderAggregateBucketResultFromArray(elements)))
      case _ =>
        None
    }
    elements.toList
  }

  def renderAggregateBucketResultFromArray(jsa: JsArray): List[AggregateBucketResult] = {
    jsa.as[List[JsValue]].map(buildAggregateBucketResult)
  }

  def renderAggregateBucketResult(jso: JsObject): List[AggregateBucketResult] = {
    (jso \ "buckets").as[List[JsValue]].map(buildAggregateBucketResult)
  }


  private def buildAggregateBucketResult(jsValue: JsValue): AggregateBucketResult = {
    AggregateBucketResult(
      key = (jsValue \ "key").as[String],
      doc_count = (jsValue \ "doc_count").as[Long]
    )
  }

}