package spire
package object scalacompat {

import scala.collection.mutable.ArrayBuilder
import scala.reflect.ClassTag

  def arrayBuilderMake[T]()(implicit tag: ClassTag[T]): ArrayBuilder[T] =
      ArrayBuilder.make[T]()

  type SeqLike[A, C] = scala.collection.SeqLike[A, C]
}
