package test

import play.api.Play.current

import org.specs2.mutable._

import no.samordnaopptak.apidoc.controllers.routes

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import no.samordnaopptak.apidoc.TestByAnnotation
import no.samordnaopptak.apidoc.controllers.ApiDocController
import no.samordnaopptak.apidoc.JsonMatcher._
import no.samordnaopptak.apidoc.{ApiDoc, SwaggerUtil}
import no.samordnaopptak.apidoc.{RoutesHelper, RouteEntry}


class ApiDocControllerSpec extends Specification {

  @ApiDoc(doc="""
    GET /api/v1/users/{id}

    DESCRIPTION
      Get user

    PARAMETERS 
      id: String <- ID of the user

    ERRORS
      400 User not found
      400 Syntax Error

    RESULT
      User
  """)
  def errorDoc() = true

  @ApiDoc(doc="""
    GET /api/v1/users/

    DESCRIPTION
      Get user

    PARAMETERS 
      id: String (header) <- ID of the user

    ERRORS
      400 User not found
      400 Syntax Error

    RESULT
      User
  """)
  def errorDoc2() = true

  def inCleanEnvironment()(func: => Unit): Boolean = {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      func
    }
    true
  }

  def getFutureResult(result: play.api.mvc.Result) =
    play.api.mvc.Action { result } (FakeRequest())

  def checkResult(
    result: play.api.mvc.Result,
    matcher: JsValue,
    statusCode: Int = OK,
    mustNotContain: List[String] = List()
  ){
    val futureResult = getFutureResult(result)

    if (contentType(futureResult) != Some("application/json"))
      throw new Exception("contentType(futureResult) is not Some(\"application/json\"), but "+contentType(futureResult))

    val json: JsValue = contentAsJson(futureResult)
    matchJson(matcher, json)

    mustNotContain.foreach(contentAsString(futureResult) must not contain(_))
  }


  "ApiDoc controller" should {

    "pass the annotation tests" in { // First run the smallest unit tests.
      TestByAnnotation.TestObject(new ApiDocController)
      true
    }

    "validate that the validate method that validates if method and uri in conf/routes and autodoc matches works" in {
      inCleanEnvironment() {
        val controller = new ApiDocController

        val routeEntries = RoutesHelper.getRouteEntries()
        controller.validate(routeEntries)

        {
          val validRouteEntry = RouteEntry("GET", "/api/v1/users/$id<[^/]+>", "test.ApiDocControllerSpec", "errorDoc")
          controller.validate(validRouteEntry::routeEntries)
        }

        {
          val errorRouteEntry = RouteEntry("GET", "/api/v1/flapp", "test.ApiDocControllerSpec", "errorDoc")
          controller.validate(errorRouteEntry::routeEntries) should throwA[controller.UriMismatchException]
        }

        {
          val errorRouteEntry = RouteEntry("PUT", "/api/v1/users/$id<[^/]+>", "test.ApiDocControllerSpec", "errorDoc")
          controller.validate(errorRouteEntry::routeEntries) should throwA[controller.MethodMismatchException]
        }

        {
          val errorRouteEntry = RouteEntry("GET", "/api/v1/users", "test.ApiDocControllerSpec", "errorDoc")
          controller.validate(errorRouteEntry::routeEntries) should throwA[controller.UriMismatchException]
        }

        {
          val errorRouteEntry = RouteEntry("GET", "/api/v1/users/$id<[^/]+>", "test.ApiDocControllerSpec", "errorDoc2")
          controller.validate(errorRouteEntry::routeEntries) should throwA[controller.UriMismatchException]
        }
      }
    }

    "Call Swagger.getJson on all annotated resource path groups, in order to run validation checks on them" in {
      inCleanEnvironment() {
        ApiDocController.validate("/")
      }
    }

    "return main swagger menu" in {
      inCleanEnvironment() {

        val controller = new ApiDocController

        checkResult(
          controller.get(),
          Json.obj(
            "paths" -> Json.obj(
              ___allowOtherFields
            ),
            ___allowOtherFields
          )
        )
      }
    }

  }
}

