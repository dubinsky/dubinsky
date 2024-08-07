---
title: Zionomicon
tags: [scala]
---
- bought: 2020-10-29

- [Zionomicon](https://www.zionomicon.com/): [content](https://u349705.ct.sendgrid.net/ls/click?upn=0A2jstIsZ2QEj3AAObpAemRZObDrvhGpC1dVmtMqlopBHFb1BvBtsr2h-2BuCw1qgO1SgXRdk7-2F2oPwlh86oPvkw-3D-3Dja-I_RjOL1L8c45P25AhIfibmqTN2ljHNj1KgxUFBHANzExUAMB7l2rZOfA8OOFZTXwa6QK6OxqkexGRqTrhmVEw1q-2FC7FbQW-2BjaLNCqUs0N66JQ-2F1aYBaFRQXSnHlIVoOpnrELwrOnWmHlyyIg-2FvIhiBc-2F9prLYmZ2RyQTvHfFVkIEd7V2bchY5cU2Qvfmfg-2FXlgokk1B5QHQV1im6q0Dp1WJg9xI2SiXxFZnPimmpTU4zqWDtCn7cXZ4o9U5reDcVKjpdbiwyPFqXdInogMLhJX6etc8FTJsSLKGiVwAmFH9ZTcXT2IOtlGZM8ix7jfIvn1ho5MycHvOxz3rd8SFqV1UIKyWmg-2BaLmIqqYjGA-2BGppGs3-2FcwUJ9gj1azw6Iym21r)
- 4.10 Execution Tracing
  - Diagnosing failures in Future-based code is notoriously difficult, which is because the stack traces generated by Future code are not very helpful. Rather than showing the surrounding context, they show implementation details deep inside the implementation of Future.

  - To deal with this problem, ZIO includes a feature called execution tracing,
which provides extremely fine-grained details on the context surrounding failures. Execution tracing is turned on by default, and allows you to more quickly track down the root cause of problems in your code.

  - Because ZIO’s error channel is polymorphic, you can use your own data types
there, and ZIO has no way to attach execution tracing to unknown types. As a
result, to retrieve the execution trace of an error, you need the full Cause. Once you have the cause, you can simply call prettyPrint to convert the full cause information, including execution tracing, into a human-readable string.

  - In the following code snippet, the full cause data is logged to the console in the event of a failure:

```scala
lazy val effect: ZIO[Any, IOException, String] = ???
effect.tapCause(cause => console.putStrLn(cause.prettyPrint))
```
Note the method `ZIO#tapCause` is used, which allows us to “tap into” the cause of a failure, effectively doing something on the side (like logging) without changing the success or failure of the effect.

- 14 Resource handling