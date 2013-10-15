package spire.random
package mutable

final class SyncGenerator(gen: Generator) extends Generator {
  def copy: SyncGenerator = new SyncGenerator(gen.copy)

  override def sync: SyncGenerator = this

  def getSeedBytes(): Array[Byte] = gen.getSeedBytes()

  def setSeedBytes(bytes: Array[Byte]): Unit = gen.setSeedBytes(bytes)

  def nextInt(): Int = this.synchronized { gen.nextInt() }

  def nextLong(): Long = this.synchronized { gen.nextLong() }
}

object SyncGenerator {
  def apply(gen: Generator) = new SyncGenerator(gen)
}
