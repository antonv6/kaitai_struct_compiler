package io.kaitai.struct.exprlang

import io.kaitai.struct.exprlang.Ast._
import io.kaitai.struct.exprlang.Ast.expr._
import io.kaitai.struct.exprlang.Ast.operator._
import io.kaitai.struct.exprlang.Ast.cmpop._
import io.kaitai.struct.exprlang.Ast.unaryop._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.collection.mutable.ArrayBuffer

class ExpressionsSpec extends FunSpec {
  describe("Expressions.parse") {
    it("parses single positive integer") {
      Expressions.parse("123") should be (IntNum(123))
    }

    it("parses single negative integer") {
      Expressions.parse("-456") should be (UnaryOp(Minus, IntNum(456)))
    }

    it("parses hex integer") {
      Expressions.parse("0x1234") should be (IntNum(0x1234))
    }

    it("parses 1 + 2") {
      Expressions.parse("1 + 2") should be (BinOp(IntNum(1), Add, IntNum(2)))
    }

    it("parses 1 + 2 + 5") {
      Expressions.parse("1 + 2 + 5") should be (
        BinOp(BinOp(IntNum(1), Add, IntNum(2)), Add, IntNum(5))
      )
    }

    it("parses (1 + 2) / (7 * 8)") {
      Expressions.parse("(1 + 2) / (7 * 8)") should be (
        BinOp(
          BinOp(IntNum(1), Add, IntNum(2)),
          Div,
          BinOp(IntNum(7), Mult, IntNum(8))
        )
      )
    }

    it("parses 1 < 2") {
      Expressions.parse("1 < 2") should be (Compare(IntNum(1), Lt, IntNum(2)))
    }

    it("parses a[42]") {
      Expressions.parse("a[42]") should be (Subscript(Name(identifier("a")), IntNum(42)))
    }

    it("parses a[42 - 2]") {
      Expressions.parse("a[42 - 2]") should be (
        Subscript(
          Name(identifier("a")),
          BinOp(IntNum(42), Sub, IntNum(2))
        )
      )
    }

    it("parses 2 < 3 ? \"foo\" : \"bar\"") {
      Expressions.parse("2 < 3 ? \"foo\" : \"bar\"") should be (
        IfExp(
          Compare(IntNum(2), Lt, IntNum(3)),
          Str("foo"),
          Str("bar")
        )
      )
    }

    it("parses bitwise invert operation") {
      Expressions.parse("~777") should be (UnaryOp(Invert, IntNum(777)))
    }

    it("parses ~(7+3)") {
      Expressions.parse("~(7+3)") should be (UnaryOp(Invert, BinOp(IntNum(7), Add, IntNum(3))))
    }

    it("parses port::http") {
      Expressions.parse("port::http") should be (EnumByLabel(identifier("port"), identifier("http")))
    }

    it("parses port::http.to_i + 8000 == 8080") {
      Expressions.parse("port::http.to_i + 8000 == 8080") should be (
        Compare(
          BinOp(
            Attribute(
              EnumByLabel(identifier("port"),identifier("http")),
              identifier("to_i")
            ),
            Add,
            IntNum(8000)
          ),
          Eq,
          IntNum(8080)
        )
      )
    }

    it("parses [1, 2, 0x1234]") {
      Expressions.parse("[1, 2, 0x1234]") should be (
        List(ArrayBuffer(IntNum(1), IntNum(2), IntNum(4660)))
      )
    }
  }
}
