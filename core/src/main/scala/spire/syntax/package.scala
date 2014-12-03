package spire

package object syntax {
  object cfor extends CforSyntax
  object literals extends LiteralsSyntax

  object eq extends EqSyntax
  object partialOrder extends PartialOrderSyntax
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
  object trig extends TrigSyntax

  object vectorSpace extends VectorSpaceSyntax
  object metricSpace extends MetricSpaceSyntax
  object normedVectorSpace extends NormedVectorSpaceSyntax
  object innerProductSpace extends InnerProductSpaceSyntax

  object lattice extends LatticeSyntax
  object heyting extends HeytingSyntax
  object bool extends BoolSyntax

  object bitString extends BitStringSyntax

  object groupAction extends GroupActionSyntax
  object torsor extends TorsorSyntax

  object integral extends IntegralSyntax
  object fractional extends FractionalSyntax
  object numeric extends NumericSyntax

  object all extends AllSyntax

  object unbound extends UnboundSyntax
}
