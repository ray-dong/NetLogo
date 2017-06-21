// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.core.{ SourceLocation, Token, TokenType, TokenDSL },
  TokenDSL.{ `(`, `)`, `[`, `]`, id, lit }
import PrimDSL._

class DelayedBlockTests extends FunSuite {
  val openBracket  = new Token("[", TokenType.OpenBracket, null)(SourceLocation(5, 6, "file.nlogo"))
  val closeBracket = new Token("]", TokenType.CloseBracket, null)(SourceLocation(14, 15, "file.nlogo"))
  val unterminatedTokens =
    BracketGroup(Seq(Atom(unid("foo")), Atom(unid("bar"))), openBracket, closeBracket)
  val arrowTokens =
    BracketGroup(Seq(
      BracketGroup(Seq(Atom(unid("foo"))), `[`, `]`),
      Atom(`->`),
      Atom(unid("foo"))),
      openBracket,
      closeBracket)

  lazy val delayBlock = DelayedBlock(unterminatedTokens, SymbolTable.empty)
  lazy val arrowBlock = DelayedBlock(arrowTokens,        SymbolTable.empty)

  def arrow(args: Seq[String], body: Seq[Token]): DelayedBlock =
    DelayedBlock(BracketGroup(
      Seq(
        BracketGroup(args.map(unid).map(Atom.apply _), `[`, `]`),
        Atom(`->`)) ++ body.map(Atom.apply _),
    openBracket, closeBracket), SymbolTable.empty)

  def anyBlock(body: Seq[Token]): DelayedBlock =
    DelayedBlock(BracketGroup(body.map(Atom.apply _), openBracket, `]`), SymbolTable.empty)

  def command(text: String): Token =
    Token(text, TokenType.Command, null)(SourceLocation(0, 0, "test"))

  test("file matches openBracket") { assert(delayBlock.filename == openBracket.filename) }
  test("start matches openBracket") { assert(delayBlock.start == openBracket.start) }
  test("end matches last unterminated token") { assert(delayBlock.end == closeBracket.end) }

  test("tokens is all tokens unterminated tokens with Eof at end") {
    assert(delayBlock.tokens.last.tpe == TokenType.Eof)
  }

  test("if it contains arrow token, is an arrow lambda") {
    assert(! delayBlock.isArrowLambda)
    assert(arrowBlock.isArrowLambda)
  }

  test("an ambiguous block containing a command is a command block") {
    testAmbiguousBlock(anyBlock(Seq(command("tick"))), { b => assert(b.isCommand) })
    testAmbiguousBlock(anyBlock(Seq(command("print"), lit(5))), { b => assert(b.isCommand) })
  }

  test("when a block is an arrow, allows access to tokens before arrow and after arrow") {
    testArrowBlock(arrowBlock, { alb =>
      assert(alb.argNames == Seq("FOO"))
      assert(alb.bodyGroups == Seq(Atom(lamvar("foo"))))
    })
  }

  test("when an arrow block contains a command, it is a command lambda") {
    testArrowBlock(arrow(Seq(), Seq(command("tick"))),
      alb => assert(alb.isCommand))
  }

  test("when an arrow block contains a parenthensized command, it is a command lambda") {
    testArrowBlock(arrow(Seq(), Seq(`(`, command("fd"), lit(2), `)`)),
      alb => assert(alb.isCommand))
  }

  test("when an arrow block contains no tokens, it is a command lambda") {
    testArrowBlock(arrow(Seq(), Seq()), alb => assert(alb.isCommand))
  }

  test("when an arrow block does not contain a command, it is a reporter lambda") {
    testArrowBlock(arrow(Seq(), Seq(lit(2))), alb => assert(! alb.isCommand))
  }

  def testArrowBlock(d: DelayedBlock, f: ArrowLambdaBlock => Unit): Unit = {
    d match {
      case alb: ArrowLambdaBlock => f(alb)
      case _ => fail("expected DelayedBlock with arrow to be ArrowLambdaBlock")
    }
  }

  def testAmbiguousBlock(d: DelayedBlock, f: AmbiguousDelayedBlock => Unit): Unit = {
    d match {
      case adb: AmbiguousDelayedBlock => f(adb)
      case _ => fail("expected DelayedBlock to be an AmbiguousDelayedBlock")
    }
  }
}
