package io.kaitai.struct.translators

import io.kaitai.struct.Utils
import io.kaitai.struct.exprlang.Ast
import io.kaitai.struct.exprlang.Ast._
import io.kaitai.struct.exprlang.DataType.{BaseType, Int1Type}
import io.kaitai.struct.languages.CSharpCompiler

class CSharpTranslator(provider: TypeProvider) extends BaseTranslator(provider) {
  override def doArrayLiteral(t: BaseType, value: Seq[expr]): String = {
    t match {
      case Int1Type(_) =>
        val commaStr = value.map((v) => s"${translate(v)}").mkString(", ")
        s"new byte[] { $commaStr }"
      case _ =>
        val nativeType = CSharpCompiler.kaitaiType2NativeType(t)
        val commaStr = value.map((v) => translate(v)).mkString(", ")
        s"new List<$nativeType> { $commaStr }"
    }
  }

  override def doName(s: String) =
    if (s.startsWith("_"))
      s"M${Utils.upperCamelCase(s)}"
    else
      s"${Utils.upperCamelCase(s)}"

  override def doEnumByLabel(enumType: String, label: String): String =
    s"${Utils.upperCamelCase(enumType)}.${Utils.upperCamelCase(label)}"

  override def doStrCompareOp(left: Ast.expr, op: Ast.cmpop, right: Ast.expr) = {
    if (op == Ast.cmpop.Eq) {
      s"${translate(left)} == ${translate(right)}"
    } else if (op == Ast.cmpop.NotEq) {
      s"${translate(left)} != ${translate(right)}"
    } else {
      s"(${translate(left)}.CompareTo(${translate(right)}) ${cmpOp(op)} 0)"
    }
  }

  override def doSubscript(container: expr, idx: expr): String =
    s"${translate(container)}[${translate(idx)}]"
  override def doIfExp(condition: expr, ifTrue: expr, ifFalse: expr): String =
    s"${translate(condition)} ? ${translate(ifTrue)} : ${translate(ifFalse)}"

  // Predefined methods of various types
  override def strToInt(s: expr, base: expr): String =
  s"long.Parse(${translate(s)})"
  override def strLength(s: expr): String =
    s"${translate(s)}.Length"
  override def strSubstring(s: expr, from: expr, to: expr): String =
    s"${translate(s)}.Substring(${translate(from)}, ${translate(to)} - ${translate(from)})"

  override def arrayFirst(a: expr): String =
    s"${translate(a)}[0]"
  override def arrayLast(a: expr): String = {
    val v = translate(a)
    s"$v[$v.Length - 1]"
  }
}
