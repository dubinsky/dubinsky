package org.podval.finance

object Main:
  def main(args: Array[String]): Unit =
    val incomeTax = IncomeTax(347000, 82000)
    println(incomeTax)
    println(incomeTax.federalByBracket)
    ()