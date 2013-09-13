package spire

import spire.syntax._

package object syntax {
  object cfor extends CforSyntax
  object literals extends LiteralsSyntax

  object eq extends EqSyntax
  object order extends OrderSyntax
  object signed extends SignedSyntax
  object isReal extends IsRealSyntax
  object convertableFrom extends ConvertableFromSyntax

  object semigroup extends SemigroupSyntax
  object monoid extends MonoidSyntax
  object group extends GroupSyntax

  object additiveSemigroup extends AdditiveSemigroupSyntax
  object additiveMonoid extends AdditiveMonoidSyntax
  object additiveGroup extends AdditiveGroupSyntax

  object multiplicativeSemigroup extends MultiplicativeSemigroupSyntax
  object multiplicativeMonoid extends MultiplicativeMonoidSyntax
  object multiplicativeGroup extends MultiplicativeGroupSyntax

  object semiring extends SemiringSyntax
  object rig extends RigSyntax
  object rng extends RngSyntax
  object ring extends RingSyntax
  object euclideanRing extends EuclideanRingSyntax
  object field extends FieldSyntax
  object nroot extends NRootSyntax

  object module extends ModuleSyntax
  object vectorSpace extends VectorSpaceSyntax
  object metricSpace extends MetricSpaceSyntax
  object normedVectorSpace extends NormedVectorSpaceSyntax
  object innerProductSpace extends InnerProductSpaceSyntax
  object coordinateSpace extends CoordinateSpaceSyntax

  object booleanAlgebra extends BooleanAlgebraSyntax

  object bitString extends BitStringSyntax

  object integral extends IntegralSyntax
  object fractional extends FractionalSyntax
  object numeric extends NumericSyntax

  object all extends AllSyntax
}
