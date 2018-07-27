footer: ##### ![inline](images/square-twitter-512.png) @alvaro_sanchez
slidenumbers: false

[.hide-footer]

# [fit] Async and event-driven 
# [fit] Grails applications

![inline](images/oci.png) ![inline](images/gr8conf-us.png)

Álvaro Sánchez-Mariscal
![inline](images//square-twitter-512.png)@alvaro_sanchez

---

![](images/alvaro.jpg)

# About me

- Coming from Madrid :es:
- Developer since 2001 (Java / Spring stack).
- Grails fanboy since v0.4.
- Working @ OCI since 2015: Groovy, Grails & Micronaut!
- Father since 2017! :family:

---

<br/>
<br/>

# [fit] Introduction

---

# Grails Async Framework

- [https://async.grails.org]()
- Introduced in Grails 2.3 with the Promise API. Spun-off in Grails 3.3
- Supports different async libraries: GPars, RxJava and Reactor.
- Application events, Spring events, GORM events.
- Async GORM.

---

# Grails Async Framework

- Server Sent Events.
- RxGORM.
- Async request/response processing.
- Servlet 3.0 async support.

---

# Getting started

- Add the dependency in `build.gradle`:

    ```groovy
    runtime "org.grails.plugins:async"
    ```
- Check the Grails plugin page for more information:
[http://plugins.grails.org/plugin/grails/async]()

---

<br/>
<br/>

# [fit] Servlet 3.0 API

---

# Servlet 3.0

- Spec'ed in 2009! :scream:
- Allows offloading of blocking operations to a different thread.
- Implement `grails.async.web.AsyncController` to get started.
- Call `startAsync()` to get the `javax.servlet.AsyncContext`
- When done, call `complete()` or `dispatch()`

---

<br/>
# [fit] Demo

---

# Advanced usage

```java
ctx.addListener(new AsyncListener() {

    void onStartAsync(AsyncEvent event) throws IOException {  }
    void onComplete(AsyncEvent event) throws IOException {  }
    void onTimeout(AsyncEvent event) throws IOException {  }
    void onError(AsyncEvent event) throws IOException {  }

})

ctx.timeout = 5_000
```

---

<br/>
<br/>

# [fit] Promise API

---

# Grails Promise API

- Builds on top of `java.util.concurrent.Future`.
- `grails.async.Promises` helps you to create `Promise<T>` instances.
- In controllers, better use `grails.async.web.WebPromises` (Servlet 3.0 Async under the covers)
- `PromiseFactory` allows pluggable implementations.

---

# `PromiseFactory` API

- `CachedThreadPoolPromiseFactory` (default): unbound thread pool.
- `GparsPromiseFactory`: if `org:grails:grails-async-gpars` dependecy is on the classpath.
- `RxPromiseFactory`: if either `org.grails:grails-async-rxjava` or `org.grails:grails-async-rxjava2` are on the classpath.

---

# `PromiseFactory` API

- `SynchronousPromiseFactory`: useful for unit testing.

    ```groovy
    import org.grails.async.factory.*
    import grails.async.*

    Promises.promiseFactory = new SynchronousPromiseFactory()
    ```

---

<br/>
# [fit] Demo

---

# `PromiseList`

```groovy
import grails.async.*

PromiseList<Integer> list = new PromiseList<>()
list << { 2 * 2 }
list << { 4 * 4 }
list << { 8 * 8 }

list.onComplete { List<Integer> results ->
  assert [4,16,64] == results
}
```

---

# `PromiseMap`

```groovy
import static grails.async.Promises.*

PromiseMap promiseMap = tasks   one:  { 2 * 2 },
                                two:  { 4 * 4},
                                three:{ 8 * 8 }

assert [one:4,two:16,three:64] == promiseMap.get()
```

---

# `@DelegateAsync`

```java
import grails.async.*

class BookService {
    List<Book> findBooks(String title) { ... }
}

class AsyncBookService {
   @DelegateAsync BookService bookService
}
```

---

# `@DelegateAsync`

```java
import grails.async.*

class BookService {
    List<Book> findBooks(String title) { ... }
}

class AsyncBookService { //Equivalent
    Promise<List<Book>> findBooks(String title) {
        task {
            bookService.findBooks(title)
        }
    }
}
```

---

# Using the service

```java
@Autowired AsyncBookService asyncBookService

def findBooks(String title) {
    asyncBookService.findBooks(title)
       .onComplete { List<Book> results ->
          render "Books = ${results}"
       }
}
```

---

<br/>
<br/>

# [fit] Events

---

# Grails Events abstraction

- Add the dependency in `build.gradle`:

    ```groovy
    runtime "org.grails.plugins:events"
    ```
- By default Grails creates an `EventBus` based off of the currently active `PromiseFactory`.
- Or you can use:
    - `org.grails:grails-events-gpars`
    - `org.grails:grails-events-rxjava`
    - `org.grails:grails-events-rxjava2`

---

# Publishing events

- With the `@Publisher` annotation:

    ```java
    class SumService {
        @Publisher
        int sum(int a, int b) {
            a + b
        }
    }
    ```

---

# Publishing events

- With the `EventPublisher` API:

    ```java
    class SumService implements EventPublisher {
        int sum(int a, int b) {
            int result = a + b
            notify("sum", result)
            return result
        }
    }
    ```

---

# Subscribing to events

- With the `@Subscriber` annotation:

    ```java
    class TotalService {
        AtomicInteger total = new AtomicInteger(0)
        @Subscriber
        void onSum(int num) {
            total.addAndGet(num)
        }
    }
    ```

---

# Subscribing to events

- With the `EventBusAware` API:

```java
class TotalService implements EventBusAware {
    AtomicInteger total = new AtomicInteger(0)

    @PostConstruct
    void init() {
        eventBus.subscribe("sum") { int num ->
            total.addAndGet(num)
        }
    }
}
```

---

# GORM events

- `DatastoreInitializedEvent`
- `PostDeleteEvent`
- `PostInsertEvent`
- `PostLoadEvent`
- `PostUpdateEvent`

---

# GORM events

- `PreDeleteEvent`
- `PreInsertEvent`
- `PreLoadEvent`
- `PreUpdateEvent`
- `SaveOrUpdateEvent`
- `ValidationEvent`

---

# Asynchronous subscription

- They cannot cancel or manipulate the persistence operations.

    ```java
    @Subscriber
    void beforeInsert(PreInsertEvent event) {
        //do stuff
    }
    ```

---

# Synchronous subscription

```java
@Listener
void tagFunnyBooks(PreInsertEvent event) {
    String title = event.getEntityAccess()
                        .getPropertyValue("title")
    if(title?.contains("funny")) {
        event.getEntityAccess()
                .setProperty("title", "Humor - ${title}".toString())
    }
}
```

--- 

# Spring events

- Disabled by default, for performance reasons.
- Enable them by setting `grails.events.spring` to `true`.

```java
@Events(namespace="spring")
class MyService {
    @Subscriber
    void applicationStarted(ApplicationStartedEvent event) {
        // fired when the application starts
    }
    @Subscriber
    void servletRequestHandled(RequestHandledEvent event) {
        // fired each time a request is handled
    }
}
```

---

<br/>
<br/>

# [fit] Async GORM

---

# Get started

- _NOTE_: drivers are still blocking. This just offloads it to a different thread pool.
- Include `compile "org.grails:grails-datastore-gorm-async"` in your build.
- Implement the `AsyncEntity` trait:

    ```groovy
    class Book implements AsyncEntity<Book> {
        ...
    }
    ```

---

# The `async` namespace

```groovy
Promise<Person> p1 = Person.async.get(1L)
Promise<Person> p2 = Person.async.get(2L)
List<Person> people = waitAll(p1, p2) //Blocks

Person.async.list().onComplete { List<Person> results ->
  println "Got people = ${results}"
}

Promise<Person> person = Person.where {
    lastName == "Simpson"
}.async.list()
```

---

# [fit] Async and the Hibernate session

- The Hibernate session is not concurrency safe.
- Objects returned from asynchronous queries will be detached entities.
- This will fail:

    ```java
    Person p = Person.async.findByFirstName("Homer").get()
    p.firstName = "Bart"
    p.save()
    ```

---

# [fit] Async and the Hibernate session

- Entities need to be merged first with the session bound to the calling thread

    ```groovy
    Person p = Person.async.findByFirstName("Homer").get()
    p.merge()
    p.firstName = "Bart"
    p.save()
    ```
- Also, be aware that association lazy loading will not work. Use eager queries / fetch joins / etc.

---

# Multiple async GORM calls

```groovy
Promise<Person> promise = Person.async.task {
    withTransaction {
       Person person = findByFirstName("Homer")
       person.firstName = "Bart"
       person.save(flush:true)
    }
}

Person updatedPerson = promise.get()
```

---

# Async models

```groovy
import static grails.async.WebPromises.*

def index() {
   tasks books: Book.async.list(),
         totalBooks: Book.async.count(),
         otherValue: {
           // do hard work
         }
}
```

---

<br/>
<br/>

# [fit] RxJava Support

---

# RxJava support

- Add `org.grails.plugins:rxjava` to your dependencies.
- You can then return `rx.Observable`'s from controllers, and Grails will:
    1. Create a new asynchronous request
    2. Spawn a new thread that subscribes to the observable
    3. When the observable emits a result, process the result using the respond method.

--- 

<br/>
# [fit] Demo

---

[.hide-footer]

# [fit] Q & A

![inline](images/oci.png) ![inline](images/gr8conf-us.png)

Álvaro Sánchez-Mariscal
![inline](images//square-twitter-512.png)@alvaro_sanchez