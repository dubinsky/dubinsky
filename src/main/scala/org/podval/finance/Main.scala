package org.podval.finance

object Main:
  def main(args: Array[String]): Unit =
    val incomeTax = IncomeTax(220000, 70000)
    println(incomeTax)
    println(incomeTax.federalByBracket)
    ()