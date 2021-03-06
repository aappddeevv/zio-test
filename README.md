# ZIO bifunctor testbed

Does ZIO's bifunctor design offer a better abstraction? And if so, when does
it work best?

This repo is a simple, quick testbed to explore a few questions around zio.

All the code assumes the ZIO IO so there are no `F` parameters in the algebras,
only an `E` parameter.

Tests are written in scalatest so there really is no useful main.


## Test1 - Multi-layer web client.

Create 3 layers of an HTTP client based on http4s--just the essentials. A client
is really a Kleisli `HttRequest => IO[E, HttpResponse]`.

We want to have each layer declare its own error type and the idea is to blend
layers together and have precise error types.

* Backend: A OS/runtime environment specific HTTP client. This is completed
  faked in this test to return exactly what I want e.g. errors :-)
* Client: Uses the lowest level layer to provide a friendly API.
* OData: An OData 4.0 REST client, totally fake as well.

Findings:
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
* Wow! It's complex to think through the outer layers of your API where you
  interface into code that throws exceptions for errors. If you don't trap the
  throws there, the upper layers require even more thought. This is a statement
  that says reasoning about your code is easier if errors are explicit values.
* If one layer of software returns Either, trying to be functional, but other
  layers return Throwable, you can merge these types together through value
  conversion. The opposite direction conversion is also possible. If you did not
  have IO[E,A], you would only have the ability to convert to a Throwable sum
  type.
* The type signatures are much clearer once the errors have been handled with
  ZIO. You you are more confident that the effect is not hiding an error
  somewhere in the effect that is not represented explicitly.
* Without an explicit E, it is possible to wrap all objects in some exception
  derived classes although you would need to unwrap to access the
  data. Essentially, explicit E removes one layer of indirection/wrapping that
  exists when you have to use Throwable derived classes.
* The point of bifunctor IO is not get rid of Throwable but to be explicit about
  the state of the data.

## Test 2

...TBD...

## Test Conclusions

Some conclusions:

* In highly failure prone environments where the IO is likely to fail more
frequently than not *and* you want to recover in architecture layers other than
the outermost layer, an explicit `E` seems to improve code reasoning.
  * High failure environments include applications such as web UIs or user
    oriented applications. Some operational, analytical data pipelines also fall
    into thas category. Batch oriented data pipelines probably do not fall into
    this category as often.
* If you are working on batch oriented programs whose failure model is to
recover by restarting the program, the explicit `E` is less important.
* Errors should be handled at units-of-work (roughly speaking) such as the
"record processing level" or the program level. An explicit `E` helps you manage
errors across units-of-work if your program has multiple granularities of
units-of-work.
* dotty's sum types will make ZIO more useful.
* Given that `E` allows you to explicitly handle errors in a certain layer (and
  not jump the stack), it forces your code to be referentially transparent when
  that is important in your architecture. You cannot be referentially
  transparent if you throw exceptions.
* ZIO does not preclude you from jumping the stack if you want to still handle
  exceptions the traditional way so don't lose this type of flexbility.
* Bifunctor IO is not about getting rid of Throwable and replacing it with an
  explicit E, although you can do that. Bifunctor IO is really about being
  explicit about the possible "error" that an effect could have including the
  ability to declare that an effect cannot contain an error.

To me, potentially the most important conclusion is that a type parameter `E` in
`IO` gives you an option of handling errors differently. Generally, more choices
are good assuming the increase in complexity is not large. The tests suggest
that the complexity is moderate and can become learned behavior fairly quickly.

On a separate note, integrating ZIO into a cats-core based application appears
to be difficult.

## Declaring Error Intent with Types
It's clear that an API can lie. You could declare a `IO[E,A]` with an
`E=Nothing` but still have that IO throw something you were not expecting that
cause the IO to fail in an "unchecked way." Because the failure was unexpected
according to the type, its a defect that the programmer must resolve. It's not a
recoverable error.

Suppose you declare your IO to be `IO[Nothing,A]`. If there is a throw that you
did not handle as a programmer then the run time system for ZIO will have an
exit status of "failed" with a `Cause` of `Unchecked`. If we had handed the
error inside our code and translated that to a `A` our type is good and we can
return that as an ExitStatus of `Succeeded`. If the IO fails with another error
`E` (remember this E is not in our `IO[Nothing,A]`), we may *want* to "fail" the
IO with a `Cause` of `Checked` `E`. But since the IO type just mentioned is
`IO[Nothing,A]`, we can't return an `Checked[E]`.

So *if* your `E=Nothing` the only `ExitStatus[Nothing,A]` you can create is a
`Unchecked` error, which has type `Cause[Nothing]`, or an `Interruption`
concept, which is also of type `Cause[Nothing]`.

When we say "return a value" in the asynchronous case, we use the runtime system
method `unsafeRunAsync` to run the IO:

```scala
trait RTS { 
  def unsafeRunAsync[E, A](io: IO[E, A])(k: (ExitResult[E, A]) ⇒ Unit): Unit
}
```

If the IO we are running is `IO[Nothing,A]` then our "callback" can only be a
`ExitResult[Nothing,A] => Unit`. The presence of `Nothing` therefore indicates
that the only way the IO can fail is by the IO being interrupted or an unchecked
exception is thrown.

In contrast, if we just had a cats `IO[A]`. We know that this IO can have an
exception in it. But its quite possible that the IO has be constructed so that
no Throwable will occur or that any Throwable is translated into an `A`. Even
*if* the error handling had provided via an `IO.recover` the signature would
still be `IO[A]`.
