package spire.macros

object compat {

  type Context = scala.reflect.macros.Context

  def freshTermName[C <: Context](c: C)(s: String) =
    c.universe.newTermName(c.fresh(s))

  def termName[C <: Context](c: C)(s: String) =
    c.universe.newTermName(s)

  def typeCheck[C <: Context](c: C)(t: c.Tree) =
    c.typeCheck(t)

  def resetLocalAttrs[C <: Context](c: C)(t: c.Tree) =
    c.resetLocalAttrs(t)
}
