package io.kaitai.struct.languages

import io.kaitai.struct.exprlang.Ast
import io.kaitai.struct.exprlang.DataType._
import io.kaitai.struct.format._

import scala.collection.mutable.ListBuffer

/**
  * Helper trait for languages where single parsing of every standard or user data type is done as expression, i.e. an
  * rvalue. In these languages, "attrStdTypeParse" is replaced with higher-level API: "stdTypeParseExpr" and
  * "handleAssignment".
  */
trait EveryReadIsExpression extends LanguageCompiler {
  def debug = false

  override def attrParse(attr: AttrLikeSpec, id: String, extraAttrs: ListBuffer[AttrSpec], io: String): Unit = {
    if (debug)
      attrDebugStart(id, io, NoRepeat)

    attr.cond.ifExpr match {
      case Some(e) =>
        instanceClear(id)
        condIfHeader(e)
        instanceSetCalculated(id)
      case None => // ignore
    }

    attr.cond.repeat match {
      case RepeatEos =>
        condRepeatEosHeader(id, io, attr.dataType, needRaw(attr.dataType))
        attrParse2(id, attr.dataType, io, extraAttrs, attr.cond.repeat)
        condRepeatEosFooter
      case RepeatExpr(repeatExpr: Ast.expr) =>
        condRepeatExprHeader(id, io, attr.dataType, needRaw(attr.dataType), repeatExpr)
        attrParse2(id, attr.dataType, io, extraAttrs, attr.cond.repeat)
        condRepeatExprFooter
      case NoRepeat =>
        attrParse2(id, attr.dataType, io, extraAttrs, attr.cond.repeat)
    }

    attr.cond.ifExpr match {
      case Some(e) => condIfFooter(e)
      case None => // ignore
    }

    if (debug)
      attrDebugEnd(id, io, NoRepeat)
  }

  def attrParse2(id: String, dataType: BaseType, io: String, extraAttrs: ListBuffer[AttrSpec], rep: RepeatSpec): Unit = {
    if (debug && rep != NoRepeat)
      attrDebugStart(id, io, rep)

    dataType match {
      case FixedBytesType(c, _) =>
        attrFixedContentsParse(id, c)
      case t: UserType =>
        attrUserTypeParse(id, dataType, io, extraAttrs, rep, t)
      case t: BytesType =>
        attrBytesTypeParse(id, dataType, io, extraAttrs, rep, t)
      case _ =>
        val expr = parseExpr(dataType, io)
        handleAssignment(id, expr, rep)
    }

    if (debug && rep != NoRepeat)
      attrDebugEnd(id, io, rep)
  }

  def attrBytesTypeParse(id: String, dataType: BaseType, io: String, extraAttrs: ListBuffer[AttrSpec], rep: RepeatSpec, t: BytesType): Unit = {
    // use intermediate variable name, if we'll be doing post-processing
    val rawId = t.process match {
      case None => id
      case Some(_) =>
        extraAttrs += AttrSpec(s"_raw_$id", t)
        s"_raw_$id"
    }

    val expr = parseExpr(dataType, io)
    handleAssignment(rawId, expr, rep)

    // apply post-processing
    t.process.foreach((proc) => attrProcess(proc, rawId, id))
  }

  def attrUserTypeParse(id: String, dataType: BaseType, io: String, extraAttrs: ListBuffer[AttrSpec], rep: RepeatSpec, t: UserType): Unit = {
    val newIO = t match {
      case knownSizeType: UserTypeKnownSize =>
        // we have a fixed buffer, thus we shall create separate IO for it
        val rawId = s"_raw_$id"
        val byteType = knownSizeType match {
          case UserTypeByteLimit(_, size, process) => BytesLimitType(size, process)
          case UserTypeEos(_, process) => BytesEosType(process)
        }

        attrParse2(rawId, byteType, io, extraAttrs, rep)

        val extraType = rep match {
          case NoRepeat => byteType
          case _ => ArrayType(byteType)
        }

        extraAttrs += AttrSpec(rawId, extraType)

        val ourIO = allocateIO(rawId, rep)
        if (needToStoreIOs) {
          extraAttrs += AttrSpec(ourIO, KaitaiStreamType)
          privateMemberName(ourIO)
        } else {
          ourIO
        }
      case _: UserTypeInstream =>
        // no fixed buffer, just use regular IO
        normalIO
    }
    val expr = parseExpr(dataType, newIO)
    handleAssignment(id, expr, rep)
  }

  def needRaw(dataType: BaseType): Boolean = {
    dataType match {
      case t: UserTypeKnownSize => true
      case _ => false
    }
  }

  def handleAssignment(id: String, expr: String, rep: RepeatSpec): Unit = {
    rep match {
      case RepeatExpr(_) => handleAssignmentRepeatExpr(id, expr)
      case RepeatEos => handleAssignmentRepeatEos(id, expr)
      case NoRepeat => handleAssignmentSimple(id, expr)
    }
  }

  def attrDebugStart(attrName: String, io: String, repeat: RepeatSpec): Unit = {}
  def attrDebugEnd(attrName: String, io: String, repeat: RepeatSpec): Unit = {}

  def handleAssignmentRepeatEos(id: String, expr: String): Unit
  def handleAssignmentRepeatExpr(id: String, expr: String): Unit
  def handleAssignmentSimple(id: String, expr: String): Unit

  def parseExpr(dataType: BaseType, io: String): String

  def needToStoreIOs: Boolean = false
}
