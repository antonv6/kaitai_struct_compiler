package io.kaitai.struct.translators

import io.kaitai.struct.exprlang.DataType.{BaseType, CalcIntType, CalcStrType, UserTypeInstream}
import io.kaitai.struct.exprlang.Expressions
import org.scalatest.Matchers._

abstract trait BaseTranslatorSpec {
  case class Always(t: BaseType) extends TypeProvider {
    override def determineType(name: String): BaseType = t
    override def determineType(parentType: List[String], name: String): BaseType = t
  }

  class FooBarProvider extends TypeProvider {
    override def determineType(name: String): BaseType = {
      name match {
        case "foo" => userType("block")
      }
    }

    override def determineType(parentType: List[String], name: String): BaseType = {
      (parentType, name) match {
        case (List("block"), "bar") => CalcStrType
        case (List("block"), "inner") => userType("innerblock")
        case (List("innerblock"), "baz") => CalcIntType
      }
    }
  }

  /**
    * Tries one translation given setup described in parameters. This is a shortcut
    * version of method to be used when no named variables are present in the
    * expression.
    * @param srcStr expression string to translate
    * @param expectedStr expected result of translation
    * @param expectedType expected type of expression
    */
  def tryOne(srcStr: String, expectedStr: String, expectedType: BaseType): Unit = {
    tryOne(Always(CalcIntType), srcStr, expectedStr, expectedType)
  }

  /**
    * Tries one translation given setup described in parameters.
    * @param alwaysType all named variables will always resolve to this type
    * @param srcStr expression string to translate
    * @param expectedStr expected result of translation
    * @param expectedType expected type of expression
    */
  def tryOne(alwaysType: BaseType, srcStr: String, expectedStr: String, expectedType: BaseType): Unit = {
    tryOne(Always(alwaysType), srcStr, expectedStr, expectedType)
  }

  /**
    * Tries one translation given setup described in parameters.
    * @param tp type provider to use
    * @param srcStr expression string to translate
    * @param expectedStr expected result of translation
    * @param expectedType expected type of expression
    */
  def tryOne(tp: TypeProvider, srcStr: String, expectedStr: String, expectedType: BaseType): Unit = {
    val e = Expressions.parse(srcStr)
    val tr = getTranslator(tp)
    tr.detectType(e) should be(expectedType)
    tr.translate(e) should be(expectedStr)
  }

  def getTranslator(tp: TypeProvider): BaseTranslator

  def userType(name: String) = UserTypeInstream(List(name))
}
