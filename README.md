# Producer-Consumer Problem — Java Multithreading POC

A simple Java implementation of the **Producer-Consumer Problem** using multithreading, synchronization, `wait()`, and `notifyAll()`.

This POC focuses on understanding how multiple threads can safely communicate through a **shared resource** without causing inconsistent data or unnecessary thread execution.

---

## 📌 What is the Producer-Consumer Problem?

The **Producer-Consumer Problem** is a classic multithreading problem where:

* A **Producer** generates data.
* A **Consumer** consumes that data.
* Both threads share the same resource.
* The Producer should produce only when the resource is empty.
* The Consumer should consume only when the resource contains data.

The main challenge is to make sure that both threads do not access or modify the shared resource incorrectly at the same time.

### Simple Example

Think of a single-slot box:

```text
Producer
   ↓
[ Shared Resource ]
   ↓
Consumer
```

The Producer puts a value into the box.

```text
[ 10 ]
```

While the value is present, the Producer must wait.

The Consumer takes the value:

```text
[ Empty ]
```

Now the Producer can produce the next value.

This continues until all values are processed.

---

## 🎯 Why is this Problem Important?

The Producer-Consumer problem helps understand several important concepts in Java multithreading:

* Thread synchronization
* Shared resources
* Race conditions
* Thread communication
* `synchronized`
* `wait()`
* `notifyAll()`
* Thread coordination
* Critical sections
* Producer and Consumer execution states

These concepts are commonly used in real-world concurrent systems such as:

* Task queues
* Job processing systems
* Message queues
* Background workers
* Web servers
* Data pipelines
* Logging systems

---

# 🔑 Problems We Need to Solve

Without proper synchronization, two major problems can occur.

### 1. Producer should not overwrite existing data

Suppose the Producer produces:

```text
10
```

Before the Consumer consumes `10`, if the Producer produces:

```text
20
```

the previous value may be overwritten.

So we need:

```text
If resource is available
        ↓
Producer must WAIT
```

---

### 2. Consumer should not consume empty data

If the Consumer tries to consume when there is no value:

```text
Resource = EMPTY
```

the Consumer must wait.

So:

```text
If resource is NOT available
        ↓
Consumer must WAIT
```

---

# 🧠 How This POC Solves the Problem

The shared `Resource` uses a boolean flag:

```java
boolean available = false;
```

This represents whether the resource currently contains a value.

### Resource States

```text
available = false
        ↓
Resource is EMPTY
        ↓
Consumer waits
```

and

```text
available = true
        ↓
Resource contains a value
        ↓
Producer waits
```

---

# 🔐 Why `synchronized`?

Both Producer and Consumer access the same `Resource`.

Therefore, the methods that modify/read the shared state are synchronized:

```java
synchronized void put(int value)
```

and

```java
synchronized int get()
```

`synchronized` ensures that only one thread at a time can execute these methods for the same `Resource` object.

This protects the shared data from inconsistent updates.

---

# ⏸️ Why `wait()`?

`wait()` allows the current thread to temporarily stop executing and release the object's monitor.

### Producer

```java
while (available) {
    wait();
}
```

Meaning:

> "If the resource already contains data, I cannot produce another value. I will wait."

### Consumer

```java
while (!available) {
    wait();
}
```

Meaning:

> "If the resource is empty, I cannot consume anything. I will wait."

---

# 🔔 Why `notifyAll()`?

After changing the resource state, the current thread needs to wake up waiting threads.

Producer:

```java
available = true;
notifyAll();
```

The Producer has produced a value, so the Consumer can continue.

Consumer:

```java
available = false;
notifyAll();
```

The Consumer has consumed the value, so the Producer can continue.

### Communication Flow

```text
Producer
   |
   | put()
   ↓
Resource becomes available
   |
   | notifyAll()
   ↓
Consumer wakes up
   |
   | get()
   ↓
Resource becomes unavailable
   |
   | notifyAll()
   ↓
Producer wakes up
```

This creates communication between the two threads.

---

# 🔄 Overall Execution Flow

```text
             Shared Resource
                   |
            available = false
                   |
                   ↓
             Producer runs
                   |
                put(0)
                   |
          available = true
                   |
              notifyAll()
                   |
                   ↓
             Consumer runs
                   |
                 get()
                   |
          available = false
                   |
              notifyAll()
                   |
                   ↓
             Producer runs
                   |
                put(1)
                   |
                  ...
```

The Producer and Consumer coordinate their execution instead of continuously checking the resource.

---

# 🏗️ Project Structure

```text
ProducerConsumer.java
```

The program contains four main components:

### `Resource`

The shared object accessed by both threads.

Responsibilities:

* Store the value
* Track whether a value is available
* Control Producer access
* Control Consumer access
* Coordinate threads using `wait()` and `notifyAll()`

### `Producer`

Implements `Runnable`.

Its responsibility is to generate values and put them into the shared resource.

```java
r.put(i);
```

### `Consumer`

Implements `Runnable`.

Its responsibility is to retrieve values from the shared resource.

```java
r.get();
```

### `ProducerConsumer`

Contains the `main()` method and creates both threads.

```java
Thread producer = new Thread(new Producer(r), "Producer");
Thread consumer = new Thread(new Consumer(r), "Consumer");
```

Both threads receive the **same Resource object**.

That shared object is what allows them to communicate.

---

# 🧩 Core Concepts Demonstrated

| Concept             | Purpose                             |
| ------------------- | ----------------------------------- |
| `Thread`            | Executes tasks concurrently         |
| `Runnable`          | Defines Producer and Consumer tasks |
| Shared Object       | Allows threads to communicate       |
| `synchronized`      | Protects shared resource            |
| `wait()`            | Makes a thread wait for a condition |
| `notifyAll()`       | Wakes waiting threads               |
| `while`             | Rechecks the condition after waking |
| `boolean available` | Represents resource state           |

---

# ⚠️ Why `while` Instead of `if`?

The condition is checked using:

```java
while (available) {
    wait();
}
```

and:

```java
while (!available) {
    wait();
}
```

This is important because after a thread wakes up, the condition should be checked again before continuing.

The general pattern is:

```java
while (condition_is_not_satisfied) {
    wait();
}
```

This is the standard approach for condition-based thread coordination.

---

# 🧪 Expected Behavior

The exact order can vary because thread scheduling is controlled by the JVM and operating system.

A typical execution may look like:

```text
Producer : 0
Consumer : 0
Producer : 1
Consumer : 1
Producer : 2
Consumer : 2
Producer : 3
Consumer : 3
...
```

The important point is not the exact output order.

The important point is:

```text
Producer produces
        ↓
Consumer consumes
        ↓
Producer produces
        ↓
Consumer consumes
```

with both threads coordinating through the shared resource.

---

# 🚀 What This POC Demonstrates

This implementation demonstrates how to solve a classic concurrency problem by combining:

```text
Shared Resource
      +
synchronized
      +
wait()
      +
notifyAll()
      +
Condition Checking
      ↓
Thread Coordination
```

The Producer and Consumer do not simply run independently.

They **communicate through the shared Resource and coordinate their execution based on the resource state.**

---

## 💡 Key Takeaway

The most important idea behind the Producer-Consumer problem is:

> **Threads should not just execute concurrently; they should coordinate safely when they share data.**

This POC helped me understand how Java threads can:

* Safely access shared data
* Wait for a required condition
* Notify other waiting threads
* Avoid overwriting data
* Avoid consuming unavailable data
* Coordinate execution using synchronization

---

## 🛠️ Technologies

* Java
* Core Java
* Multithreading
* Synchronization
* Inter-thread Communication

---

## 📚 Learning Focus

This POC was implemented as part of my **Java Multithreading practice**, with a focus on understanding thread communication and synchronization through hands-on coding.

---

## 👩‍💻 Author

**Vishnupriya Jangati**

B.Tech CSE Graduate | Java Full Stack Developer | Generative AI Enthusiast

GitHub: [Jangativishnupriya](https://github.com/Jangativishnupriya)
