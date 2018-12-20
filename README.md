# ZIO bifunctor testbed

Does ZIO's bifunctor design offer a better abstraction in some cases?

This repo is a simple testbed to explore that question.

Everything assumes the ZIO IO so there are no `F` parameters in the algebras,
only an `E` parameter.


## Test1 - Multi-layer web client.
Create 3 layers of an HTTP client based on http4s--just the essentials. A client
is really a Kleisli `HttRequest => IO[E, HttpResponse]`.

We want to have each layer declare its own error type and the idea is to blend
layers together and have precise error types.

* Backend: A OS/runtime environment specific HTTP client. This is completed
  faked in this test to return exactly what I want e.g. errors :-)
* Client: Uses the lowest level layer to provide a friendly API.
* OData: An OData 4.0 REST client, totally fake as well.

Conclusions:
* Without proper sum types, you cannot merge the error types from the different
  layers upward to the topmost layer if you wanted to, so its a bit more
  difficult to "combine" error types to handle at the top.
* If you want to stop all exceptions from propagating upward, you must supply a
  handler that handles a Throwable and converts it to an `E` in that layer. This
  should be expected but is made explicit give the type signature of IO.
* It is hard to break the habit instilled by cats that the error channel is
  built into `IO` already.
* Explicit `E` forces you to handle the error in the layer above or let it
  bubble upwards into the next layer based on your explicit choice. It's alot
  like checked exceptions, which I don't think are bad except that java did not
  have the language support to make it a useful feature when they introduced it
  into java. Checked exceptions have a bad reputation for that reason.
* It's not strictly necessary to parameterize on `E` but if you parameterize on
  `E`, you need to provide the ability to create an `E` (which is what the
  `mkError` functions do in each layer).
  
## Test 2

...TBD...

## Test Conclusions

Some conclusions:

* In highly failure prone environments where the IO is likely to fail more
frequently than not *and* you want to recover in architecture layers other than
the outermost layer, an explicit `E` seems to improve code reasoning.
* If you are working on batch oriented programs whose failure model is to recover
by restarting the program, the explicit `E` is less important.
* Errors should be handled at units-of-work (roughly speaking) such as the "record
processing level" or the program level. An explicit `E` helps you manage errors
across units-of-work if your program has multiple granularities of
units-of-work.
* dotty's sum types will make ZIO more useful.
* Given that `E` allows you to not jump the stack, it forces your code to be
  referentially transparent when that is important in your architecture.
* ZIO does not preclude you jumping the stack if you want to still handle things
  the traditional way.

Probably the most important conclusion is that a type parameter `E` in `IO`
gives you an option of handling errors differently *if* you want to. So choices
are good given that it is not overly complicated.
