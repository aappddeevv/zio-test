# ZIO bifunctor testbed

Does ZIO's bifunctor design offer a better abstraction in some cases?

This repo is a simple testbed to explore that question.

Everything assumes the ZIO IO so there are no `F` parameters in the algebras,
only an `E` parameter.


## Test1
Create 3 layers of an HTTP client based on http4s--just the essentials. A client is really a Kleisli `HttRequest => IO[E, HttpResponse]`.

We want to have each layer declare its own error type and the idea is to blend
layers together and have a sane and precise error approach.

* Backend: A OS/runtime environment specific HTTP client. This is completed
  faked in this test to return exactly what I want e.g. errors :-)
* Client: Uses the lowest level layer to provide a friendly API.
* OData: An OData 4.0 REST client, totally fake as well.

