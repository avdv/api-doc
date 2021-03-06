package test.lib

import org.specs2.mutable._
import play.api.test._
import play.api.libs.json._

import no.samordnaopptak.apidoc.ApiDocValidation



class ApiDocValidationSpec extends Specification {

  class Inner1{
    case class Inner2()
  }

  "ApiDocValidation" should {
    "Get Class objects from inner classes" in {
      play.api.test.Helpers.running(FakeApplication()) {
        ApiDocValidation.loadInnerClass("test.lib.ApiDocValidationSpec.Inner1.Inner2")
        true
      }
    }

    "Validate data type fields, with no added or removed fields" in {
      play.api.test.Helpers.running(FakeApplication()) {
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "attributes", "unrelated"),        Set(), Set())
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id"),                                   Set(), Set()) should throwA[ApiDocValidation.MismatchFieldException]
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "attributes", "unrelated", "id2"), Set(), Set()) should throwA[ApiDocValidation.MismatchFieldException]
      }
    }

    "Validate data type fields, with added field" in {
      play.api.test.Helpers.running(FakeApplication()) {
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "id2", "attributes", "unrelated"),        Set("id2"), Set())
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "id2", "attributes", "unrelated"),        Set("id"),  Set()) should throwA[ApiDocValidation.AlreadyDefinedFieldException]
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "attributes", "unrelated"),               Set("id2"), Set()) should throwA[ApiDocValidation.MismatchFieldException]
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "id2", "id3", "attributes", "unrelated"), Set("id2"), Set()) should throwA[ApiDocValidation.MismatchFieldException]
      }
    }

    "Validate data type fields, with removed field" in {
      play.api.test.Helpers.running(FakeApplication()) {
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("attributes", "unrelated"),              Set(),     Set("id"))
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id","attributes", "unrelated"),         Set("id"), Set("id"))  should throwA[ApiDocValidation.AlreadyDefinedFieldException]
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("unrelated"),                            Set(),     Set("id"))  should throwA[ApiDocValidation.MismatchFieldException]
        ApiDocValidation.validateDataTypeFields("test.lib.User", "hepp", Set("id", "attributes", "unrelated"),        Set(),     Set("id2")) should throwA[ApiDocValidation.UnknownFieldException]
      }
    }


  }

}


