package de.kaufhof.ets.elasticsearchrestconnector.core.client.model.mapping

import de.kaufhof.ets.elasticsearchrestconnector.core.client.model.mapping.MappingType.MappingType
import play.api.libs.json._

case class Mapping(
                    name: String,
                    dynamic: MappingType,
                    dateDetection: Boolean,
                    numericDetection: Boolean,
                    parent: Option[String] = None,
                    routing: Option[Boolean] = None,
                    dynamicTemplates: List[DynamicTemplate],
                    properties: List[MappingProperty]
                  )

object Mapping {

  def apply(mapping: Mapping): (String, JsValue) = {
    mapping.name -> JsObject(
      Map(
        "dynamic" -> JsString(mapping.dynamic.toString),
        "dynamic_templates" -> JsArray(mapping.dynamicTemplates.map(DynamicTemplate.writes.writes(_))),
        "date_detection" -> JsBoolean(mapping.dateDetection),
        "properties" -> JsObject(mapping.properties.map(MappingProperty(_))),
        "numeric_detection" -> JsBoolean(mapping.numericDetection)
      )
        ++ renderParent(mapping.parent)
        ++ renderRouting(mapping.routing)
    )
  }

  private def renderParent(str: Option[String]): Option[(String, JsObject)] = {
    str.map { parent =>
      "_parent" -> Json.obj(
        "type" -> parent
      )
    }
  }

  private def renderRouting(str: Option[Boolean]): Option[(String, JsObject)] = {
    str.map { routing =>
      "routing" -> Json.obj(
        "required" -> routing
      )
    }
  }

}
