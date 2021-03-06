package io.kaitai.struct.translators

import io.kaitai.struct.exprlang.DataType._
import org.scalatest.FunSpec

class JavaScriptTranslatorSpec extends FunSpec with BaseTranslatorSpec {
  override def getTranslator(tp: TypeProvider): BaseTranslator = new JavaScriptTranslator(tp)

  describe("JavaTranslator.translate") {
    it("parses single positive integer") {
      tryOne(CalcIntType, "123", "123", CalcIntType)
    }

    it("parses single negative integer") {
      tryOne(CalcIntType, "-456", "-456", CalcIntType)
    }

    it("parses hex integer") {
      tryOne(CalcIntType, "0x1234", "4660", CalcIntType)
    }

    it("parses 1 + 2") {
      tryOne(CalcIntType, "1 + 2", "(1 + 2)", CalcIntType)
    }

    it("parses 3 / 2") {
      tryOne(CalcIntType, "3 / 2", "Math.floor(3 / 2)", CalcIntType)
    }

    it("parses 1 + 2 + 5") {
      tryOne(CalcIntType, "1 + 2 + 5", "((1 + 2) + 5)", CalcIntType)
    }

    it("parses (1 + 2) / (7 * 8)") {
      tryOne(CalcIntType, "(1 + 2) / (7 * 8)", "Math.floor((1 + 2) / (7 * 8))", CalcIntType)
    }

    it("parses 1 < 2") {
      tryOne(CalcIntType, "1 < 2", "1 < 2", BooleanType)
    }

    it("parses a[42]") {
      tryOne(ArrayType(CalcStrType), "a[42]", "this.a[42]", CalcStrType)
    }

    it("parses a[42 - 2]") {
      tryOne(ArrayType(CalcStrType), "a[42 - 2]", "this.a[(42 - 2)]", CalcStrType)
    }

    it("parses 2 < 3 ? \"foo\" : \"bar\"") {
      tryOne(CalcIntType, "2 < 3 ? \"foo\" : \"bar\"", "2 < 3 ? \"foo\" : \"bar\"", CalcStrType)
    }

    it("parses bitwise invert operation") {
      tryOne(CalcIntType, "~777", "~777", CalcIntType)
    }

    it("parses ~(7+3)") {
      tryOne(CalcIntType, "~(7+3)", "~(7 + 3)", CalcIntType)
    }

    it("parses foo of string type") {
      tryOne(CalcStrType, "foo", "this.foo", CalcStrType)
    }

    it("parses foo of user type") {
      tryOne(userType("block"), "foo", "this.foo", userType("block"))
    }

    it("parses foo.bar") {
      tryOne(new FooBarProvider, "foo.bar", "this.foo.bar", CalcStrType)
    }

    it("parses foo.inner.baz") {
      tryOne(new FooBarProvider, "foo.inner.baz", "this.foo.inner.baz", CalcIntType)
    }
  }
}
